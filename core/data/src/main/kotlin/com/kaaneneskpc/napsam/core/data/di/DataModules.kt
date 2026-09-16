package com.kaaneneskpc.napsam.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.room.Room
import com.kaaneneskpc.napsam.core.data.content.AssetContentSource
import com.kaaneneskpc.napsam.core.data.content.ContentImporter
import com.kaaneneskpc.napsam.core.data.content.ContentSource
import com.kaaneneskpc.napsam.core.data.content.ContentSynchronizer
import com.kaaneneskpc.napsam.core.data.database.NapsamDatabase
import com.kaaneneskpc.napsam.core.data.database.dao.ContentDao
import com.kaaneneskpc.napsam.core.data.database.dao.UserStateDao
import com.kaaneneskpc.napsam.core.data.datastore.AppSettings
import com.kaaneneskpc.napsam.core.data.datastore.AppSettingsSerializer
import com.kaaneneskpc.napsam.core.data.napsamJson
import com.kaaneneskpc.napsam.core.data.network.NapsamApi
import com.kaaneneskpc.napsam.core.data.repository.DataStorePreferencesRepository
import com.kaaneneskpc.napsam.core.data.repository.NetworkConfigRepository
import com.kaaneneskpc.napsam.core.data.repository.OfflineSuggestionRepository
import com.kaaneneskpc.napsam.core.data.repository.OfflineUserStateRepository
import com.kaaneneskpc.napsam.core.domain.engine.SuggestionEngine
import com.kaaneneskpc.napsam.core.domain.repository.ConfigRepository
import com.kaaneneskpc.napsam.core.domain.repository.PreferencesRepository
import com.kaaneneskpc.napsam.core.domain.repository.SuggestionRepository
import com.kaaneneskpc.napsam.core.domain.repository.UserStateRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import java.time.Clock
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
internal object CoroutinesModule {
    @Provides
    @IoDispatcher
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    @ApplicationScope
    fun applicationScope(@DefaultDispatcher dispatcher: CoroutineDispatcher): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatcher)
}

@Module
@InstallIn(SingletonComponent::class)
internal object StorageModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): NapsamDatabase =
        Room.databaseBuilder(context, NapsamDatabase::class.java, NapsamDatabase.NAME).build()

    @Provides
    fun contentDao(database: NapsamDatabase): ContentDao = database.contentDao()

    @Provides
    fun userStateDao(database: NapsamDatabase): UserStateDao = database.userStateDao()

    @Provides
    @Singleton
    fun settingsDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): DataStore<AppSettings> = DataStoreFactory.create(
        serializer = AppSettingsSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { AppSettings() },
        scope = CoroutineScope(scope.coroutineContext + ioDispatcher),
        produceFile = { context.dataStoreFile(AppSettingsSerializer.FILE_NAME) },
    )
}

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Provides
    fun json(): Json = napsamJson

    @Provides
    @Singleton
    fun httpClient(json: Json): HttpClient = NapsamApi.createClient(Android.create(), json)
}

/** Motorun rastgelelik ve saat kaynağı enjekte edilir (spesifikasyon B9); testler sabitini verir. */
@Module
@InstallIn(SingletonComponent::class)
internal object EngineModule {
    @Provides
    fun clock(): Clock = Clock.systemDefaultZone()

    @Provides
    fun random(): Random = Random.Default

    @Provides
    fun suggestionEngine(random: Random, clock: Clock): SuggestionEngine = SuggestionEngine(random, clock.zone)
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class BindingsModule {
    @Binds
    abstract fun contentSource(source: AssetContentSource): ContentSource

    @Binds
    abstract fun contentSynchronizer(importer: ContentImporter): ContentSynchronizer

    @Binds
    abstract fun suggestionRepository(repository: OfflineSuggestionRepository): SuggestionRepository

    @Binds
    abstract fun userStateRepository(repository: OfflineUserStateRepository): UserStateRepository

    @Binds
    abstract fun configRepository(repository: NetworkConfigRepository): ConfigRepository

    @Binds
    abstract fun preferencesRepository(repository: DataStorePreferencesRepository): PreferencesRepository
}
