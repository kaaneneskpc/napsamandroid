package com.kaaneneskpc.napsam.core.domain.repository

import com.kaaneneskpc.napsam.core.domain.EngineConstants
import com.kaaneneskpc.napsam.core.domain.Outcome
import com.kaaneneskpc.napsam.core.model.AppConfig
import com.kaaneneskpc.napsam.core.model.Suggestion
import com.kaaneneskpc.napsam.core.model.SuggestionContext
import com.kaaneneskpc.napsam.core.model.UserSuggestionState
import kotlinx.coroutines.flow.Flow

/*
 * Repository arayüzleri (spesifikasyon B2, karar K10): arayüzler burada, uygulamaları `:core:data`'da.
 * Feature modülleri yalnızca bu arayüzleri görür.
 */

/** Uygulamaya gömülü öneri içeriği (`contract/content`). */
public interface SuggestionRepository {
    /** İçeriği ilk açılışta veya içerik sürümü değiştiğinde tek transaction'da içe aktarır (karar K3). */
    public suspend fun ensureContentImported()

    /** Motorun havuzu. */
    public suspend fun suggestions(): List<Suggestion>
}

/** Kullanıcının önerilerle ilişkisi; v1'de yalnızca cihazda (web ile senkron yok). */
public interface UserStateRepository {
    public suspend fun states(): List<UserSuggestionState>

    /** Kaydedilenler listesi; üst bardaki sayı da buradan türer. */
    public fun observeSaved(): Flow<List<Suggestion>>

    public suspend fun markShown(slug: String)

    /** "Başka fikir": [EngineConstants.DISMISS_COOLDOWN_DAYS] gün boyunca tekrar gösterilmez. */
    public suspend fun dismiss(slug: String)

    public suspend fun save(slug: String)

    /** Kayıttan çıkarır; web gibi kayıt "gösterildi" durumuna döner. */
    public suspend fun unsave(slug: String)

    /** "Tamam, yapıyorum". */
    public suspend fun markDone(slug: String)
}

/** Uzaktan yapılandırma (`GET /api/v1/config/`). */
public interface ConfigRepository {
    /** Önce gömülü varsayılan (`contract/content/meta.json`), sunucu yanıtı gelince güncellenir. */
    public fun observeConfig(): Flow<AppConfig>

    /**
     * Önbellek 12 saatten eskiyse ve hız sınırı ertelemesi bitmişse sunucudan çeker (spesifikasyon B7);
     * gerek yoksa istek atmadan başarı döner. Hata durumunda önbellek ve varsayılan korunur.
     */
    public suspend fun refresh(): Outcome<Unit>
}

/** `POST /api/v1/suggest/`: yalnızca "Yaz" modunda ve config'te AI açıkken çağrılır. */
public interface RemoteSuggestionRepository {
    /** Yanıt 5 saniyede gelmezse [com.kaaneneskpc.napsam.core.domain.DataError.TIMEOUT]. */
    public suspend fun suggest(context: SuggestionContext, text: String): Outcome<RemoteSuggestionResult>
}

/** Web'deki gibi yalnızca tema tercihi saklanır; bütçe her açılışta varsayılana ("Bedava") döner. */
public interface PreferencesRepository {
    public fun observeThemeMode(): Flow<ThemeMode>

    public suspend fun setThemeMode(mode: ThemeMode)
}

public enum class ThemeMode {
    /** Elle seçim yapılmadı: sistem teması. */
    SYSTEM,
    LIGHT,
    DARK,
}
