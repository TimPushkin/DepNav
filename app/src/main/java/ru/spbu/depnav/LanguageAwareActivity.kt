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

package ru.spbu.depnav

import androidx.activity.ComponentActivity
import androidx.compose.ui.text.intl.Locale
import ru.spbu.depnav.model.MarkerText

/**
 * Activity which can convert the language currently set in the application to
 * [MarkerText.LanguageId].
 */
abstract class LanguageAwareActivity : ComponentActivity() {
    protected val systemLanguage: MarkerText.LanguageId
        get() = when (Locale.current.language) {
            "en" -> MarkerText.LanguageId.EN
            "ru" -> MarkerText.LanguageId.RU
            else -> MarkerText.LanguageId.EN
        }
}
