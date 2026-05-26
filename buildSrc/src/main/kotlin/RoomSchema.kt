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

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.collections.contains

@Serializable
internal data class RoomSchema(
    val formatVersion: Int,
    val database: Database
) {
    @Serializable
    data class Database(
        val version: Int,
        val identityHash: String,
        val entities: List<Entity> = emptyList(),
        val views: JsonArray = JsonArray(emptyList()), // To be implemented once needed
        val setupQueries: List<String> = emptyList()
    ) {
        @Serializable(with = EntitySerializer::class)
        sealed interface Entity {
            val tableName: String
            val createSql: String
            val fields: JsonArray // Not interesting
            val primaryKey: JsonObject // Not interesting
            val indices: List<Index>
            val foreignKeys: JsonArray // Not interesting

            @Serializable
            data class Normal(
                override val tableName: String,
                override val createSql: String,
                override val fields: JsonArray,
                override val primaryKey: JsonObject,
                override val indices: List<Index> = emptyList(),
                override val foreignKeys: JsonArray = JsonArray(emptyList())
            ) : Entity

            @Serializable
            data class Fts(
                val ftsVersion: String,
                val ftsOptions: JsonObject, // Not interesting
                val contentSyncTriggers: List<String>,
                override val tableName: String,
                override val createSql: String,
                override val fields: JsonArray,
                override val primaryKey: JsonObject,
                override val indices: List<Index> = emptyList(),
                override val foreignKeys: JsonArray = JsonArray(emptyList())
            ) : Entity

            @Serializable
            data class Index(
                val name: String,
                val unique: Boolean,
                val columnNames: JsonArray, // Not Interesting
                val orders: JsonArray, // Not Interesting
                val createSql: String
            )
        }

        object EntitySerializer : JsonContentPolymorphicSerializer<Entity>(Entity::class) {
            override fun selectDeserializer(element: JsonElement) = when {
                "ftsVersion" in element.jsonObject -> Entity.Fts.serializer()
                else -> Entity.Normal.serializer()
            }
        }
    }
}
