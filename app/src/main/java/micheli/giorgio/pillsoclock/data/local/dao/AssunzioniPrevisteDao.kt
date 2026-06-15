package micheli.giorgio.pillsoclock.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.AssunzionePrevista
import java.time.LocalDate

@Dao
interface AssunzioniPrevisteDao {

    // --- query per la home ---

    @Query("SELECT * FROM assunzioni_previste WHERE data = :data AND id_orario_assunzione IN " +
            "(SELECT id FROM orari_assunzioni WHERE id_piano_assunzione IN " +
            "(SELECT id FROM piani_assunzioni WHERE id_medicinale IN " +
            "(SELECT id FROM medicinali WHERE id_utente = :idUtente))) ORDER BY orario_previsto ASC")
    fun getAssunzioniPrevistePerGiorno(idUtente: Int, data: LocalDate): Flow<List<AssunzionePrevista>>

    @Query("SELECT * FROM assunzioni_previste WHERE data = :data AND stato = 'IN_ATTESA' AND id_orario_assunzione IN " +
            "(SELECT id FROM orari_assunzioni WHERE id_piano_assunzione IN " +
            "(SELECT id FROM piani_assunzioni WHERE id_medicinale IN " +
            "(SELECT id FROM medicinali WHERE id_utente = :idUtente))) ORDER BY orario_previsto ASC")
    fun getAssunzioniInAttesaPerGiorno(idUtente: Int, data: LocalDate): Flow<List<AssunzionePrevista>>

    // --- query per lo storico ---

    @Query("SELECT DISTINCT data FROM assunzioni_previste WHERE data BETWEEN :dataInizio AND :dataFine AND id_orario_assunzione IN " +
            "(SELECT id FROM orari_assunzioni WHERE id_piano_assunzione IN " +
            "(SELECT id FROM piani_assunzioni WHERE id_medicinale IN " +
            "(SELECT id FROM medicinali WHERE id_utente = :idUtente)))")
    fun getGiorniConAssunzioni(idUtente: Int, dataInizio: LocalDate, dataFine: LocalDate): Flow<List<LocalDate>>

    // --- query di servizio per la generazione giornaliera ---

    @Query("SELECT * FROM assunzioni_previste WHERE id_orario_assunzione = :idOrarioAssunzione AND data = :data")
    suspend fun getAssunzionePrevistaByOrarioEData(idOrarioAssunzione: Int, data: LocalDate): AssunzionePrevista?

    @Query("SELECT * FROM assunzioni_previste WHERE id = :id")
    fun getAssunzionePrevistaById(id: Int): Flow<AssunzionePrevista?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(assunzionePrevista: AssunzionePrevista): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(assunzioni: List<AssunzionePrevista>): List<Long>

    @Query("UPDATE assunzioni_previste SET stato = :stato WHERE id = :id")
    suspend fun aggiornaStato(id: Int, stato: String)

    @Query("UPDATE assunzioni_previste SET stato = 'SALTATA' WHERE data < :oggi AND stato = 'IN_ATTESA' AND id_orario_assunzione IN " +
            "(SELECT id FROM orari_assunzioni WHERE id_piano_assunzione IN " +
            "(SELECT id FROM piani_assunzioni WHERE id_medicinale IN " +
            "(SELECT id FROM medicinali WHERE id_utente = :idUtente)))")
    suspend fun segnaVecchieComeSaltate(idUtente: Int, oggi: LocalDate)

    @Delete
    suspend fun delete(assunzionePrevista: AssunzionePrevista)
}