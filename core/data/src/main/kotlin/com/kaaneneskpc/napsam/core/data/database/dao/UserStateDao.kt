package com.kaaneneskpc.napsam.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.kaaneneskpc.napsam.core.data.database.entity.SuggestionEntity
import com.kaaneneskpc.napsam.core.data.database.entity.UserStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class UserStateDao {
    @Query("SELECT * FROM user_state")
    abstract suspend fun all(): List<UserStateEntity>

    @Query("SELECT * FROM user_state WHERE slug = :slug")
    abstract suspend fun find(slug: String): UserStateEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM suggestion WHERE slug = :slug)")
    abstract suspend fun suggestionExists(slug: String): Boolean

    @Upsert
    abstract suspend fun upsert(state: UserStateEntity)

    /** Kaydedilenler, web gibi en son kaydedilen önce (`-saved_at`, `-updated_at`). */
    @Query(
        """
        SELECT s.* FROM suggestion AS s
        INNER JOIN user_state AS u ON u.slug = s.slug
        WHERE u.status = 'saved'
        ORDER BY u.saved_at DESC, u.updated_at DESC
        """,
    )
    abstract fun observeSaved(): Flow<List<SuggestionEntity>>

    /**
     * Oku-değiştir-yaz tek transaction'da. Web gibi içerikte olmayan bir öneri için kayıt açılmaz;
     * [transform] null dönerse hiçbir şey yazılmaz.
     */
    @Transaction
    open suspend fun modify(slug: String, transform: (UserStateEntity?) -> UserStateEntity?) {
        if (!suggestionExists(slug)) return
        transform(find(slug))?.let { upsert(it) }
    }
}
