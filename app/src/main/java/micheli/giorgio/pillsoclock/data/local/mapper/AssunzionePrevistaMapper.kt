package micheli.giorgio.pillsoclock.data.local.mapper

import micheli.giorgio.pillsoclock.data.local.entity.AssunzionePrevistaEntity
import micheli.giorgio.pillsoclock.domain.model.AssunzionePrevista

fun AssunzionePrevistaEntity.toDomain(): AssunzionePrevista = AssunzionePrevista(
    id = id,
    idOrarioAssunzione = idOrarioAssunzione,
    data = data,
    orarioPrevisto = orarioPrevisto,
    stato = stato
)

fun AssunzionePrevista.toEntity(): AssunzionePrevistaEntity = AssunzionePrevistaEntity(
    id = id,
    idOrarioAssunzione = idOrarioAssunzione,
    data = data,
    orarioPrevisto = orarioPrevisto,
    stato = stato
)