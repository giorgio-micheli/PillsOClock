package micheli.giorgio.pillsoclock.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.Utente

@Dao
interface UtentiDao {

    @Query("SELECT * FROM utenti WHERE id = :id")
    fun getUtenteById(id: Int): Flow<Utente?>

    @Query("SELECT * FROM utenti WHERE email = :email")
    suspend fun getUtenteByEmail(email: String): Utente?

    @Query("SELECT * FROM utenti LIMIT 1")
    fun getUtente(): Flow<Utente?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(utente: Utente): Long

    @Update
    suspend fun update(utente: Utente)

    @Delete
    suspend fun delete(utente: Utente)
}