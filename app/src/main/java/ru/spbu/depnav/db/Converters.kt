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
