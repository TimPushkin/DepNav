package ru.spbu.depnav.ui.map

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.R
import ru.spbu.depnav.utils.PreferencesManager

@Composable
fun SettingsDialog(prefs: PreferencesManager, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        buttons = {
            Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp)) {
                RadioOption(
                    title = stringResource(R.string.theme),
                    options = listOf(
                        R.string.light_theme,
                        R.string.dark_theme,
                        R.string.system_theme
                    )
                        .map { StringWithId(stringResource(it), it) },
                    selected = prefs.themeMode.titleId.let { StringWithId(stringResource(it), it) },
                    onSelected = { (_, id) ->
                        val selectedMode = PreferencesManager.ThemeMode.fromTitleId(id)
                        checkNotNull(selectedMode) { "Unknown theme mode selected (id $id)" }
                        prefs.themeMode = selectedMode
                    }
                )
            }
        },
        title = { Text(stringResource(R.string.settings), style = MaterialTheme.typography.h6) },
        shape = MaterialTheme.shapes.large
    )
}

private data class StringWithId(val string: String, @StringRes val id: Int)

@Composable
private fun RadioOption(
    title: String,
    options: List<StringWithId>,
    selected: StringWithId,
    onSelected: (StringWithId) -> Unit
) {
    Column(modifier = Modifier.padding(10.dp)) {
        Text(
            text = title,
            modifier = Modifier.padding(bottom = 10.dp),
            style = MaterialTheme.typography.caption,
        )

        Column(modifier = Modifier.selectableGroup()) {
            for (option in options) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = option == selected,
                            onClick = { onSelected(option) },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = option == selected,
                        onClick = null
                    )

                    Text(
                        text = option.string,
                        modifier = Modifier.padding(start = 10.dp),
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }
}
