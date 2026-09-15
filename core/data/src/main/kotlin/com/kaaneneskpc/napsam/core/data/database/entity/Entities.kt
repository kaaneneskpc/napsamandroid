package com.kaaneneskpc.napsam.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** İçerikteki bir öneri (spesifikasyon C5.2). Enum alanları web anahtarlarıyla saklanır. */
@Entity(tableName = "suggestion")
internal data class SuggestionEntity(
    @PrimaryKey val slug: String,
    val title: String,
    val hook: String,
    val steps: List<String>,
    val fallback: String,
    val category: String,
    @ColumnInfo(name = "cost_min") val costMin: Int,
    @ColumnInfo(name = "cost_max") val costMax: Int,
    @ColumnInfo(name = "cost_note") val costNote: String,
    @ColumnInfo(name = "duration_min") val durationMin: Int,
    @ColumnInfo(name = "duration_max") val durationMax: Int,
    val companions: List<String>,
    val energy: String,
    val place: String,
    @ColumnInfo(name = "venue_type") val venueType: String,
    @ColumnInfo(name = "weather_need") val weatherNeed: String,
    val seasonality: List<Int>,
    @ColumnInfo(name = "cultural_tags") val culturalTags: List<String>,
    @ColumnInfo(name = "required_items") val requiredItems: List<String>,
    @ColumnInfo(name = "theme_tags") val themeTags: List<String>,
    val tags: List<String>,
    @ColumnInfo(name = "night_unsafe_solo") val nightUnsafeSolo: Boolean,
)

/** Veritabanındaki içeriğin sürümü (`contract/content/meta.json` contentVersion); tek satır. */
@Entity(tableName = "content_info")
internal data class ContentInfoEntity(@PrimaryKey val id: Int = SINGLE_ROW_ID, val version: String, val count: Int) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}

/**
 * Kullanıcının bir öneriyle ilişkisi (web `SuggestionState`). Zamanlar epoch milisaniyesidir.
 * İçerik tablosuna yabancı anahtar yok: içerik yenilenirken kayıtlar silinmesin diye; artık olmayan
 * önerilerin kayıtları içe aktarma sırasında elle temizlenir.
 */
@Entity(tableName = "user_state", indices = [Index(value = ["status"])])
internal data class UserStateEntity(
    @PrimaryKey val slug: String,
    val status: String,
    @ColumnInfo(name = "dismissed_until") val dismissedUntil: Long?,
    @ColumnInfo(name = "saved_at") val savedAt: Long?,
    @ColumnInfo(name = "done_at") val doneAt: Long?,
    @ColumnInfo(name = "shown_count") val shownCount: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
