package com.kaaneneskpc.napsam.core.data.repository

import com.kaaneneskpc.napsam.core.data.datastore.AppSettings
import com.kaaneneskpc.napsam.core.data.datastore.SettingsDataSource
import com.kaaneneskpc.napsam.core.domain.repository.PreferencesRepository
import com.kaaneneskpc.napsam.core.domain.repository.ThemeMode
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class DataStorePreferencesRepository @Inject constructor(private val settings: SettingsDataSource) :
    PreferencesRepository {

    override fun observeThemeMode(): Flow<ThemeMode> = settings.settings
        .map { settings ->
            when (settings.themeMode) {
                AppSettings.THEME_LIGHT -> ThemeMode.LIGHT
                AppSettings.THEME_DARK -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }
        .distinctUntilChanged()

    override suspend fun setThemeMode(mode: ThemeMode) {
        val key = when (mode) {
            ThemeMode.SYSTEM -> AppSettings.THEME_SYSTEM
            ThemeMode.LIGHT -> AppSettings.THEME_LIGHT
            ThemeMode.DARK -> AppSettings.THEME_DARK
        }
        settings.update { it.copy(themeMode = key) }
    }
}
