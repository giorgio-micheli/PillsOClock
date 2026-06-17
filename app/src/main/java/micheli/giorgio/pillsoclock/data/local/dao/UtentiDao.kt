package micheli.giorgio.pillsoclock.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.UtenteEntity

@Dao
interface UtentiDao {

    @Query("SELECT * FROM utenti WHERE id = :id")
    fun getUtenteById(id: Int): Flow<UtenteEntity?>

    @Query("SELECT * FROM utenti WHERE email = :email")
    suspend fun getUtenteByEmail(email: String): UtenteEntity?

    @Query("SELECT * FROM utenti LIMIT 1")
    fun getUtente(): Flow<UtenteEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(utente: UtenteEntity): Long

    @Update
    suspend fun update(utente: UtenteEntity)

    @Delete
    suspend fun delete(utente: UtenteEntity)
}