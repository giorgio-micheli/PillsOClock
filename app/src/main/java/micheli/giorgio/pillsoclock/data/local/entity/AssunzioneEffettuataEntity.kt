package micheli.giorgio.pillsoclock.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "assunzioni_effettuate",
    foreignKeys = [
        ForeignKey(
            entity = AssunzionePrevistaEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_assunzione_prevista"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UtenteEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_utente"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_assunzione_prevista"], unique = true),
        Index(value = ["id_utente"])
    ]
)
data class AssunzioneEffettuataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "id_assunzione_prevista")
    val idAssunzionePrevista: Int,

    @ColumnInfo(name = "id_utente")
    val idUtente: Int,

    @ColumnInfo(name = "timestamp_assunzione")
    val timestampAssunzione: LocalDateTime,

    val note: String? = null
)