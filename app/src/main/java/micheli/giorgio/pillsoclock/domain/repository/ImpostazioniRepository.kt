package micheli.giorgio.pillsoclock.domain.repository

import kotlinx.coroutines.flow.Flow

interface ImpostazioniRepository {

    fun getDarkModeAbilitata(): Flow<Boolean>

    suspend fun impostaDarkMode(abilitata: Boolean)
}
