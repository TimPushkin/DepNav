/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofey Pushkin
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

package ru.spbu.depnav.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.spbu.depnav.model.MarkerText

/**
 * DAO for the table containing the available [MarkerText] entries.
 */
@Dao
interface MarkerTextDao {
    /**
     * Inserts the provided [MarkerText] entries into the database.
     */
    @Insert
    suspend fun insertAll(vararg markerTexts: MarkerText)

    /**
     * Returns [MarkerText] entries containing the specified tokens as a substring on the specified
     * language.
     */
    @Query(
        "SELECT *, lid FROM marker_texts WHERE marker_texts MATCH :tokens AND lid = :language"
    )
    suspend fun loadByTokens(tokens: String, language: MarkerText.LanguageId): List<MarkerText>
}
