package com.kaaneneskpc.napsam.core.designsystem.component

import androidx.compose.ui.text.intl.LocaleList

/**
 * Uygulama içeriği web gibi Türkçedir: büyük harfe çevirme cihaz dilinden bağımsız olarak Türkçe kuralla
 * yapılır ("Canlı öneri" → "CANLI ÖNERİ"; İngilizce kuralla "ÖNERI" olurdu).
 */
internal val ContentLocale: LocaleList = LocaleList("tr-TR")
