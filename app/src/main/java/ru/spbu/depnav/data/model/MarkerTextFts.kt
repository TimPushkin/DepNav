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

package ru.spbu.depnav.data.model

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

/** FTS index table for some of [MarkerText] columns. */
@Fts4( // Using FTS4 built-in `languageid` column leads to crash when querying it on Huawei devices
    contentEntity = MarkerText::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61
)
@Entity(tableName = "marker_text_fts")
data class MarkerTextFts(
    /** Column for indexing [MarkerText.title]. */
    val title: String?,
    /** Column for indexing [MarkerText.description]. */
    val description: String?
)
