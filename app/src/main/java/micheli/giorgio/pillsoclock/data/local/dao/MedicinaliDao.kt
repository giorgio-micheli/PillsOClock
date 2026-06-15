package micheli.giorgio.pillsoclock.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.Medicinale
import micheli.giorgio.pillsoclock.data.local.entity.relations.MedicinaleConPiano
import micheli.giorgio.pillsoclock.data.local.entity.relations.MedicinaleConPianoEOrari

@Dao
interface MedicinaliDao {

    @Query("SELECT * FROM medicinali WHERE id_utente = :idUtente AND attivo = 1")
    fun getMedicinaliAttivi(idUtente: Int): Flow<List<Medicinale>>

    @Query("SELECT * FROM medicinali WHERE id_utente = :idUtente")
    fun getAllMedicinali(idUtente: Int): Flow<List<Medicinale>>

    @Query("SELECT * FROM medicinali WHERE id = :id")
    fun getMedicinaleById(id: Int): Flow<Medicinale?> // è nullable perchè l'id potrebbe non esistere

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(medicinale: Medicinale): Long

    @Update
    suspend fun update(medicinale: Medicinale)

    @Query("UPDATE medicinali SET attivo = 0 WHERE id = :id")
    suspend fun disattiva(id: Int)

    @Delete
    suspend fun delete(medicinale: Medicinale)

    @Transaction
    @Query("SELECT * FROM medicinali WHERE id = :id")
    fun getMedicinaleConPianoAssunzioneEOrari(id: Int): Flow<MedicinaleConPianoEOrari?>

    @Transaction
    @Query("SELECT * FROM medicinali WHERE id_utente = :idUtente AND attivo = 1")
    fun getMedicinaliAttiviConPianoEOrari(idUtente: Int): Flow<List<MedicinaleConPianoEOrari>>

}