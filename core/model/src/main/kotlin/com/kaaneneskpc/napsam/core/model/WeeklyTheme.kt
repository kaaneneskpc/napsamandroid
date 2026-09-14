package com.kaaneneskpc.napsam.core.model

import java.time.LocalDate

/**
 * Haftalık editoryal tema (spesifikasyon A6.6). Kullanıcı görmez; yalnızca sıralamayı
 * hafifçe ağırlıklandırır. Seçim kuralı `:core:domain` içindedir (spesifikasyon C4.7).
 */
public data class WeeklyTheme(public val key: String, public val startsOn: LocalDate, public val isActive: Boolean)
