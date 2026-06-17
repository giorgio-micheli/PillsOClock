package micheli.giorgio.pillsoclock.domain.repository

import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.MedicinaleEntity
import micheli.giorgio.pillsoclock.data.local.entity.OrarioAssunzioneEntity
import micheli.giorgio.pillsoclock.data.local.entity.PianoAssunzioneEntity
import micheli.giorgio.pillsoclock.domain.models.Medicinale
import micheli.giorgio.pillsoclock.domain.models.MedicinaleConPianoEOrari
import micheli.giorgio.pillsoclock.domain.models.OrarioAssunzione
import micheli.giorgio.pillsoclock.domain.models.PianoAssunzione

interface MedicinaleRepository {

    fun getMedicinaliAttivi(idUtente: Int): Flow<List<Medicinale>>

    fun getMedicinaleConPianoEOrari(id: Int): Flow<MedicinaleConPianoEOrari?>

    fun getMedicinaliAttiviConPianoEOrari(idUtente: Int): Flow<List<MedicinaleConPianoEOrari>>

    suspend fun inserisciMedicinaleConPianoEOrari(
        medicinale: Medicinale,
        piano: PianoAssunzione,
        orari: List<OrarioAssunzione>
    )

    suspend fun aggiornaPianoEOrari(
        piano: PianoAssunzione,
        nuoviOrari: List<OrarioAssunzione>
    )

    suspend fun disattivaMedicinale(id: Int)

    suspend fun eliminaMedicinale(medicinale: Medicinale)

}