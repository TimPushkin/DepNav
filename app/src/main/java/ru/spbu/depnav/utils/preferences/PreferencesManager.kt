package ru.spbu.depnav.utils.preferences

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.spbu.depnav.R
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFERENCES_FILE_NAME = "preferences"

private const val THEME_MODE_KEY = "theme_mode"
private val THEME_MODE_DEFAULT = PreferencesManager.ThemeMode.SYSTEM.name

@Singleton
/** Helper class to load ans save user settings. */
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
}