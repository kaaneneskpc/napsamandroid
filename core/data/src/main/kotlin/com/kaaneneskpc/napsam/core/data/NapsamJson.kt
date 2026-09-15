package com.kaaneneskpc.napsam.core.data

import kotlinx.serialization.json.Json

/**
 * İçerik, ağ ve ayar dosyası için ortak JSON yapılandırması (spesifikasyon B8): bilinmeyen alanlar
 * yok sayılır (sunucu alan ekleyebilir), null alanlar yazılmaz.
 */
internal val napsamJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
