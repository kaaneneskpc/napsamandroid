package com.kaaneneskpc.napsam.core.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.kaaneneskpc.napsam.core.data.napsamJson
import com.kaaneneskpc.napsam.core.data.network.dto.ConfigDto
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/**
 * Cihazdaki tercihler (karar K12: kotlinx.serialization ile tipli DataStore). Web gibi yalnızca tema
 * saklanır; bütçe hatırlanmaz. Kimlik üretilmez (AI kapalı, Faz 13); eski dosyadaki fazla alanlar yok sayılır.
 */
@Serializable
internal data class AppSettings(
    val themeMode: String = THEME_SYSTEM,
    val config: ConfigDto? = null,
    val configFetchedAtMillis: Long? = null,
    /** 429 sonrası bu ana kadar config istenmez. */
    val configRetryAfterMillis: Long? = null,
) {
    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }
}

internal object AppSettingsSerializer : Serializer<AppSettings> {
    const val FILE_NAME = "napsam_settings.json"

    override val defaultValue: AppSettings = AppSettings()

    override suspend fun readFrom(input: InputStream): AppSettings = try {
        napsamJson.decodeFromString<AppSettings>(input.readBytes().decodeToString())
    } catch (e: SerializationException) {
        throw CorruptionException("Ayar dosyası okunamadı", e)
    } catch (e: IllegalArgumentException) {
        throw CorruptionException("Ayar dosyası okunamadı", e)
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        output.write(napsamJson.encodeToString(t).encodeToByteArray())
    }
}

@Singleton
internal class SettingsDataSource @Inject constructor(private val dataStore: DataStore<AppSettings>) {
    val settings: Flow<AppSettings> = dataStore.data

    suspend fun current(): AppSettings = dataStore.data.first()

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.updateData(transform)
    }
}
