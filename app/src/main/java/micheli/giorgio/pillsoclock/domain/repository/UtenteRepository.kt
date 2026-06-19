package micheli.giorgio.pillsoclock.domain.repository

import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.domain.model.Utente

interface UtenteRepository {

    fun getUtente(): Flow<Utente?>

    suspend fun getUtenteByEmail(email: String): Utente?
    suspend fun inserisciUtente(utente: Utente): Int
    suspend fun aggiornaUtente(utente: Utente)
    suspend fun eliminaUtente(utente: Utente)
}