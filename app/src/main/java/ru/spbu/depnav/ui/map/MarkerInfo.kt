package ru.spbu.depnav.ui.map

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MarkerInfo(
    title: String,
    description: String? = null
) {
    if (title.isNotBlank()) {
        Text(
            text = title,
            modifier = Modifier.padding(10.dp),
            style = MaterialTheme.typography.h6
        )
    }
    if (description != null && description.isNotBlank()) {
        Text(
            text = description,
            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
        )
    }
}
