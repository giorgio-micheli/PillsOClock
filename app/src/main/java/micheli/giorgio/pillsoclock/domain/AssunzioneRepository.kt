package micheli.giorgio.pillsoclock.domain

import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.AssunzionePrevista
import micheli.giorgio.pillsoclock.data.local.entity.PianoAssunzione
import micheli.giorgio.pillsoclock.data.local.entity.relations.AssunzionePrevistaConEffettuata
import micheli.giorgio.pillsoclock.data.local.entity.relations.MedicinaleConPianoEOrari
import java.time.LocalDate

interface AssunzioneRepository {

    fun getAssunzioniPerGiorno(idUtente: Int, data: LocalDate): Flow<List<AssunzionePrevistaConEffettuata>>

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