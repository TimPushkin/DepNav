/**
 * DepNav -- department navigator.
 * Copyright (C) 2026  Timofei Pushkin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.sqlite.SQLiteDataSource
import java.io.File
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import kotlin.io.extension
import kotlin.io.inputStream
import kotlin.io.nameWithoutExtension
import kotlin.io.writeBytes
import kotlin.use

private const val TABLE_NAME_PLACEHOLDER = "\${TABLE_NAME}"

/**
 * Generates maps database from latest Room schema and map-info JSONs.
 */
@CacheableTask
abstract class GenerateMapsDatabaseTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val schemaDirectory: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dataDirectory: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    @Suppress("UndocumentedPublicFunction") // Described by class'es doc
    fun generateMapsDatabase() {
        val schemaFile = requireNotNull(
            schemaDirectory.asFileTree
                .filter { it.extension == "json" }
                .maxByOrNull { it.nameWithoutExtension.toInt() }
        ) { "Database schema not found in ${schemaDirectory.asFile.get()}" }

        val dbFile = outputFile.asFile.get()
        dbFile.writeBytes(ByteArray(0)) // Ensure the file is empty

        // DriverManager sometimes fails to find the SQLite driver so using the DataSource API
        SQLiteDataSource().apply { url = "jdbc:sqlite:$dbFile" }.connection.use { connection ->
            connection.autoCommit = false
            connection.createStatement().use { it.createDatabaseObjects(schemaFile) }
            for (file in dataDirectory.asFileTree.files.filter { it.extension == "json" }) {
                connection.fillMapInfo(file)
            }
            connection.commit()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun Statement.createDatabaseObjects(schemaFile: File) {
        logger.info("Using schema from $schemaFile")
        val schema = schemaFile.inputStream().use { Json.decodeFromStream<RoomSchema>(it) }
        // Another format version would pobably fail to be parsed but let's check just in case
        if (schema.formatVersion != 1) {
            throw UnsupportedOperationException(
                "Database schema has formatVersion ${schema.formatVersion} but only 1 is supported"
            )
        }

        execute("PRAGMA user_version = ${schema.database.version}")

        for (entity in schema.database.entities) {
            createEntity(entity)
        }

        // To be implemented when a need arises
        if (schema.database.views.isNotEmpty()) {
            throw UnsupportedOperationException("Database schema contains views")
        }
    }

    private fun Statement.createEntity(entity: RoomSchema.Database.Entity) {
        execute(entity.createSql.replace(TABLE_NAME_PLACEHOLDER, entity.tableName))

        for (index in entity.indices) {
            execute(index.createSql.replace(TABLE_NAME_PLACEHOLDER, entity.tableName))
        }

        if (entity is RoomSchema.Database.Entity.Fts) {
            for (triggerCreateSql in entity.contentSyncTriggers) {
                // Table name seems to be hard-coded in contentSyncTriggers
                if (triggerCreateSql.contains(TABLE_NAME_PLACEHOLDER)) {
                    throw UnsupportedOperationException(
                        "Table name parameter in contentSyncTriggers: $triggerCreateSql"
                    )
                }
                execute(triggerCreateSql)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun Connection.fillMapInfo(mapInfoFile: File) {
        logger.info("Filling data from $mapInfoFile")
        val mapInfo = mapInfoFile.inputStream().use { Json.decodeFromStream<MapInfo>(it) }

        withPreparedStatement(
            "INSERT INTO map_info (id, internal_name, floor_width, floor_height, tile_size, levels_num, floors_num) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)"
        ) {
            setMapInfoParameters(mapInfo)
            executeUpdate()
        }

        withPreparedStatement(
            "INSERT INTO map_title (map_id, language_id, title) VALUES (?, ?, ?)"
        ) {
            for ((language, title) in mapInfo.title.toMap()) {
                setMapTitleParameters(mapInfo.id, language, title)
                addBatch()
            }
            executeBatch()
        }

        usePreparedStatements(
            "INSERT INTO marker (map_id, type, floor, x, y) VALUES (?, ?, ?, ?, ?) RETURNING id",
            "INSERT INTO marker_text (marker_id, language_id, title, location, description)" +
                    "VALUES (?, ?, ?, ?, ?)"
        ) { insertMarker, insertMarkerText ->
            for (floor in mapInfo.floors) {
                for (marker in floor.markers) {
                    insertMarker.setMarkerParameters(
                        mapInfo.id, floor.floor, mapInfo.floorWidth, mapInfo.floorHeight, marker
                    )
                    val markerId = insertMarker.executeQuery().use {
                        check(it.next())
                        it.getLong("id")
                    }
                    for ((language, textInfo) in marker.textInfos()) {
                        insertMarkerText.setMarkerTestParameters(markerId, language, textInfo)
                        insertMarkerText.addBatch()
                    }
                }
            }
            insertMarkerText.executeBatch()
        }
    }

    private inline fun Connection.withPreparedStatement(
        sql: String, action: PreparedStatement.() -> Unit
    ) {
        prepareStatement(sql).use { it.action() }
    }

    private inline fun Connection.usePreparedStatements(
        sql1: String, sql2: String, block: (PreparedStatement, PreparedStatement) -> Unit
    ) {
        prepareStatement(sql1).use { s1 -> prepareStatement(sql2).use { s2 -> block(s1, s2) } }
    }

    private fun PreparedStatement.setMapInfoParameters(mapInfo: MapInfo) {
        setInt(1, mapInfo.id)
        setString(2, mapInfo.internalName)
        setInt(3, mapInfo.floorWidth)
        setInt(4, mapInfo.floorHeight)
        setInt(5, mapInfo.tileSize)
        setInt(6, mapInfo.zoomLevelsNum)
        setInt(7, mapInfo.floors.size)
    }

    private fun PreparedStatement.setMapTitleParameters(
        mapId: Int, language: String, title: String
    ) {
        setInt(1, mapId)
        setString(2, language)
        setString(3, title)
    }

    private fun PreparedStatement.setMarkerParameters(
        mapId: Int, floor: Int, floorWidth: Int, floorHeight: Int, marker: MapInfo.Floor.Marker
    ) {
        setInt(1, mapId)
        setString(2, marker.type)
        setInt(3, floor)
        setDouble(4, marker.x.toDouble() / floorWidth)
        setDouble(5, marker.y.toDouble() / floorHeight)
    }

    private fun PreparedStatement.setMarkerTestParameters(
        markerId: Long, language: String, textInfo: MapInfo.Floor.Marker.TextInfo
    ) {
        setLong(1, markerId)
        setString(2, language)
        setString(3, textInfo.title)
        setString(4, textInfo.location)
        setString(5, textInfo.description)
    }
}
