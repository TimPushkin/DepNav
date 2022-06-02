package ru.spbu.depnav.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction

/**
 * Text field with a search icon.
 */
@Composable
fun SearchField(
    modifier: Modifier = Modifier,
    placeholder: String = "",
    onTextChange: (String) -> Unit = {},
    onClear: () -> Unit = {}
) {
    var text by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    TextField(
        value = text,
        onValueChange = {
            text = it
            onTextChange(it)
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .then(modifier),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            AnimatedVisibility(visible = text.isNotEmpty()) {
                IconButton(
                    onClick = {
                        text = ""
                        onClear()
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear field"
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        shape = RectangleShape,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colors.background,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
