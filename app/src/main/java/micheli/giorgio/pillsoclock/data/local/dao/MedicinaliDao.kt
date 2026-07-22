package micheli.giorgio.pillsoclock.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.MedicinaleEntity
import micheli.giorgio.pillsoclock.data.local.entity.relations.MedicinaleConPianoEOrariEntity

@Dao
interface MedicinaliDao {

    @Query("SELECT * FROM medicinali WHERE id_utente = :idUtente AND attivo = 1 AND eliminato = 0")
    fun getMedicinaliAttivi(idUtente: Int): Flow<List<MedicinaleEntity>>

    @Query("SELECT * FROM medicinali WHERE id_utente = :idUtente")
    fun getAllMedicinali(idUtente: Int): Flow<List<MedicinaleEntity>>

    @Query("SELECT * FROM medicinali WHERE id_utente = :idUtente AND attivo = 1 AND eliminato = 0 LIMIT 1")
    fun checkIfAMedicinaleExist(idUtente: Int): Flow<MedicinaleEntity?>

    @Query("SELECT * FROM medicinali WHERE id = :id")
    fun getMedicinaleById(id: Int): Flow<MedicinaleEntity?> // è nullable perchè l'id potrebbe non esistere

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(medicinale: MedicinaleEntity): Long

    @Update
    suspend fun update(medicinale: MedicinaleEntity)

    @Query("UPDATE medicinali SET attivo = 0 WHERE id = :id")
    suspend fun disattiva(id: Int)

    @Query("UPDATE medicinali SET attivo = 1 WHERE id = :id")
    suspend fun attiva(id: Int)

    @Query("UPDATE medicinali SET eliminato = 1 WHERE id = :id")
    suspend fun elimina(id: Int)

    @Transaction
    @Query("SELECT * FROM medicinali WHERE id = :id")
    fun getMedicinaleConPianoAssunzioneEOrari(id: Int): Flow<MedicinaleConPianoEOrariEntity?>

    @Transaction
    @Query("SELECT * FROM medicinali WHERE id_utente = :idUtente AND attivo = 1 AND eliminato = 0")
    fun getMedicinaliAttiviConPianoEOrari(idUtente: Int): Flow<List<MedicinaleConPianoEOrariEntity>>

    @Transaction
    @Query("SELECT * FROM medicinali WHERE id_utente = :idUtente AND eliminato = 0")
    fun getTuttiMedicinaliConPianoEOrari(idUtente: Int): Flow<List<MedicinaleConPianoEOrariEntity>>

}