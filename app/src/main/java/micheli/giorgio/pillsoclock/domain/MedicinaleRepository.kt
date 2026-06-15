package micheli.giorgio.pillsoclock.domain

import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.Medicinale
import micheli.giorgio.pillsoclock.data.local.entity.OrarioAssunzione
import micheli.giorgio.pillsoclock.data.local.entity.PianoAssunzione
import micheli.giorgio.pillsoclock.data.local.entity.relations.MedicinaleConPianoEOrari

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