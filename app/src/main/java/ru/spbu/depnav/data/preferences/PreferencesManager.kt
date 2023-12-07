/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofei Pushkin
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

package ru.spbu.depnav.data.preferences

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.spbu.depnav.R
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFERENCES_FILE_NAME = "preferences"

private const val THEME_MODE_KEY = "theme_mode"
private val THEME_MODE_DEFAULT = ThemeMode.SYSTEM.name

private const val ENABLE_ROTATION_KEY = "rotation"
private const val ENABLE_ROTATION_DEFAULT = false

private const val SELECTED_MAP_KEY = "map"

/** Helper class to load ans save user settings. */
@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    private var themeMode: ThemeMode
        get() = ThemeMode.valueOf(checkNotNull(prefs.getString(THEME_MODE_KEY, THEME_MODE_DEFAULT)))
        set(value) = prefs.edit { putString(THEME_MODE_KEY, value.name) }
    private val _themeModeFlow = MutableStateFlow(themeMode)

    /** Defines what theme the app uses. */
    val themeModeFlow = _themeModeFlow.asStateFlow()

    /** Updates [themeModeFlow]. */
    fun updateThemeMode(value: ThemeMode) {
        themeMode = value
        _themeModeFlow.value = themeMode
    }

    private var enableRotation: Boolean
        get() = prefs.getBoolean(ENABLE_ROTATION_KEY, ENABLE_ROTATION_DEFAULT)
        set(value) = prefs.edit { putBoolean(ENABLE_ROTATION_KEY, value) }
    private val _enableRotationFlow = MutableStateFlow(enableRotation)

    /** Whether map rotation is enabled. */
    val enableRotationFlow = _enableRotationFlow.asStateFlow()

    /** Updates [enableRotationFlow]. */
    fun updateEnableRotation(value: Boolean) {
        enableRotation = value
        _enableRotationFlow.value = enableRotation
    }

    private var selectedMapId: Int?
        get() = prefs.getInt(SELECTED_MAP_KEY, -1).takeIf { it >= 0 }
        set(value) {
            requireNotNull(value) { "Selected map cannot be set to null, i.e. unselected" }
            prefs.edit { putInt(SELECTED_MAP_KEY, value) }
        }
    private val _selectedMapIdFlow = MutableStateFlow(selectedMapId)

    /** Defines what map the app shows. */
    val selectedMapIdFlow = _selectedMapIdFlow.asStateFlow()

    /** Updates [selectedMapIdFlow]. */
    fun updateSelectedMapId(value: Int) {
        selectedMapId = value
        _selectedMapIdFlow.value = selectedMapId
    }
}

/** Defines the theme of the app. */
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
