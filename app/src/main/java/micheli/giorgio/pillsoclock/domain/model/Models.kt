package micheli.giorgio.pillsoclock.domain.model

import micheli.giorgio.pillsoclock.data.local.entity.StatoAssunzione
import micheli.giorgio.pillsoclock.data.local.entity.TipoFrequenza
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Medicinale(
    val id: Int,
    val idUtente: Int,
    val nome: String,
    val dosaggio: String?,
    val note: String?,
    val attivo: Boolean,
    val dataInizio: LocalDateTime,
    val dataFine: LocalDateTime?
)

data class PianoAssunzione(
    val id: Int,
    val idMedicinale: Int,
    val tipoFrequenza: TipoFrequenza,
    val intervalloGiorni: Int?,
    val giorniSettimana: List<Int>?, // qui ha senso usare List<Int> invece di String, il domain non deve sapere come viene serializzato
    val dataInizio: LocalDate
)


data class OrarioAssunzione(
    val id: Int,
    val idPianoAssunzione: Int,
    val orario: LocalTime
)

data class AssunzionePrevista(
    val id: Int,
    val idOrarioAssunzione: Int,
    val data: LocalDate,
    val orarioPrevisto: LocalTime,
    val stato: StatoAssunzione
)

data class AssunzioneEffettuata(
    val id: Int,
    val idAssunzionePrevista: Int,
    val idUtente: Int,
    val timestampAssunzione: LocalDateTime,
    val note: String?
)

data class PianoConOrari(
    val piano: PianoAssunzione,
    val orari: List<OrarioAssunzione>
)

data class MedicinaleConPianoEOrari(
    val medicinale: Medicinale,
    val piani: List<PianoConOrari>
)

data class AssunzionePrevistaConEffettuata(
    val assunzionePrevista: AssunzionePrevista,
    val assunzioneEffettuata: AssunzioneEffettuata?
)

data class AssunzioneGiornaliera(
    val assunzionePrevista: AssunzionePrevista,
    val assunzioneEffettuata: AssunzioneEffettuata?,
    val nomeMedicinale: String,
    val dosaggio: String?
)