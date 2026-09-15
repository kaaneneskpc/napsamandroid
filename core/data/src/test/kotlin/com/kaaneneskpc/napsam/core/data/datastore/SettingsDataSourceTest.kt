package com.kaaneneskpc.napsam.core.data.datastore

import com.kaaneneskpc.napsam.core.data.repository.DataStorePreferencesRepository
import com.kaaneneskpc.napsam.core.data.testDataStore
import com.kaaneneskpc.napsam.core.domain.repository.ThemeMode
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Tipli DataStore (karar K12) ve kurulum kimliği (spesifikasyon C3.2). */
class SettingsDataSourceTest {
    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `kurulum kimligi rastgele uuid v4, bir kez uretilir`() = runTest {
        val settings = SettingsDataSource(testDataStore(folder.root, backgroundScope))

        val first = settings.installId()
        assertEquals(first, settings.installId())
        assertEquals(4, UUID.fromString(first).version())
    }

    @Test
    fun `tema tercihi saklanir, varsayilan sistem`() = runTest {
        val preferences =
            DataStorePreferencesRepository(SettingsDataSource(testDataStore(folder.root, backgroundScope)))

        assertEquals(ThemeMode.SYSTEM, preferences.observeThemeMode().first())
        preferences.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, preferences.observeThemeMode().first())
        preferences.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, preferences.observeThemeMode().first())
    }

    @Test
    fun `bozuk ayar dosyasi varsayilana doner`() = runTest {
        File(folder.root, AppSettingsSerializer.FILE_NAME).writeText("{bozuk")
        val settings = SettingsDataSource(testDataStore(folder.root, backgroundScope))

        assertEquals(AppSettings(), settings.current())
    }
}
