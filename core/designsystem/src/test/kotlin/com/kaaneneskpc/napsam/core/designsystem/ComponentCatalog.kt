package com.kaaneneskpc.napsam.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaaneneskpc.napsam.core.designsystem.component.BudgetOption
import com.kaaneneskpc.napsam.core.designsystem.component.BudgetSelector
import com.kaaneneskpc.napsam.core.designsystem.component.CardActions
import com.kaaneneskpc.napsam.core.designsystem.component.CardBand
import com.kaaneneskpc.napsam.core.designsystem.component.CommitMessage
import com.kaaneneskpc.napsam.core.designsystem.component.CtaNote
import com.kaaneneskpc.napsam.core.designsystem.component.FallbackBlock
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamBackground
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamBadge
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamCard
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamChip
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamCommitButton
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamConsole
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamDivider
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamFooter
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamIconToggleButton
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamPrimaryButton
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamSecondaryButton
import com.kaaneneskpc.napsam.core.designsystem.component.NapsamTopBar
import com.kaaneneskpc.napsam.core.designsystem.component.NeedsRow
import com.kaaneneskpc.napsam.core.designsystem.component.OverlinePill
import com.kaaneneskpc.napsam.core.designsystem.component.RefineToggle
import com.kaaneneskpc.napsam.core.designsystem.component.SectionLabel
import com.kaaneneskpc.napsam.core.designsystem.component.SkeletonBlock
import com.kaaneneskpc.napsam.core.designsystem.component.StageHeader
import com.kaaneneskpc.napsam.core.designsystem.component.StepList
import com.kaaneneskpc.napsam.core.designsystem.component.ValuePropCard
import com.kaaneneskpc.napsam.core.designsystem.icon.NapsamIcons
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import kotlinx.collections.immutable.persistentListOf

// Ekran görüntüsü testleri için örnek içerik: Stitch son tasarımındaki ana ekranın aynısı.
// Uygulama metinleri değildir (onlar strings.xml'de); tasarımla yan yana karşılaştırmak içindir.

private val budgetOptions = persistentListOf(
    BudgetOption("bedava", "Bedava", "0 TL"),
    BudgetOption("az", "Az", "~1-150 TL"),
    BudgetOption("orta", "Orta", "~150-500 TL"),
    BudgetOption("iyi", "İyi", "~500-1.500 TL"),
    BudgetOption("bol", "Bol", "1.500 TL+"),
)

/** Stitch mobil ana ekranı: üst bar → başlık → bütçe paneli → canlı öneri kartı → tanıtım → alt bilgi. */
@Composable
internal fun HomePageCatalog() {
    val spacing = NapsamTheme.spacing
    val typography = NapsamTheme.typography
    val colors = NapsamTheme.colors
    NapsamBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screenHorizontal)
                .padding(top = spacing.lg, bottom = spacing.section),
        ) {
            NapsamTopBar(
                brand = "napsam",
                savedLabel = "Kaydedilenler",
                savedCount = 0,
                themeToggleDescription = "Temayı değiştir",
                onSavedClick = {},
                onThemeToggle = {},
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.xxxl, bottom = spacing.xxxl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OverlinePill(text = "Karar verme motoru · Sıfır tereddüt")
                Text(
                    text = "“Ne yapsam ya?”",
                    style = typography.heroTitle,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = spacing.lg, bottom = spacing.md),
                )
                Text(
                    text = "Sonsuz listelere ve kararsızlığa son.\n10 saniyede somut, uygulanabilir tek bir eylem.",
                    style = typography.heroSubtitle,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            NapsamConsole {
                Text(text = "BÜTÇE DURUMU", style = typography.sectionLabel, color = colors.textTertiary)
                BudgetSelector(
                    options = budgetOptions,
                    selectedKey = "bedava",
                    onSelect = {},
                    modifier = Modifier.padding(top = spacing.ml),
                )
                NapsamPrimaryButton(text = "Napsam?", onClick = {}, modifier = Modifier.padding(top = spacing.lgx))
                CtaNote(
                    text = "Tek dokunuş · Tek kesin karar · Sonsuz kaydırma yok",
                    modifier = Modifier.padding(top = spacing.ml),
                )
                // Altta boşluk yok: daraltma düğmesinin 48dp dokunma alanı tasarımdaki pt-3 boşluğunu karşılıyor.
                NapsamDivider(modifier = Modifier.padding(top = spacing.lg))
                RefineToggle(
                    text = "İstersen daralt — kelime seç ya da yaz",
                    expanded = false,
                    stateDescription = "Kapalı",
                    onClick = {},
                )
            }
            StageHeader(
                accentText = "Canlı öneri",
                secondaryText = "Hemen şimdi başla",
                modifier = Modifier.padding(top = spacing.section, bottom = spacing.lg),
            )
            SuggestionCardSample()
            Column(
                modifier = Modifier.padding(top = spacing.section, bottom = spacing.section),
                verticalArrangement = Arrangement.spacedBy(spacing.ml),
            ) {
                ValuePropCard(
                    "01 / Kararsızlık yok",
                    "Tekil odak",
                    "Seçenekler çoğaldıkça felç olursun. NAPSAM tek ve net bir yön önerir.",
                )
                ValuePropCard(
                    "02 / Sıfır maliyet",
                    "Varsayılan: 0 TL",
                    "Zaman geçirmek parayla ölçülmez. Tüm öneriler önce bütçesiz kurgulanır.",
                )
                ValuePropCard(
                    "03 / Anında hareket",
                    "Somut adımlar",
                    "Soyut tavsiye değil, hemen şimdi yapabileceğin sıralı adımlar.",
                )
            }
            NapsamFooter(
                text = "Konum bağımsız, reklamsız, hemen harekete geçiren Türkiye boş zaman rehberi.",
                linkText = "Gizlilik",
                onLinkClick = {},
            )
        }
    }
}

