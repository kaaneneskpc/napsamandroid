package com.kaaneneskpc.napsam.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.kaaneneskpc.napsam.core.data.database.dao.ContentDao
import com.kaaneneskpc.napsam.core.data.database.dao.UserStateDao
import com.kaaneneskpc.napsam.core.data.database.entity.ContentInfoEntity
import com.kaaneneskpc.napsam.core.data.database.entity.SuggestionEntity
import com.kaaneneskpc.napsam.core.data.database.entity.UserStateEntity
import kotlinx.serialization.json.Json

/**
 * Yerel veritabanı (spesifikasyon B7). İçerik tabloları sürüm değişince yenilenir; kullanıcı tablosu
 * içerikten ayrıdır ve içerik güncellemesinde silinmez. Şema `schemas/` altında repodadır; kullanıcı
 * verisinde destructive migration YASAK.
 */
@Database(
    entities = [SuggestionEntity::class, ContentInfoEntity::class, UserStateEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(ListConverters::class)
internal abstract class NapsamDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao

    abstract fun userStateDao(): UserStateDao

    companion object {
        const val NAME = "napsam.db"
    }
}

/** Liste alanları JSON dizisi olarak saklanır; içerik yalnızca bütün hâlinde okunur, bu alanlarda sorgu yok. */
internal class ListConverters {
    @TypeConverter
    fun stringsToJson(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun jsonToStrings(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun intsToJson(value: List<Int>): String = Json.encodeToString(value)

    @TypeConverter
    fun jsonToInts(value: String): List<Int> = Json.decodeFromString(value)
}
