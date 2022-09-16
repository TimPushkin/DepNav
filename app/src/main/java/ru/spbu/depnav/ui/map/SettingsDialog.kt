/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofey Pushkin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.spbu.depnav.ui.map

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.R
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING
import ru.spbu.depnav.utils.preferences.PreferencesManager

private val HORIZONTAL_PADDING = 15.dp
private val VERTICAL_PADDING = 5.dp

/** Dialog with app settings. */
@Composable
fun SettingsDialog(prefs: PreferencesManager, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        buttons = {
            LazyColumn(
                modifier = Modifier.padding(
                    horizontal = HORIZONTAL_PADDING,
                    vertical = VERTICAL_PADDING
                )
            ) {
                item { GroupTitle(stringResource(R.string.theme)) }

                item {
                    RadioOption(
                        options = listOf(
                            R.string.light_theme,
                            R.string.dark_theme,
                            R.string.system_theme
                        ).map { id -> StringWithId(stringResource(id), id) },
                        selected = prefs.themeMode.titleId.let { id ->
                            StringWithId(stringResource(id), id)
                        },
                        onSelected = { (_, id) ->
                            val selectedMode = PreferencesManager.ThemeMode.fromTitleId(id)
                            checkNotNull(selectedMode) { "Unknown theme mode selected (id $id)" }
                            prefs.themeMode = selectedMode
                        }
                    )
                }

                item { GroupTitle(stringResource(R.string.map)) }

                item {
                    SwitchOption(
                        title = stringResource(R.string.rotation_gesture),
                        checked = prefs.enableRotation,
                        onChecked = prefs::enableRotation::set
                    )
                }
            }
        },
        title = { Text(stringResource(R.string.settings), style = MaterialTheme.typography.h6) },
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun GroupTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(DEFAULT_PADDING),
        style = MaterialTheme.typography.caption
    )
}

@Composable
private fun SwitchOption(
    title: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(DEFAULT_PADDING)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(bottom = DEFAULT_PADDING)
        )

        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

private data class StringWithId(val string: String, @StringRes val id: Int)

@Composable
private fun RadioOption(
    options: List<StringWithId>,
    selected: StringWithId,
    onSelected: (StringWithId) -> Unit
) {
    Column(
        modifier = Modifier
            .selectableGroup()
            .padding(horizontal = DEFAULT_PADDING)
    ) {
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
                    modifier = Modifier.padding(start = DEFAULT_PADDING),
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}
