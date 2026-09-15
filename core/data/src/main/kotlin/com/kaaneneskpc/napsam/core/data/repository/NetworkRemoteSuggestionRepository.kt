package com.kaaneneskpc.napsam.core.data.repository

import com.kaaneneskpc.napsam.core.data.datastore.SettingsDataSource
import com.kaaneneskpc.napsam.core.data.network.NapsamApi
import com.kaaneneskpc.napsam.core.data.network.dto.toResultOrNull
import com.kaaneneskpc.napsam.core.data.network.dto.toSuggestRequest
import com.kaaneneskpc.napsam.core.domain.DataError
import com.kaaneneskpc.napsam.core.domain.Outcome
import com.kaaneneskpc.napsam.core.domain.repository.RemoteSuggestionRepository
import com.kaaneneskpc.napsam.core.domain.repository.RemoteSuggestionResult
import com.kaaneneskpc.napsam.core.model.SuggestionContext
import javax.inject.Inject
import kotlinx.coroutines.withTimeoutOrNull

/**
 * "Yaz" modunda AI önerisi (spesifikasyon C3.4, B8). POST'ta yeniden deneme yok; 5 saniyede yanıt
 * gelmezse zaman aşımı döner ve arayüz sessizce yerel motora düşer. Metin hiçbir yere loglanmaz.
 */
internal class NetworkRemoteSuggestionRepository @Inject constructor(
    private val api: NapsamApi,
    private val settings: SettingsDataSource,
) : RemoteSuggestionRepository {

    override suspend fun suggest(context: SuggestionContext, text: String): Outcome<RemoteSuggestionResult> {
        val installId = settings.installId()
        val response = withTimeoutOrNull(AI_TIMEOUT_MS) {
            api.suggest(installId, context.toSuggestRequest(text))
        } ?: return Outcome.Failure(DataError.TIMEOUT)

        return when (response) {
            is Outcome.Success -> response.value.toResultOrNull()
                ?.let { Outcome.Success(it) }
                ?: Outcome.Failure(DataError.INVALID_RESPONSE)

            is Outcome.Failure -> response
        }
    }

    private companion object {
        const val AI_TIMEOUT_MS = 5_000L
    }
}
