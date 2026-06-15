package micheli.giorgio.pillsoclock.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entità degli utenti, al momento l'applicazione verrà implementata per supportare solamente un
 * solo utente. Nel futuro forse verrà aggiunta la possibilità di gestire più utenti dalla stessa
 * applicazione.
 */
@Entity(tableName = "utenti")
data class Utente(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    val nome: String,
    val email: String,

    @ColumnInfo("data_registrazione")
    val dataRegistrazione: LocalDateTime
)