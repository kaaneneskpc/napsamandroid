package com.kaaneneskpc.napsam.core.data.content

import android.content.Context
import com.kaaneneskpc.napsam.core.data.di.DefaultDispatcher
import com.kaaneneskpc.napsam.core.data.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** Uygulamayla gelen içerik dosyaları (`AndroidContentAssetsConventionPlugin` asset olarak paketler). */
internal interface ContentSource {
    suspend fun readSuggestions(): String

    suspend fun readMeta(): String
}

internal class AssetContentSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ContentSource {
    override suspend fun readSuggestions(): String = read("content/suggestions.json")

    override suspend fun readMeta(): String = read("content/meta.json")

    private suspend fun read(path: String): String = withContext(ioDispatcher) {
        context.assets.open(path).bufferedReader().use { it.readText() }
    }
}

/** Gömülü meta dosyası süreç boyunca bir kez okunur. */
@Singleton
internal class ContentMetaProvider @Inject constructor(
    private val source: ContentSource,
    private val json: Json,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    private val mutex = Mutex()

    @Volatile
    private var meta: ContentMetaDto? = null

    suspend fun get(): ContentMetaDto = meta ?: mutex.withLock {
        meta ?: withContext(defaultDispatcher) {
            json.decodeFromString<ContentMetaDto>(source.readMeta())
        }.also { meta = it }
    }
}
