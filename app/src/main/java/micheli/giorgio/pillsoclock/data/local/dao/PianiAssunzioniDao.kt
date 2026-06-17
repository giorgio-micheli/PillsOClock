package micheli.giorgio.pillsoclock.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.PianoAssunzioneEntity
import micheli.giorgio.pillsoclock.data.local.entity.relations.PianoConOrariEntity

@Dao
interface PianiAssunzioniDao {

    @Query("SELECT * FROM piani_assunzioni WHERE id_medicinale = :idMedicinale")
    fun getPianiByMedicinale(idMedicinale: Int): Flow<List<PianoAssunzioneEntity>>

    @Query("SELECT * FROM piani_assunzioni WHERE id = :id")
    fun getPianoById(id: Int): Flow<PianoAssunzioneEntity?>

    @Transaction
    @Query("SELECT * FROM piani_assunzioni WHERE id = :id")
    fun getPianoConOrari(id: Int): Flow<PianoConOrariEntity?>

    @Transaction
    @Query("SELECT * FROM piani_assunzioni WHERE id_medicinale = :idMedicinale")
    fun getPianiConOrariByMedicinale(idMedicinale: Int): Flow<List<PianoConOrariEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(pianoAssunzione: PianoAssunzioneEntity): Long

    @Update
    suspend fun update(pianoAssunzione: PianoAssunzioneEntity)

    @Delete
    suspend fun delete(pianoAssunzione: PianoAssunzioneEntity)

}