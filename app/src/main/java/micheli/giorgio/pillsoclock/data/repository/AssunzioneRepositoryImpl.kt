package micheli.giorgio.pillsoclock.data.repository

import androidx.room.Transaction
import kotlinx.coroutines.flow.map
import micheli.giorgio.pillsoclock.data.local.dao.AssunzioniEffettuateDao
import micheli.giorgio.pillsoclock.data.local.dao.AssunzioniPrevisteDao
import micheli.giorgio.pillsoclock.data.local.entity.AssunzioneEffettuataEntity
import micheli.giorgio.pillsoclock.data.local.entity.AssunzionePrevistaEntity
import micheli.giorgio.pillsoclock.data.local.entity.StatoAssunzione
import micheli.giorgio.pillsoclock.data.local.entity.TipoFrequenza
import micheli.giorgio.pillsoclock.data.local.mapper.toDomain
import micheli.giorgio.pillsoclock.domain.model.AssunzionePrevista
import micheli.giorgio.pillsoclock.domain.model.MedicinaleConPianoEOrari
import micheli.giorgio.pillsoclock.domain.model.PianoAssunzione
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class AssunzioneRepositoryImpl (
    private val assunzionePrevistaDao: AssunzioniPrevisteDao,
    private val assunzioneEffettuataDao: AssunzioniEffettuateDao
): AssunzioneRepository {
    // --- lettura ---

    override fun getAssunzioniGiornaliere(idUtente: Int, data: LocalDate) =
        assunzioneEffettuataDao.getAssunzioniGiornaliere(idUtente, data)
            .map { assunzioni -> assunzioni.map { it.toDomain() } }


    override fun getGiorniConAssunzioni(idUtente: Int, dataInizio: LocalDate, dataFine: LocalDate) =
        assunzionePrevistaDao.getGiorniConAssunzioni(idUtente, dataInizio, dataFine)

    // --- generazione giornaliera ---

    override suspend fun generaAssunzioniPerGiorno(
        idUtente: Int,
        data: LocalDate,
        medicinaliConPianoEOrari: List<MedicinaleConPianoEOrari>
    ) {
        val assunzioniDaInserire = medicinaliConPianoEOrari
            .flatMap { it.piani }
            .filter { pianoConOrari -> isPianoAttivoInData(pianoConOrari.piano, data) }
            .flatMap { pianoConOrari ->
                pianoConOrari.orari.map { orario ->
                    AssunzionePrevistaEntity(
                        idOrarioAssunzione = orario.id,
                        data = data,
                        orarioPrevisto = orario.orario,
                        stato = StatoAssunzione.IN_ATTESA
                    )
                }
            }
        assunzionePrevistaDao.insertAll(assunzioniDaInserire)
    }

    override fun isPianoAttivoInData(piano: PianoAssunzione, data: LocalDate): Boolean {
        return when (piano.tipoFrequenza) {
            TipoFrequenza.GIORNALIERA -> true
            TipoFrequenza.OGNI_N_GIORNI -> {
                val giorniDallinizio = ChronoUnit.DAYS.between(
                    piano.dataInizio, data // dataInizio va aggiunta a PianoAssunzione
                )
                giorniDallinizio % (piano.intervalloGiorni ?: 1) == 0L
            }
            TipoFrequenza.GIORNI_SETTIMANA -> {
                val giornoSettimana = data.dayOfWeek.value // 1=lunedì, 7=domenica
                piano.giorniSettimana
                    ?.contains(giornoSettimana) == true
            }
        }
    }

    // --- azione utente ---

    @Transaction
    override suspend fun registraAssunzione(assunzionePrevista: AssunzionePrevista, idUtente: Int) {
        assunzioneEffettuataDao.insert(
            AssunzioneEffettuataEntity(
                idAssunzionePrevista = assunzionePrevista.id,
                idUtente = idUtente,
                timestampAssunzione = LocalDateTime.now()
            )
        )
        assunzionePrevistaDao.aggiornaStato(assunzionePrevista.id, "ASSUNTA")
    }

    @Transaction
    override suspend fun annullaAssunzione(assunzionePrevista: AssunzionePrevista) {
        assunzioneEffettuataDao.deleteByPrevista(assunzionePrevista.id)
        assunzionePrevistaDao.aggiornaStato(assunzionePrevista.id, "IN_ATTESA")
    }

    override suspend fun segnaVecchieComeSaltate(idUtente: Int) =
        assunzionePrevistaDao.segnaVecchieComeSaltate(idUtente, LocalDate.now())
}