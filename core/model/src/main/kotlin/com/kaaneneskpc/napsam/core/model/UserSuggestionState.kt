package com.kaaneneskpc.napsam.core.model

import java.time.Instant

/**
 * Kullanıcının bir öneriyle ilişkisi. v1'de yalnızca cihazda tutulur (spesifikasyon B7).
 */
public data class UserSuggestionState(
    public val slug: String,
    public val status: StateStatus,
    /** Geçilen öneri bu ana kadar tekrar gösterilmez. */
    public val dismissedUntil: Instant?,
    public val savedAt: Instant?,
    public val doneAt: Instant?,
    public val updatedAt: Instant,
) {
    public fun isCoolingDown(now: Instant): Boolean = dismissedUntil?.isAfter(now) == true
}
