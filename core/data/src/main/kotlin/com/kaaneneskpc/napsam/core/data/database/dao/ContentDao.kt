package com.kaaneneskpc.napsam.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.kaaneneskpc.napsam.core.data.database.entity.ContentInfoEntity
import com.kaaneneskpc.napsam.core.data.database.entity.SuggestionEntity

@Dao
internal abstract class ContentDao {
    @Query("SELECT * FROM suggestion")
    abstract suspend fun suggestions(): List<SuggestionEntity>

    @Query("SELECT * FROM content_info WHERE id = ${ContentInfoEntity.SINGLE_ROW_ID}")
    abstract suspend fun info(): ContentInfoEntity?

    /**
     * İçeriği tek transaction'da değiştirir (spesifikasyon C5.4): ya yeni içerik tamamen yazılır ya da
     * eski içerik olduğu gibi kalır. Kullanıcı kayıtları korunur; artık olmayan önerilere ait olanlar silinir.
     */
    @Transaction
    open suspend fun replaceContent(suggestions: List<SuggestionEntity>, info: ContentInfoEntity) {
        deleteSuggestions()
        insertSuggestions(suggestions)
        upsertInfo(info)
        deleteOrphanStates()
    }

    @Query("DELETE FROM suggestion")
    abstract suspend fun deleteSuggestions()

    @Insert
    abstract suspend fun insertSuggestions(suggestions: List<SuggestionEntity>)

    @Upsert
    abstract suspend fun upsertInfo(info: ContentInfoEntity)

    @Query("DELETE FROM user_state WHERE slug NOT IN (SELECT slug FROM suggestion)")
    abstract suspend fun deleteOrphanStates()
}
