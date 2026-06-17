package micheli.giorgio.pillsoclock.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entità che rappresenta i medicinali che l'utente deve assumere
 */
@Entity(
    tableName = "medicinali",
    foreignKeys = [
        ForeignKey(
            entity = UtenteEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_utente"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("id_utente")
    ]
)
data class MedicinaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo("id_utente")
    val idUtente: Int,

    val nome: String,
    val dosaggio: String?,
    val note: String?,
    val attivo: Boolean,

    @ColumnInfo("data_inizio")
    val dataInizio: LocalDateTime,

    @ColumnInfo("data_fine")
    val dataFine: LocalDateTime?
)