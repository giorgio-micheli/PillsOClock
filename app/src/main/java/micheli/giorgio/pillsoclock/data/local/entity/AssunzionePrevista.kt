package micheli.giorgio.pillsoclock.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

enum class StatoAssunzione {
    IN_ATTESA,
    ASSUNTA,
    SALTATA
}

@Entity(
    tableName = "assunzioni_previste",
    foreignKeys = [
        ForeignKey(
            entity = OrarioAssunzione::class,
            parentColumns = ["id"],
            childColumns = ["id_orario_assunzione"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("id_orario_assunzione"),
        Index(value = ["id_orario_assunzione", "data"], unique = true)
    ]
)
data class AssunzionePrevista(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo("id_orario_assunzione")
    val idOrarioAssunzione: Int,

    val data: LocalDate,

    @ColumnInfo("orario_previsto")
    val orarioPrevisto: LocalTime,

    val stato: StatoAssunzione
)