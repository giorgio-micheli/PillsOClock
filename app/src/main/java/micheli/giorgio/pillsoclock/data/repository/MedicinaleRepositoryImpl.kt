package micheli.giorgio.pillsoclock.data.repository

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    override fun checkIfAMedicinaleExist(idUtente: Int): Flow<Boolean> {
        return medicinaleDao.checkIfAMedicinaleExist(idUtente)
            .map { medicinale -> medicinale?.toDomain() != null }
    }

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

        // Diff invece di delete-all/recreate: preserva l'id degli orari invariati
        // così AssunzionePrevista/AssunzioneEffettuata storiche non vengono perse
        // per un cascade delete indesiderato (vedi OrarioAssunzioneEntity onDelete = CASCADE).
        val orariEsistenti = orarioAssunzioneDao.getOrariByPiano(piano.id).first()
        val esistentiPerOrario = orariEsistenti.associateBy { it.orario }
        val nuoviValori = nuoviOrari.map { it.orario }.toSet()

        // Orari rimossi dal piano: soft delete, la cronologia resta intatta
        orariEsistenti
            .filter { it.attivo && it.orario !in nuoviValori }
            .forEach { orarioAssunzioneDao.disattiva(it.id) }

        nuoviOrari.forEach { nuovo ->
            val esistente = esistentiPerOrario[nuovo.orario]
            when {
                esistente == null ->
                    orarioAssunzioneDao.insert(nuovo.copy(idPianoAssunzione = piano.id).toEntity())
                !esistente.attivo ->
                    orarioAssunzioneDao.attiva(esistente.id)
                // esistente già attivo con lo stesso orario: id stabile, nessuna azione
            }
        }
    }

    override suspend fun disattivaMedicinale(id: Int) =
        medicinaleDao.disattiva(id)

    override suspend fun attivaMedicinale(id: Int) =
        medicinaleDao.attiva(id)

    override suspend fun eliminaMedicinale(id: Int) =
        medicinaleDao.elimina(id)
}