package ru.spbu.depnav.ui.map

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun ColumnScope.MarkerInfo(
    title: String,
    description: String? = null
) {
    if (title.isNotBlank()) Text(text = title)
    if (description != null && description.isNotBlank()) Text(text = description)
}
