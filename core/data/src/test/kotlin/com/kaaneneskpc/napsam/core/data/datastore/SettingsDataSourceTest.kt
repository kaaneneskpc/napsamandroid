package com.kaaneneskpc.napsam.core.data.datastore

import com.kaaneneskpc.napsam.core.data.repository.DataStorePreferencesRepository
import com.kaaneneskpc.napsam.core.data.testDataStore
import com.kaaneneskpc.napsam.core.domain.repository.ThemeMode
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Tipli DataStore (karar K12). */
class SettingsDataSourceTest {
    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `eski surumdeki kurulum kimligi alani okunurken yok sayilir`() = runTest {
        File(folder.root, AppSettingsSerializer.FILE_NAME).writeText("""{"themeMode":"dark","installId":"eski"}""")
        val settings = SettingsDataSource(testDataStore(folder.root, backgroundScope))

        assertEquals(AppSettings(themeMode = AppSettings.THEME_DARK), settings.current())
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
