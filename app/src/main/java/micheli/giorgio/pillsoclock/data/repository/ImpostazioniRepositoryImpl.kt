package micheli.giorgio.pillsoclock.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import micheli.giorgio.pillsoclock.domain.repository.ImpostazioniRepository

private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_abilitata")

class ImpostazioniRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : ImpostazioniRepository {

    override fun getDarkModeAbilitata(): Flow<Boolean> =
        dataStore.data.map { preferenze -> preferenze[DARK_MODE_KEY] ?: false }

    override suspend fun impostaDarkMode(abilitata: Boolean) {
        dataStore.edit { preferenze -> preferenze[DARK_MODE_KEY] = abilitata }
    }
}
