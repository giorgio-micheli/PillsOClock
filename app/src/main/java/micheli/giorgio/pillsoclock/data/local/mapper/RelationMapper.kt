package micheli.giorgio.pillsoclock.data.local.mapper

import micheli.giorgio.pillsoclock.data.local.entity.relations.AssunzionePrevistaConEffettuataEntity
import micheli.giorgio.pillsoclock.data.local.entity.relations.MedicinaleConPianoEOrariEntity
import micheli.giorgio.pillsoclock.data.local.entity.relations.PianoConOrariEntity
import micheli.giorgio.pillsoclock.domain.models.AssunzionePrevistaConEffettuata
import micheli.giorgio.pillsoclock.domain.models.MedicinaleConPianoEOrari
import micheli.giorgio.pillsoclock.domain.models.PianoConOrari

/*
    Mapper da Entity verso Domain, perchè queste Entity sono di sola lettura.
    Vengono generate da Room e per inserire i dati si utilizzano le altre entity singole
 */
fun PianoConOrariEntity.toDomain(): PianoConOrari = PianoConOrari(
    piano = piano.toDomain(),
    orari = orari.map { it.toDomain() }
)

fun MedicinaleConPianoEOrariEntity.toDomain(): MedicinaleConPianoEOrari = MedicinaleConPianoEOrari(
    medicinale = medicinale.toDomain(),
    piani = piani.map { it.toDomain() }
)

fun AssunzionePrevistaConEffettuataEntity.toDomain(): AssunzionePrevistaConEffettuata =
    AssunzionePrevistaConEffettuata(
        assunzionePrevista = assunzionePrevista.toDomain(),
        assunzioneEffettuata = assunzioneEffettuata?.toDomain()
    )
