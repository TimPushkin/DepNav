package ru.spbu.depnav.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.R
import ru.spbu.depnav.ui.theme.DepNavTheme

/**
 * Lines with text information about a marker.
 */
@Composable
fun MarkerInfoLines(title: String, description: String?, isClosed: Boolean) {
    Row(
        modifier = Modifier.padding(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.h6
            )
        }

        if (isClosed) {
            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "•",
                modifier = Modifier.alpha(0.6f),
                fontSize = MaterialTheme.typography.h6.fontSize
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = stringResource(R.string.closed),
                modifier = Modifier.alpha(0.6f),
                fontSize = MaterialTheme.typography.h6.fontSize
            )
        }
    }

    if (description != null && description.isNotBlank()) {
        Text(
            text = description,
            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
}

@Preview
@Composable
private fun MarkerInfoPreview() {
    DepNavTheme {
        Column {
            MarkerInfoLines(
                title = "Some title",
                isClosed = true,
                description = "Some description"
            )
        }
    }
}
