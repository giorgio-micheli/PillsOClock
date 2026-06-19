package micheli.giorgio.pillsoclock.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import micheli.giorgio.pillsoclock.data.local.dao.UtentiDao
import micheli.giorgio.pillsoclock.data.local.mapper.toDomain
import micheli.giorgio.pillsoclock.data.local.mapper.toEntity
import micheli.giorgio.pillsoclock.domain.model.Utente
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository

class UtenteRepositoryImpl(
    val utenteDao: UtentiDao
): UtenteRepository {

    override fun getUtente(): Flow<Utente?> =
        utenteDao.getUtente().map { entity -> entity?.toDomain() }

    override suspend fun getUtenteByEmail(email: String): Utente? =
        utenteDao.getUtenteByEmail(email)?.toDomain()

    override suspend fun inserisciUtente(utente: Utente): Int =
        utenteDao.insert(utente.toEntity()).toInt()

    override suspend fun aggiornaUtente(utente: Utente) =
        utenteDao.update(utente.toEntity())

    override suspend fun eliminaUtente(utente: Utente) =
        utenteDao.delete(utente.toEntity())

}