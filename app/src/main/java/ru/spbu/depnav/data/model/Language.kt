/**
 * DepNav -- department navigator.
 * Copyright (C) 2023  Timofei Pushkin
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

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase

/** Supported app language. */
enum class Language {
    EN, RU;

    /** IETF BCP47 compliant language tag. */
    fun tag() = name.lowercase()
}

/** Returns [Language] of this locale. */
fun Locale.toLanguage() = if (language == Language.RU.tag()) Language.RU else Language.EN

/**
 * Returns this string converted to lowercase using Unicode mapping rules of the specified language.
 */
fun String.lowercase(language: Language) = toLowerCase(Locale(language.tag()))
