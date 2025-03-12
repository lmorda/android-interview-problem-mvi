package com.lmorda.homework.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.lmorda.homework.R
import com.lmorda.homework.domain.model.GithubRepo
import com.lmorda.homework.ui.theme.Blue80
import com.lmorda.homework.ui.theme.Green80
import com.lmorda.homework.ui.theme.Orange80
import com.lmorda.homework.ui.theme.Pink80
import com.lmorda.homework.ui.theme.Yellow80
import com.lmorda.homework.ui.theme.sizeLarge
import com.lmorda.homework.ui.theme.sizeMedium

@Composable
internal fun RepositoryStats(details: GithubRepo) {
    Row(
        modifier = Modifier
            .padding(vertical = sizeMedium)
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(size = sizeLarge),
            imageVector = Icons.Default.Star,
            tint = Yellow80,
            contentDescription = "star",
        )
        Text(
            modifier = Modifier.padding(horizontal = sizeMedium),
            text = countPrettyString(details.stargazersCount),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Icon(
            modifier = Modifier.size(size = sizeLarge),
            imageVector = Icons.Default.PlayArrow,
            tint = getLanguageTintColor(language = details.language),
            contentDescription = "language",
        )
        Text(
            modifier = Modifier.padding(start = sizeMedium),
            text = details.language?.takeIf { it.isNotBlank() }
                ?: stringResource(R.string.informational_repo),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun getLanguageTintColor(language: String?): Color =
    language?.takeIf { it.isNotBlank() }
        ?.firstOrNull()
        ?.lowercaseChar()
        ?.let { char ->
            when (char) {
                in 'a'..'e' -> Pink80
                in 'f'..'i' -> Orange80
                in 'j'..'p' -> Green80
                in 'q'..'z' -> Blue80
                else -> Pink80
            }
        } ?: Pink80

private fun countPrettyString(value: Int?): String {
    if (value == null) return "0"
    return when {
        value >= 1_000_000 -> "${"%.1f".format(value / 1_000_000.0)}M"
        value >= 1_000 -> "${"%.1f".format(value / 1_000.0)}k"
        else -> value.toString()
    }
}
