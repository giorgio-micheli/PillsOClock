package micheli.giorgio.pillsoclock.data.local.mapper

import micheli.giorgio.pillsoclock.data.local.entity.AssunzioneEffettuataEntity
import micheli.giorgio.pillsoclock.domain.models.AssunzioneEffettuata

fun AssunzioneEffettuataEntity.toDomain(): AssunzioneEffettuata = AssunzioneEffettuata(
    id = id,
    idAssunzionePrevista = idAssunzionePrevista,
    idUtente = idUtente,
    timestampAssunzione = timestampAssunzione,
    note = note
)

fun AssunzioneEffettuata.toEntity(): AssunzioneEffettuataEntity = AssunzioneEffettuataEntity(
    id = id,
    idAssunzionePrevista = idAssunzionePrevista,
    idUtente = idUtente,
    timestampAssunzione = timestampAssunzione,
    note = note
)