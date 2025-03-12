package com.lmorda.homework.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.lmorda.homework.R

private val LightColorScheme = lightColorScheme(
    background = White,
    onBackground = Grey220,
    onSurface = BrandLight,
)

private val DarkColorScheme = darkColorScheme(
    background = BrandDark,
    onBackground = Grey20,
    onSurface = BrandLight,
)

@Composable
fun HomeworkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun topAppBarColors() = TopAppBarColors(
    containerColor = MaterialTheme.colorScheme.background,
    titleContentColor = MaterialTheme.colorScheme.background,
    actionIconContentColor = MaterialTheme.colorScheme.background,
    navigationIconContentColor = MaterialTheme.colorScheme.background,
    scrolledContainerColor = MaterialTheme.colorScheme.background,
)

val sizeSmall: Dp
    @Composable get() =
        dimensionResource(id = R.dimen.small)

val sizeMedium: Dp
    @Composable get() =
        dimensionResource(id = R.dimen.medium)

val sizeDefault: Dp
    @Composable get() =
        dimensionResource(id = R.dimen.standard)

val sizeLarge: Dp
    @Composable get() =
        dimensionResource(id = R.dimen.large)

val sizeXLarge: Dp
    @Composable get() =
        dimensionResource(id = R.dimen.xLarge)
