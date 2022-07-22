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

import androidx.room.TypeConverter
import ru.spbu.depnav.model.MarkerText

/**
 * Type converters for Room databases.
 */
class Converters {
    /**
     * Converts a long to a language IDs with the corresponding ordinal. If no such language ID
     * exists [MarkerText.LanguageId.EN] is returned.
     */
    @TypeConverter
    fun longToLanguageId(value: Long) =
        MarkerText.LanguageId.values().getOrElse(value.toInt()) { MarkerText.LanguageId.EN }

    /**
     * Converts a language ID to a long by taking its ordinal.
     */
    @TypeConverter
    fun languageIdToLong(languageId: MarkerText.LanguageId) = languageId.ordinal.toLong()
}
