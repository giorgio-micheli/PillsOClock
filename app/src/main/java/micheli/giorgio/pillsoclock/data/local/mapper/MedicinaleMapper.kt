package micheli.giorgio.pillsoclock.data.local.mapper

import micheli.giorgio.pillsoclock.data.local.entity.MedicinaleEntity
import micheli.giorgio.pillsoclock.domain.model.Medicinale

fun MedicinaleEntity.toDomain(): Medicinale = Medicinale(
    id = id,
    idUtente = idUtente,
    nome = nome,
    dosaggio = dosaggio,
    note = note,
    attivo = attivo,
    dataInizio = dataInizio,
    dataFine = dataFine
)

fun Medicinale.toEntity(): MedicinaleEntity = MedicinaleEntity(
    id = id,
    idUtente = idUtente,
    nome = nome,
    dosaggio = dosaggio,
    note = note,
    attivo = attivo,
    dataInizio = dataInizio,
    dataFine = dataFine
)