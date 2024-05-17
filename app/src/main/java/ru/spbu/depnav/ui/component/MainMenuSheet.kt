/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofei Pushkin
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

package ru.spbu.depnav.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.R
import ru.spbu.depnav.ui.viewmodel.AvailableMap

// https://m3.material.io/components/navigation-drawer/specs#368147de-9661-4a28-9fc1-ce2f8c9eac40
private val ITEM_HEIGHT = 56.dp
private val ITEM_HORIZONTAL_PADDING = 28.dp
private val DIVIDER_VERTICAL_PADDING = 16.dp
private val ICON_SIZE = 24.dp

/** [ModalDrawerSheet] for the main menu. */
@Composable
fun MainMenuSheet(
    selectedMapId: Int?,
    availableMaps: Collection<AvailableMap>,
    onMapSelected: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    onMapLegendClick: () -> Unit
) {
    ModalDrawerSheet {
        AppTitle()

        MapItems(selectedMapId, availableMaps, onMapSelected)

        HorizontalDivider(
            modifier = Modifier.padding(
                horizontal = ITEM_HORIZONTAL_PADDING,
                vertical = DIVIDER_VERTICAL_PADDING
            )
        )

        MiscItem(
            icon = {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.label_open_settings)
                )
            },
            labelText = stringResource(R.string.settings),
            onClick = onSettingsClick
        )

        MiscItem(
            icon = {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = stringResource(R.string.label_open_map_legend)
                )
            },
            labelText = stringResource(R.string.map_legend),
            onClick = onMapLegendClick
        )
    }
}

@Composable
private fun AppTitle() {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .height(ITEM_HEIGHT)
            .padding(horizontal = ITEM_HORIZONTAL_PADDING)
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ColumnScope.MapItems(
    selectedMapId: Int?,
    availableMaps: Collection<AvailableMap>,
    onMapSelected: (Int) -> Unit
) {
    AnimatedVisibility(visible = availableMaps.isNotEmpty()) {
        Column {
            for ((mapId, internalMapName, mapTitle) in availableMaps) {
                NavigationDrawerItem(
                    icon = {
                        MapLogo(
                            internalMapName,
                            contentDescription = "${stringResource(R.string.label_open_map)} $mapTitle"
                        )
                    },
                    label = { Text(mapTitle) },
                    selected = mapId == selectedMapId,
                    onClick = { onMapSelected(mapId) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}

@Composable
private fun MapLogo(internalMapName: String, contentDescription: String) {
    val logoId = LocalContext.current.resources.getIdentifier(
        "logo_${internalMapName.replace('-', '_')}", "drawable", LocalContext.current.packageName
    )
    Icon(
        painterResource(logoId),
        contentDescription,
        modifier = Modifier.size(ICON_SIZE)
    )
}

@Composable
private fun MiscItem(icon: @Composable () -> Unit, labelText: String, onClick: () -> Unit) {
    NavigationDrawerItem(
        icon = icon,
        label = { Text(labelText) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