@Composable
private fun SuggestionCardSample() {
    val spacing = NapsamTheme.spacing
    val typography = NapsamTheme.typography
    val colors = NapsamTheme.colors
    NapsamCard {
        CardBand(categoryLabel = "Ufak işler") {
            NapsamBadge(text = "₺0 Bedava")
            NapsamBadge(text = "25-50 dk", icon = NapsamIcons.Clock)
            NapsamBadge(text = "Evde", icon = NapsamIcons.Pin)
            NapsamBadge(text = "Tek başına", icon = NapsamIcons.Person)
        }
        Column(modifier = Modifier.padding(spacing.xl)) {
            Text(text = "Faturaları tek klasörde topla", style = typography.cardTitle, color = colors.textPrimary)
            Text(
                text = "Lazım olduğunda aramak için geç kalmış olursun.",
                style = typography.cardHook,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = spacing.sm, bottom = spacing.xxl),
            )
            SectionLabel(text = "Nasıl yapılır (3 adım)", modifier = Modifier.padding(bottom = spacing.md))
            StepList(
                steps = persistentListOf(
                    "Son bir yılın faturalarını fiziksel ve dijital olarak topla.",
                    "Tarihe göre sırala, tek bir klasöre koy.",
                    "Dijital olanları bulut yedeğine at.",
                ),
            )
            FallbackBlock(
                title = "Olmazsa alternatifi",
                text = "Faturaların dağınıksa: Sadece son üç ayı yap, gerisini haftaya bırak.",
                modifier = Modifier.padding(top = spacing.xl, bottom = spacing.xxl),
            )
            CardActions(
                primary = { NapsamCommitButton(text = "Tamam, yapıyorum", onClick = {}) },
                secondary = { NapsamSecondaryButton(text = "Başka fikir", icon = NapsamIcons.Refresh, onClick = {}) },
                toggle = {
                    NapsamIconToggleButton(
                        checked = false,
                        onCheckedChange = {},
                        icon = NapsamIcons.Heart,
                        checkedIcon = NapsamIcons.HeartFilled,
                        contentDescription = "Kaydet",
                    )
                },
            )
        }
    }
}

/** Tasarımda görünmeyen ama web'de olan durumlar: açık daraltma paneli, gerekenler, kayıtlı kalp, mesaj, iskelet. */
@Composable
internal fun StatesCatalog() {
    val spacing = NapsamTheme.spacing
    NapsamBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(spacing.xl),
        ) {
            NapsamTopBar(
                brand = "napsam",
                savedLabel = "Kaydedilenler",
                savedCount = 3,
                themeToggleDescription = "Temayı değiştir",
                onSavedClick = {},
                onThemeToggle = {},
            )
            NapsamConsole {
                RefineToggle(
                    text = "İstersen daralt — kelime seç ya da yaz",
                    expanded = true,
                    stateDescription = "Açık",
                    onClick = {},
                    summary = "2 kelime",
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    NapsamChip(text = "yürümek", selected = true, onSelectedChange = {})
                    NapsamChip(text = "ellerimi kullanayım", selected = false, onSelectedChange = {})
                    NapsamChip(text = "kalabalık olmasın", selected = true, onSelectedChange = {})
                    NapsamChip(text = "kısa sürsün", selected = false, onSelectedChange = {})
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                SectionLabel(text = "Gerekenler")
                NeedsRow(items = persistentListOf("rahat ayakkabı", "su", "not defteri"))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                NapsamIconToggleButton(
                    checked = true,
                    onCheckedChange = {},
                    icon = NapsamIcons.Heart,
                    checkedIcon = NapsamIcons.HeartFilled,
                    contentDescription = "Kayıttan çıkar",
                )
            }
            CommitMessage(
                title = "Harika seçim. Şimdi ekranı kapat.",
                body = "Bu uygulamada iyi bir oturum, kısa bitendir.",
                closeContentDescription = "Kapat",
                onClose = {},
            )
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.7f).height(24.dp))
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.9f).height(16.dp))
            }
        }
    }
}
