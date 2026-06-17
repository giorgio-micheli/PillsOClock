package micheli.giorgio.pillsoclock.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class TipoFrequenza {
    GIORNALIERA,
    OGNI_N_GIORNI,
    GIORNI_SETTIMANA
}

/**
 * Entità relativa ai piani delle assunzioni di ogni pasticca creati dall'utente.
 * Ogni record in questa tabella rappresenta la frequenza di assunzione di un medicinale presente
 * nella tabella 'medicinali'.
 * Nel caso in cui l'utente scelga come frequenza il valore 'GIORNALIERA', i campi 'intervallo_giorni'
 * e 'giorni_settimana' avranno valore NULL.
 * Nel caso in cui l'utente scelga come frequenza il valore 'OGNI_N_GIORNI' solo il campo 'giorni_settimana'
 * avrà valore NULL.
 * Nel caso in cui l'utente scelga come frequenza il valore 'GIORNI_SETTIMANA' solo il campo 'intervallo_giorni'
 * avrà valore NULL.
 */
@Entity(
    tableName = "piani_assunzioni",
    foreignKeys = [
        ForeignKey(
            entity = MedicinaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_medicinale"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("id_medicinale")]
)
data class PianoAssunzioneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo("id_medicinale")
    val idMedicinale: Int,

    @ColumnInfo("tipo_frequenza")
    val tipoFrequenza: TipoFrequenza,

    @ColumnInfo("intervallo_giorni")
    val intervalloGiorni: Int?,

    @ColumnInfo("giorni_settimana")
    val giorniSettimana: String?,

    @ColumnInfo("data_inizio")
    val dataInizio: LocalDate
)