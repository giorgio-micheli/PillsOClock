package micheli.giorgio.pillsoclock.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.PianoAssunzione
import micheli.giorgio.pillsoclock.data.local.entity.relations.PianoConOrari

@Dao
interface PianiAssunzioniDao {

    @Query("SELECT * FROM piani_assunzioni WHERE id_medicinale = :idMedicinale")
    fun getPianiByMedicinale(idMedicinale: Int): Flow<List<PianoAssunzione>>

    @Query("SELECT * FROM piani_assunzioni WHERE id = :id")
    fun getPianoById(id: Int): Flow<PianoAssunzione?>

    @Transaction
    @Query("SELECT * FROM piani_assunzioni WHERE id = :id")
    fun getPianoConOrari(id: Int): Flow<PianoConOrari?>

    @Transaction
    @Query("SELECT * FROM piani_assunzioni WHERE id_medicinale = :idMedicinale")
    fun getPianiConOrariByMedicinale(idMedicinale: Int): Flow<List<PianoConOrari>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(pianoAssunzione: PianoAssunzione): Long

    @Update
    suspend fun update(pianoAssunzione: PianoAssunzione)

    @Delete
    suspend fun delete(pianoAssunzione: PianoAssunzione)

}