package micheli.giorgio.pillsoclock.data.local.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import micheli.giorgio.pillsoclock.data.local.entity.AssunzioneEffettuataEntity
import micheli.giorgio.pillsoclock.data.local.entity.AssunzionePrevistaEntity
import micheli.giorgio.pillsoclock.data.local.entity.MedicinaleEntity
import micheli.giorgio.pillsoclock.data.local.entity.OrarioAssunzioneEntity
import micheli.giorgio.pillsoclock.data.local.entity.PianoAssunzioneEntity

data class MedicinaleConPianoEntity(
    @Embedded val medicinale: MedicinaleEntity,
    @Relation(parentColumn = "id", entityColumn = "id_medicinale")
    val piani: List<PianoAssunzioneEntity>
)

data class PianoConOrariEntity(
    @Embedded val piano: PianoAssunzioneEntity,
    @Relation(parentColumn = "id", entityColumn = "id_piano_assunzione")
    val orari: List<OrarioAssunzioneEntity>
)

data class MedicinaleConPianoEOrariEntity(
    @Embedded val medicinale: MedicinaleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id_medicinale",
        entity = PianoAssunzioneEntity::class
    )
    val piani: List<PianoConOrariEntity>
)

data class AssunzioneGiornalieraEntity(
    @Embedded val assunzionePrevista: AssunzionePrevistaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id_assunzione_prevista"
    )
    val assunzioneEffettuata: AssunzioneEffettuataEntity?,
    val nomeMedicinale: String,
    val dosaggio: String?
)