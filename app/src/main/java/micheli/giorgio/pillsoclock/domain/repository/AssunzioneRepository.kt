package micheli.giorgio.pillsoclock.domain.repository

import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.domain.model.AssunzioneGiornaliera
import micheli.giorgio.pillsoclock.domain.model.AssunzionePrevista
import micheli.giorgio.pillsoclock.domain.model.MedicinaleConPianoEOrari
import micheli.giorgio.pillsoclock.domain.model.PianoAssunzione
import java.time.LocalDate
import java.time.LocalDateTime

interface AssunzioneRepository {

    fun getAssunzioniGiornaliere(idUtente: Int, data: LocalDate): Flow<List<AssunzioneGiornaliera>>

    fun getAssunzioniGiornaliereStorico(idUtente: Int, data: LocalDate): Flow<List<AssunzioneGiornaliera>>

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

    suspend fun registraAssunzione(
        assunzionePrevista: AssunzionePrevista,
        idUtente: Int,
        timestamp: LocalDateTime = LocalDateTime.now()
    )

    suspend fun annullaAssunzione(assunzionePrevista: AssunzionePrevista)

    suspend fun deleteAssunzioniPrevisteFromPiano(idPiano: Int)

    suspend fun segnaVecchieComeSaltate(idUtente: Int)

    suspend fun getAssunzioniPrevisteInAttesaOggiPerPiano(idPiano: Int, data: LocalDate): List<AssunzionePrevista>

}