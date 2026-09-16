package com.kaaneneskpc.napsam.feature.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamBackLink
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme

/**
 * Gizlilik ve veriler (karar K18, spesifikasyon B17): web gizlilik sayfasının uygulamaya uyarlanmış hâli
 * ve KVKK veri sorumlusu bilgisi. Ayarlar ekranı yoktur; aydınlatma metnine alt bilgideki bağlantıdan gelinir.
 */
@Composable
fun PrivacyRoute(onBack: () -> Unit, modifier: Modifier = Modifier, contentPadding: PaddingValues = PaddingValues()) {
    PrivacyScreen(onBack = onBack, modifier = modifier, contentPadding = contentPadding)
}

@Composable
fun PrivacyScreen(onBack: () -> Unit, modifier: Modifier = Modifier, contentPadding: PaddingValues = PaddingValues()) {
    val spacing = NapsamTheme.spacing
    val colors = NapsamTheme.colors
    val typography = NapsamTheme.typography
    val layoutDirection = LocalLayoutDirection.current

    // Zemin uygulama kabuğunda (NapsamApp) bir kez çizilir; ekranlar üzerine yerleşir.
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection) + spacing.screenHorizontal,
                end = contentPadding.calculateEndPadding(layoutDirection) + spacing.screenHorizontal,
                top = contentPadding.calculateTopPadding() + spacing.lg,
                bottom = contentPadding.calculateBottomPadding() + spacing.section,
            ),
        ) {
            item(key = "header") {
                Column(modifier = Modifier.padding(bottom = spacing.xl)) {
                    NapsamBackLink(text = stringResource(R.string.privacy_back), onClick = onBack)
                    Text(
                        text = stringResource(R.string.privacy_title),
                        style = typography.cardTitle,
                        color = colors.textPrimary,
                        modifier = Modifier
                            .padding(top = spacing.lg, bottom = spacing.md)
                            .semantics { heading() },
                    )
                    Text(
                        text = stringResource(R.string.privacy_lead),
                        style = typography.heroSubtitle,
                        color = colors.textSecondary,
                    )
                }
            }
            item(key = "not-collected") {
                Section(title = stringResource(R.string.privacy_not_collected_title)) {
                    Bullet(stringResource(R.string.privacy_not_collected_1))
                    Bullet(stringResource(R.string.privacy_not_collected_2))
                    Bullet(stringResource(R.string.privacy_not_collected_3))
                }
            }
            item(key = "stored") {
                Section(title = stringResource(R.string.privacy_stored_title)) {
                    Bullet(stringResource(R.string.privacy_stored_1))
                    Bullet(stringResource(R.string.privacy_stored_2))
                    Paragraph(stringResource(R.string.privacy_stored_note))
                }
            }
            item(key = "network") {
                Section(title = stringResource(R.string.privacy_network_title)) {
                    Paragraph(stringResource(R.string.privacy_network_body))
                }
            }
            item(key = "transfer") {
                Section(title = stringResource(R.string.privacy_transfer_title)) {
                    Paragraph(stringResource(R.string.privacy_transfer_body))
                }
            }
            item(key = "delete") {
                Section(title = stringResource(R.string.privacy_delete_title)) {
                    Paragraph(stringResource(R.string.privacy_delete_body))
                }
            }
            item(key = "rights") {
                Section(title = stringResource(R.string.privacy_rights_title)) {
                    Paragraph(stringResource(R.string.privacy_rights_body))
                }
            }
            item(key = "controller") {
                Section(title = stringResource(R.string.privacy_controller_title)) {
                    Paragraph(stringResource(R.string.privacy_controller_body))
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val spacing = NapsamTheme.spacing
    Column(modifier = modifier.padding(bottom = spacing.xl), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        Text(
            text = title,
            style = NapsamTheme.typography.valueTitle,
            color = NapsamTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        content()
    }
}

@Composable
private fun Paragraph(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = NapsamTheme.typography.valueText,
        color = NapsamTheme.colors.textSecondary,
        modifier = modifier,
    )
}

@Composable
private fun Bullet(text: String, modifier: Modifier = Modifier) {
    val spacing = NapsamTheme.spacing
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
        Text(text = "•", style = NapsamTheme.typography.valueText, color = NapsamTheme.colors.accent)
        Paragraph(text = text)
    }
}
