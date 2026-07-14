package micheli.giorgio.pillsoclock.data.repository

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import micheli.giorgio.pillsoclock.data.local.dao.MedicinaliDao
import micheli.giorgio.pillsoclock.data.local.dao.OrariAssunzioniDao
import micheli.giorgio.pillsoclock.data.local.dao.PianiAssunzioniDao
import micheli.giorgio.pillsoclock.data.local.mapper.toDomain
import micheli.giorgio.pillsoclock.data.local.mapper.toEntity
import micheli.giorgio.pillsoclock.domain.model.Medicinale
import micheli.giorgio.pillsoclock.domain.model.MedicinaleConPianoEOrari
import micheli.giorgio.pillsoclock.domain.model.OrarioAssunzione
import micheli.giorgio.pillsoclock.domain.model.PianoAssunzione
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository

class MedicinaleRepositoryImpl (
    private val medicinaleDao: MedicinaliDao,
    private val pianoAssunzioneDao: PianiAssunzioniDao,
    private val orarioAssunzioneDao: OrariAssunzioniDao
): MedicinaleRepository {
    // --- lettura ---

    /*
    Il primo map è quello appartenente alle coroutine, in particolare ai flow.
    Serve per trasformare il contenuto di un flow in qualcos'altro e ritornare un nuovo flow.
    Il secondo map invece è quello utilizzato sulle collezioni.
     */
    override fun getMedicinaliAttivi(idUtente: Int): Flow<List<Medicinale>> =
        medicinaleDao.getMedicinaliAttivi(idUtente)
            .map { medicinali -> medicinali.map { it.toDomain() } }

    override fun getMedicinaleConPianoEOrari(id: Int): Flow<MedicinaleConPianoEOrari?> =
        medicinaleDao.getMedicinaleConPianoAssunzioneEOrari(id)
            .map { medicinale -> medicinale?.toDomain() }

    override fun getMedicinaliAttiviConPianoEOrari(idUtente: Int): Flow<List<MedicinaleConPianoEOrari>> =
        medicinaleDao.getMedicinaliAttiviConPianoEOrari(idUtente)
            .map { medicinali -> medicinali.map { it.toDomain() } }

    override fun getTuttiMedicinaliConPianoEOrari(idUtente: Int): Flow<List<MedicinaleConPianoEOrari>> =
        medicinaleDao.getTuttiMedicinaliConPianoEOrari(idUtente)
            .map { medicinali -> medicinali.map { it.toDomain() } }

    // --- scrittura ---

    @Transaction
    override suspend fun inserisciMedicinaleConPianoEOrari(
        medicinale: Medicinale,
        piano: PianoAssunzione,
        orari: List<OrarioAssunzione>
    ) {
        val idMedicinale = medicinaleDao.insert(medicinale.toEntity()).toInt()
        val idPiano = pianoAssunzioneDao.insert(
            piano.copy(idMedicinale = idMedicinale).toEntity()
        ).toInt()
        orarioAssunzioneDao.insertAll(
            orari.map { it.copy(idPianoAssunzione = idPiano).toEntity() }
        )
    }

    override suspend fun aggiornaMedicinale(medicinale: Medicinale) =
        medicinaleDao.update(medicinale.toEntity())

    @Transaction
    override suspend fun aggiornaPianoEOrari(
        piano: PianoAssunzione,
        nuoviOrari: List<OrarioAssunzione>
    ) {
        pianoAssunzioneDao.update(piano.toEntity())
        orarioAssunzioneDao.deleteAllByPiano(piano.id)
        orarioAssunzioneDao.insertAll(
            nuoviOrari.map { it.copy(idPianoAssunzione = piano.id).toEntity() }
        )
    }

    override suspend fun disattivaMedicinale(id: Int) =
        medicinaleDao.disattiva(id)

    override suspend fun attivaMedicinale(id: Int) =
        medicinaleDao.attiva(id)

    override suspend fun eliminaMedicinale(medicinale: Medicinale) =
        medicinaleDao.delete(medicinale.toEntity())
}