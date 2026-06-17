package micheli.giorgio.pillsoclock.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.OrarioAssunzioneEntity

@Dao
interface OrariAssunzioniDao {

    @Query("SELECT * FROM orari_assunzioni WHERE id_piano_assunzione = :idPianoAssunzione")
    fun getOrariByPiano(idPianoAssunzione: Int): Flow<List<OrarioAssunzioneEntity>>

    @Query("SELECT * FROM orari_assunzioni WHERE id = :id")
    fun getOrarioById(id: Int): Flow<OrarioAssunzioneEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(orarioAssunzione: OrarioAssunzioneEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(orari: List<OrarioAssunzioneEntity>): List<Long>

    @Update
    suspend fun update(orarioAssunzione: OrarioAssunzioneEntity)

    @Delete
    suspend fun delete(orarioAssunzione: OrarioAssunzioneEntity)

    @Query("DELETE FROM orari_assunzioni WHERE id_piano_assunzione = :idPianoAssunzione")
    suspend fun deleteAllByPiano(idPianoAssunzione: Int)

}