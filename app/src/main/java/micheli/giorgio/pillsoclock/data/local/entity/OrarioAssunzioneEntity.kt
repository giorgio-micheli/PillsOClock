package micheli.giorgio.pillsoclock.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalTime

/**
 * Entità che rappresenta tutti gli orari per cui è stata programmata l'assunzione di un certo medicinale.
 * Un medicinale può avere più orari di assunzione.
 */
@Entity(
    tableName = "orari_assunzioni",
    foreignKeys = [
        ForeignKey(
            entity = PianoAssunzioneEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_piano_assunzione"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("id_piano_assunzione"),
        Index(value = ["id_piano_assunzione", "orario"], unique = true)
    ]
)
data class OrarioAssunzioneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "id_piano_assunzione")
    val idPianoAssunzione: Int,

    val orario: LocalTime
)