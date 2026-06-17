package micheli.giorgio.pillsoclock.domain.repository

import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.AssunzionePrevistaEntity
import micheli.giorgio.pillsoclock.data.local.entity.PianoAssunzioneEntity
import micheli.giorgio.pillsoclock.data.local.entity.relations.AssunzionePrevistaConEffettuataEntity
import micheli.giorgio.pillsoclock.data.local.entity.relations.MedicinaleConPianoEOrariEntity
import micheli.giorgio.pillsoclock.domain.models.AssunzionePrevista
import micheli.giorgio.pillsoclock.domain.models.AssunzionePrevistaConEffettuata
import micheli.giorgio.pillsoclock.domain.models.MedicinaleConPianoEOrari
import micheli.giorgio.pillsoclock.domain.models.PianoAssunzione
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