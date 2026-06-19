package micheli.giorgio.pillsoclock.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.AssunzioneEffettuataEntity
import micheli.giorgio.pillsoclock.data.local.entity.relations.AssunzioneGiornalieraEntity
import java.time.LocalDate

@Dao
interface AssunzioniEffettuateDao {

    // --- query per la home ---

    @Query("SELECT * FROM assunzioni_effettuate WHERE id_assunzione_prevista = :idAssunzionePrevista")
    fun getAssunzioneEffettuataByPrevista(idAssunzionePrevista: Int): Flow<AssunzioneEffettuataEntity?>

    // --- query per lo storico ---

    @Query("""
        SELECT ae.* FROM assunzioni_effettuate ae
        INNER JOIN assunzioni_previste ap ON ae.id_assunzione_prevista = ap.id
        WHERE ap.data = :data AND ae.id_utente = :idUtente
        ORDER BY ae.timestamp_assunzione ASC
    """)
    fun getAssunzioniEffettuatePerGiorno(idUtente: Int, data: LocalDate): Flow<List<AssunzioneEffettuataEntity>>

    @Transaction
    @Query("""
        SELECT ap.*, m.nome AS nomeMedicinale, m.dosaggio AS dosaggio 
        FROM assunzioni_previste ap
        INNER JOIN orari_assunzioni oa ON ap.id_orario_assunzione = oa.id
        INNER JOIN piani_assunzioni pa ON oa.id_piano_assunzione = pa.id
        INNER JOIN medicinali m ON pa.id_medicinale = m.id
        WHERE ap.data = :data AND m.id_utente = :idUtente
        ORDER BY ap.orario_previsto ASC
    """)
    fun getAssunzioniGiornaliere(
        idUtente: Int,
        data: LocalDate
    ): Flow<List<AssunzioneGiornalieraEntity>>

    // --- scrittura ---

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(assunzioneEffettuata: AssunzioneEffettuataEntity): Long

    @Delete
    suspend fun delete(assunzioneEffettuata: AssunzioneEffettuataEntity)

    @Query("DELETE FROM assunzioni_effettuate WHERE id_assunzione_prevista = :idAssunzionePrevista")
    suspend fun deleteByPrevista(idAssunzionePrevista: Int)

}