package ru.spbu.depnav.ui.elements

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction

@Composable
fun SearchField(onSearch: (String) -> Unit) {
    var value by remember { mutableStateOf("") }

    TextField(
        value = value,
        onValueChange = { value = it },
        placeholder = { Text("Search...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch(value) }
        ),
        singleLine = true
    )
}
