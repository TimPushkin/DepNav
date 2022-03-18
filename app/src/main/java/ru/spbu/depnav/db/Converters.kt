package ru.spbu.depnav.db

import androidx.room.TypeConverter
import ru.spbu.depnav.model.MarkerText

class Converters {
    @TypeConverter
    fun longToLanguageId(value: Long) =
        MarkerText.LanguageId.values().getOrElse(value.toInt()) { MarkerText.LanguageId.EN }

    @TypeConverter
    fun languageIdToLong(languageId: MarkerText.LanguageId) = languageId.ordinal.toLong()
}
