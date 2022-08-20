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

package ru.spbu.depnav.utils.preferences

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.spbu.depnav.R
import ru.spbu.depnav.data.model.MapInfo
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFERENCES_FILE_NAME = "preferences"

private const val THEME_MODE_KEY = "theme_mode"
private val THEME_MODE_DEFAULT = PreferencesManager.ThemeMode.SYSTEM.name

private const val MAP_STORED_NAME_KEY = "map"
private val MAP_STORED_NAME_DEFAULT = PreferencesManager.MapStoredName.SPBU_MM.name

/** Helper class to load ans save user settings. */
@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    private var _themeMode by mutableStateOf(
        prefs.getString(THEME_MODE_KEY, THEME_MODE_DEFAULT) ?: THEME_MODE_DEFAULT
    )

    /** Defines what theme the app uses. */
    var themeMode: ThemeMode
        get() = ThemeMode.valueOf(_themeMode)
        set(value) {
            prefs.edit { putString(THEME_MODE_KEY, value.name) }
            _themeMode = value.name
        }

    /** Possible app theme mode. */
    enum class ThemeMode(
        /** ID of the string resource corresponding to the user-visible title of the mode. */
        @StringRes val titleId: Int
    ) {
        LIGHT(R.string.light_theme),
        DARK(R.string.dark_theme),
        SYSTEM(R.string.system_theme);

        companion object {
            /** Returns [ThemeMode] having the title referenced by this string resource ID. */
            fun fromTitleId(@StringRes id: Int) = when (id) {
                LIGHT.titleId -> LIGHT
                DARK.titleId -> DARK
                SYSTEM.titleId -> SYSTEM
                else -> null
            }
        }
    }

    private var _mapStoredName by mutableStateOf(
        prefs.getString(MAP_STORED_NAME_KEY, MAP_STORED_NAME_DEFAULT) ?: MAP_STORED_NAME_DEFAULT
    )

    /** Defines what map the app shows. */
    var mapStoredName: MapStoredName
        get() = MapStoredName.valueOf(_mapStoredName)
        set(value) {
            prefs.edit { putString(MAP_STORED_NAME_KEY, value.name) }
            _mapStoredName = value.name
        }

    /** Maps available in the app. */
    enum class MapStoredName(
        /** Map's name as it is stored in the corresponding [MapInfo] in the database. */
        val storedName: String
    ) {
        SPBU_MM("spbu-mm");

        /** Subdirectory with map's tiles in assets. */
        val tilesSubdir = "$storedName/tiles"
    }
}
