package ru.spbu.depnav.ui.elements.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SearchButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        text = { Text(text.ifBlank { "Search..." }) },
        onClick = onClick,
        icon = { Icons.Default.Search },
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .then(modifier)
    )
}
