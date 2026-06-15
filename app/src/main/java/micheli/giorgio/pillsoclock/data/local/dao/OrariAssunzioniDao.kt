package micheli.giorgio.pillsoclock.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.OrarioAssunzione
import micheli.giorgio.pillsoclock.data.local.entity.PianoAssunzione

@Dao
interface OrariAssunzioniDao {

    @Query("SELECT * FROM orari_assunzioni WHERE id_piano_assunzione = :idPianoAssunzione")
    fun getOrariByPiano(idPianoAssunzione: Int): Flow<List<OrarioAssunzione>>

    @Query("SELECT * FROM orari_assunzioni WHERE id = :id")
    fun getOrarioById(id: Int): Flow<OrarioAssunzione?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(orarioAssunzione: OrarioAssunzione): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(orari: List<OrarioAssunzione>): List<Long>

    @Update
    suspend fun update(orarioAssunzione: OrarioAssunzione)

    @Delete
    suspend fun delete(orarioAssunzione: OrarioAssunzione)

    @Query("DELETE FROM orari_assunzioni WHERE id_piano_assunzione = :idPianoAssunzione")
    suspend fun deleteAllByPiano(idPianoAssunzione: Int)

}