package micheli.giorgio.pillsoclock.data.local.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import micheli.giorgio.pillsoclock.data.local.entity.AssunzioneEffettuata
import micheli.giorgio.pillsoclock.data.local.entity.AssunzionePrevista
import micheli.giorgio.pillsoclock.data.local.entity.Medicinale
import micheli.giorgio.pillsoclock.data.local.entity.OrarioAssunzione
import micheli.giorgio.pillsoclock.data.local.entity.PianoAssunzione

data class MedicinaleConPiano(
    @Embedded val medicinale: Medicinale,
    @Relation(parentColumn = "id", entityColumn = "id_medicinale")
    val piani: List<PianoAssunzione>
)

data class PianoConOrari(
    @Embedded val piano: PianoAssunzione,
    @Relation(parentColumn = "id", entityColumn = "id_piano_assunzione")
    val orari: List<OrarioAssunzione>
)

data class MedicinaleConPianoEOrari(
    @Embedded val medicinale: Medicinale,
    @Relation(
        parentColumn = "id",
        entityColumn = "id_medicinale",
        entity = PianoAssunzione::class
    )
    val piani: List<PianoConOrari>
)

data class AssunzionePrevistaConEffettuata(
    @Embedded val assunzionePrevista: AssunzionePrevista,
    @Relation(
        parentColumn = "id",
        entityColumn = "id_assunzione_prevista"
    )
    val assunzioneEffettuata: AssunzioneEffettuata?
)