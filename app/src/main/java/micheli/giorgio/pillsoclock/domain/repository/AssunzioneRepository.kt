package micheli.giorgio.pillsoclock.domain.repository

import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.domain.model.AssunzioneGiornaliera
import micheli.giorgio.pillsoclock.domain.model.AssunzionePrevista
import micheli.giorgio.pillsoclock.domain.model.MedicinaleConPianoEOrari
import micheli.giorgio.pillsoclock.domain.model.PianoAssunzione
import java.time.LocalDate

interface AssunzioneRepository {

    fun getAssunzioniGiornaliere(idUtente: Int, data: LocalDate): Flow<List<AssunzioneGiornaliera>>

    fun getGiorniConAssunzioni(
        idUtente: Int,
        dataInizio: LocalDate,
        dataFine: LocalDate
    ): Flow<List<LocalDate>>

    suspend fun generaAssunzioniPerGiorno(
        idUtente: Int,
        data: LocalDate,
        medicinaliConPianoEOrari: List<MedicinaleConPianoEOrari>
    )

    fun isPianoAttivoInData(piano: PianoAssunzione, data: LocalDate): Boolean

    suspend fun registraAssunzione(assunzionePrevista: AssunzionePrevista, idUtente: Int)

    suspend fun annullaAssunzione(assunzionePrevista: AssunzionePrevista)

    suspend fun segnaVecchieComeSaltate(idUtente: Int)

}