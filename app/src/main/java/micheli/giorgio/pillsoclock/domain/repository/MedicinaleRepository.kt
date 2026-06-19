package micheli.giorgio.pillsoclock.domain.repository

import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.domain.model.Medicinale
import micheli.giorgio.pillsoclock.domain.model.MedicinaleConPianoEOrari
import micheli.giorgio.pillsoclock.domain.model.OrarioAssunzione
import micheli.giorgio.pillsoclock.domain.model.PianoAssunzione

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