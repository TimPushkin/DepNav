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
