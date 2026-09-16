package com.kaaneneskpc.napsam

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamBackground
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamTopBar
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import com.kaaneneskpc.napsam.core.domain.repository.ThemeMode
import com.kaaneneskpc.napsam.feature.home.HomeRoute
import com.kaaneneskpc.napsam.feature.privacy.PrivacyRoute
import com.kaaneneskpc.napsam.feature.saved.SavedRoute
import kotlinx.serialization.Serializable

/** Rotalar (spesifikasyon B5): üç ekran, öneri kartı ana ekranın parçası (K4). */
@Serializable
data object HomeKey : NavKey

@Serializable
data object SavedKey : NavKey

@Serializable
data object PrivacyKey : NavKey

/** Web'deki tek odaklı akış genişliği (`--stage`): geniş ekranda içerik ortalanır ve bu genişliği aşmaz. */
private val ContentMaxWidth = 760.dp

@Composable
fun NapsamApp(modifier: Modifier = Modifier, viewModel: NapsamAppViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val darkTheme = when (state.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // Sistem çubuğu ikonları uygulamanın temasını izler (tema düğmesiyle sistem teması ayrışabilir).
    val activity = LocalActivity.current as? ComponentActivity
    DisposableEffect(activity, darkTheme) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
        )
        onDispose { }
    }

    NapsamTheme(darkTheme = darkTheme) {
        val backStack = rememberNavBackStack(HomeKey)
        val contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues()

        NapsamBackground(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                    ),
            ) {
                NapsamTopBar(
                    brand = stringResource(R.string.brand),
                    savedLabel = stringResource(R.string.top_bar_saved),
                    savedCount = state.savedCount,
                    themeToggleDescription = stringResource(
                        if (darkTheme) R.string.theme_switch_to_light else R.string.theme_switch_to_dark,
                    ),
                    onSavedClick = dropUnlessResumed { backStack.navigateTo(SavedKey) },
                    onThemeToggle = { viewModel.onThemeToggle(currentlyDark = darkTheme) },
                    modifier = Modifier.padding(horizontal = NapsamTheme.spacing.screenHorizontal),
                )
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = entryProvider {
                        entry<HomeKey> {
                            HomeRoute(
                                onOpenPrivacy = dropUnlessResumed { backStack.navigateTo(PrivacyKey) },
                                contentPadding = contentPadding,
                            )
                        }
                        entry<SavedKey> {
                            SavedRoute(
                                onBack = dropUnlessResumed { backStack.removeLastOrNull() },
                                contentPadding = contentPadding,
                            )
                        }
                        entry<PrivacyKey> {
                            PrivacyRoute(
                                onBack = dropUnlessResumed { backStack.removeLastOrNull() },
                                contentPadding = contentPadding,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/** Aynı ekran üst üste yığılmaz (üst bardaki Kaydedilenler'e tekrar basmak). */
private fun MutableList<NavKey>.navigateTo(key: NavKey) {
    if (lastOrNull() != key) add(key)
}
