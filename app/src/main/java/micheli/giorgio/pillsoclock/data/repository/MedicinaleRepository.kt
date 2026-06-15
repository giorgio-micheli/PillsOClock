package micheli.giorgio.pillsoclock.data.repository

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.dao.MedicinaliDao
import micheli.giorgio.pillsoclock.data.local.dao.OrariAssunzioniDao
import micheli.giorgio.pillsoclock.data.local.dao.PianiAssunzioniDao
import micheli.giorgio.pillsoclock.data.local.entity.Medicinale
import micheli.giorgio.pillsoclock.data.local.entity.OrarioAssunzione
import micheli.giorgio.pillsoclock.data.local.entity.PianoAssunzione
import micheli.giorgio.pillsoclock.data.local.entity.relations.MedicinaleConPianoEOrari
import micheli.giorgio.pillsoclock.domain.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.MedicinaleRepository

class MedicinaleRepository (
    private val medicinaleDao: MedicinaliDao,
    private val pianoAssunzioneDao: PianiAssunzioniDao,
    private val orarioAssunzioneDao: OrariAssunzioniDao
): MedicinaleRepository {
    // --- lettura ---

    override fun getMedicinaliAttivi(idUtente: Int): Flow<List<Medicinale>> =
        medicinaleDao.getMedicinaliAttivi(idUtente)

    override fun getMedicinaleConPianoEOrari(id: Int): Flow<MedicinaleConPianoEOrari?> =
        medicinaleDao.getMedicinaleConPianoAssunzioneEOrari(id)

    override fun getMedicinaliAttiviConPianoEOrari(idUtente: Int): Flow<List<MedicinaleConPianoEOrari>> =
        medicinaleDao.getMedicinaliAttiviConPianoEOrari(idUtente)

    // --- scrittura ---

    @Transaction
    override suspend fun inserisciMedicinaleConPianoEOrari(
        medicinale: Medicinale,
        piano: PianoAssunzione,
        orari: List<OrarioAssunzione>
    ) {
        val idMedicinale = medicinaleDao.insert(medicinale).toInt()
        val idPiano = pianoAssunzioneDao.insert(
            piano.copy(idMedicinale = idMedicinale)
        ).toInt()
        orarioAssunzioneDao.insertAll(
            orari.map { it.copy(idPianoAssunzione = idPiano) }
        )
    }

    @Transaction
    override suspend fun aggiornaPianoEOrari(
        piano: PianoAssunzione,
        nuoviOrari: List<OrarioAssunzione>
    ) {
        pianoAssunzioneDao.update(piano)
        orarioAssunzioneDao.deleteAllByPiano(piano.id)
        orarioAssunzioneDao.insertAll(
            nuoviOrari.map { it.copy(idPianoAssunzione = piano.id) }
        )
    }

    override suspend fun disattivaMedicinale(id: Int) =
        medicinaleDao.disattiva(id)

    override suspend fun eliminaMedicinale(medicinale: Medicinale) =
        medicinaleDao.delete(medicinale)
}