package micheli.giorgio.pillsoclock.data.local.mapper

import micheli.giorgio.pillsoclock.data.local.entity.relations.AssunzioneGiornalieraEntity
import micheli.giorgio.pillsoclock.data.local.entity.relations.MedicinaleConPianoEOrariEntity
import micheli.giorgio.pillsoclock.data.local.entity.relations.PianoConOrariEntity
import micheli.giorgio.pillsoclock.domain.model.AssunzioneGiornaliera
import micheli.giorgio.pillsoclock.domain.model.AssunzionePrevistaConEffettuata
import micheli.giorgio.pillsoclock.domain.model.MedicinaleConPianoEOrari
import micheli.giorgio.pillsoclock.domain.model.PianoConOrari

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

fun AssunzioneGiornalieraEntity.toDomain(): AssunzioneGiornaliera =
    AssunzioneGiornaliera(
        assunzionePrevista = assunzionePrevista.toDomain(),
        assunzioneEffettuata = assunzioneEffettuata?.toDomain(),
        nomeMedicinale = nomeMedicinale,
        dosaggio = dosaggio
    )
