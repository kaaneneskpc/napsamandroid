package com.kaaneneskpc.napsam.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kaaneneskpc.napsam.core.data.content.ContentMetaDto
import com.kaaneneskpc.napsam.core.data.content.ContentSource
import com.kaaneneskpc.napsam.core.data.database.NapsamDatabase
import com.kaaneneskpc.napsam.core.data.datastore.AppSettings
import com.kaaneneskpc.napsam.core.data.datastore.AppSettingsSerializer
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.File
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope

/** `contract/` klasöründen bir dosya (yol Gradle'dan `napsam.contract.dir` ile gelir). */
internal fun contractText(path: String): String {
    val root = requireNotNull(System.getProperty("napsam.contract.dir")) { "Testleri Gradle ile çalıştır." }
    return File(root, path).readText()
}

internal val embeddedMeta: ContentMetaDto by lazy {
    napsamJson.decodeFromString<ContentMetaDto>(contractText("content/meta.json"))
}

/** Asset yerine `contract/` dosyalarını (veya testin değiştirdiği kopyasını) veren içerik kaynağı. */
internal class FakeContentSource(
    private val suggestions: String = contractText("content/suggestions.json"),
    private val meta: String = contractText("content/meta.json"),
) : ContentSource {
    var suggestionReads: Int = 0
        private set

    override suspend fun readSuggestions(): String {
        suggestionReads++
        return suggestions
    }

    override suspend fun readMeta(): String = meta
}

internal class MutableClock(var instant: Instant, private val zone: ZoneId = ZoneOffset.UTC) : Clock() {
    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = MutableClock(instant, zone)

    override fun instant(): Instant = instant

    fun advance(duration: Duration) {
        instant = instant.plus(duration)
    }
}

internal fun inMemoryDatabase(): NapsamDatabase = Room
    .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), NapsamDatabase::class.java)
    .build()

/** DataStore aynı dosyaya tek örnek ister: her depo kendi klasörünü alır. */
internal fun testDataStore(directory: File, scope: CoroutineScope): DataStore<AppSettings> = DataStoreFactory.create(
    serializer = AppSettingsSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { AppSettings() },
    scope = scope,
    produceFile = { File(directory, AppSettingsSerializer.FILE_NAME) },
)

internal fun MockRequestHandleScope.respondJson(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData = respond(
    content = body,
    status = status,
    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
)
