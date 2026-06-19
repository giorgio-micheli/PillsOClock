package micheli.giorgio.pillsoclock.data.local.mapper

import micheli.giorgio.pillsoclock.data.local.entity.OrarioAssunzioneEntity
import micheli.giorgio.pillsoclock.domain.model.OrarioAssunzione

fun OrarioAssunzioneEntity.toDomain(): OrarioAssunzione = OrarioAssunzione(
    id = id,
    idPianoAssunzione = idPianoAssunzione,
    orario = orario
)

fun OrarioAssunzione.toEntity(): OrarioAssunzioneEntity = OrarioAssunzioneEntity(
    id = id,
    idPianoAssunzione = idPianoAssunzione,
    orario = orario
)