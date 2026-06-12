package micheli.giorgio.pillsoclock.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalTime


@Entity(
    tableName = "orari_assunzioni",
    foreignKeys = [
        ForeignKey(
            entity = PianoAssunzione::class,
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
data class OrarioAssunzione(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "id_piano_assunzione")
    val id_piano_assunzione: Int,

    val orario: LocalTime
)