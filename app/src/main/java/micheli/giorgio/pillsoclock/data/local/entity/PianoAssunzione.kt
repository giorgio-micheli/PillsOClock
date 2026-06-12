package micheli.giorgio.pillsoclock.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TipoFrequenza {
    GIORNALIERA,
    OGNI_N_GIORNI,
    GIORNI_SETTIMANA
}

@Entity(
    tableName = "piani_assunzioni",
    foreignKeys = [
        ForeignKey(
            entity = Medicinale::class,
            parentColumns = ["id"],
            childColumns = ["id_medicinale"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("id_medicinale")]
)
data class PianoAssunzione(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo("id_medicinale")
    val idMedicinale: Int,

    @ColumnInfo("tipo_frequenza")
    val tipoFrequenza: Int,

    @ColumnInfo("intervallo_giorni")
    val intervalloGiorni: Int?,

    @ColumnInfo("giorni_settimana")
    val giorniSettimana: String?
)