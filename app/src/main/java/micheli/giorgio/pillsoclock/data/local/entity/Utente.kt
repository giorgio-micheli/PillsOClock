package micheli.giorgio.pillsoclock.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "utenti")
data class Utente(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    val nome: String,
    val email: String,

    @ColumnInfo("data_registrazione")
    val dataRegistrazione: LocalDateTime
)