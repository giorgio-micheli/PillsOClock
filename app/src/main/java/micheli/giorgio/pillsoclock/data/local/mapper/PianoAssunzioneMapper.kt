package micheli.giorgio.pillsoclock.data.local.mapper

import micheli.giorgio.pillsoclock.data.local.entity.PianoAssunzioneEntity
import micheli.giorgio.pillsoclock.domain.model.PianoAssunzione

fun PianoAssunzioneEntity.toDomain(): PianoAssunzione = PianoAssunzione(
    id = id,
    idMedicinale = idMedicinale,
    tipoFrequenza = tipoFrequenza,
    intervalloGiorni = intervalloGiorni,
    giorniSettimana = giorniSettimana?.split(",")?.map { it.toInt() },
    dataInizio = dataInizio,
    dataFine = dataFine
)

fun PianoAssunzione.toEntity(): PianoAssunzioneEntity = PianoAssunzioneEntity(
    id = id,
    idMedicinale = idMedicinale,
    tipoFrequenza = tipoFrequenza,
    intervalloGiorni = intervalloGiorni,
    giorniSettimana = giorniSettimana?.joinToString(","),
    dataInizio = dataInizio,
    dataFine = dataFine
)