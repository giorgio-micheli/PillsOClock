package micheli.giorgio.pillsoclock.data.local.entity

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromTipoFrequenza(value: TipoFrequenza): String = value.name

    @TypeConverter
    fun toTipoFrequenza(value: String): TipoFrequenza = TipoFrequenza.valueOf(value)

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime): String = value.toString()

    @TypeConverter
    fun toLocalDateTime(value: String): LocalDateTime = LocalDateTime.parse(value)

    @TypeConverter
    fun fromLocalDate(value: LocalDate): String = value.toString()

    @TypeConverter
    fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)

    @TypeConverter
    fun fromLocalTime(value: LocalTime): String = value.toString()

    @TypeConverter
    fun toLocalTime(value: String): LocalTime = LocalTime.parse(value)

    @TypeConverter
    fun fromStatoAssunzione(value: StatoAssunzione): String = value.name

    @TypeConverter
    fun toStatoAssunzione(value: String): StatoAssunzione = StatoAssunzione.valueOf(value)
}