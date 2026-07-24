package micheli.giorgio.pillsoclock.data.local.mapper

import micheli.giorgio.pillsoclock.data.local.entity.UtenteEntity
import micheli.giorgio.pillsoclock.domain.model.Utente

fun UtenteEntity.toDomain(): Utente = Utente(
    id = id,
    nome = nome,
    cognome = cognome,
    email = email,
    dataRegistrazione = dataRegistrazione
)

fun Utente.toEntity() = UtenteEntity(
    id = id,
    nome = nome,
    cognome = cognome,
    email = email,
    dataRegistrazione = dataRegistrazione
)