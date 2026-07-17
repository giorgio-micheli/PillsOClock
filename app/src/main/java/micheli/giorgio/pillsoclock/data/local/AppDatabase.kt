package micheli.giorgio.pillsoclock.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import micheli.giorgio.pillsoclock.data.local.dao.AssunzioniEffettuateDao
import micheli.giorgio.pillsoclock.data.local.dao.AssunzioniPrevisteDao
import micheli.giorgio.pillsoclock.data.local.dao.MedicinaliDao
import micheli.giorgio.pillsoclock.data.local.dao.OrariAssunzioniDao
import micheli.giorgio.pillsoclock.data.local.dao.PianiAssunzioniDao
import micheli.giorgio.pillsoclock.data.local.dao.UtentiDao
import micheli.giorgio.pillsoclock.data.local.entity.AssunzioneEffettuataEntity
import micheli.giorgio.pillsoclock.data.local.entity.AssunzionePrevistaEntity
import micheli.giorgio.pillsoclock.data.local.entity.Converters
import micheli.giorgio.pillsoclock.data.local.entity.MedicinaleEntity
import micheli.giorgio.pillsoclock.data.local.entity.OrarioAssunzioneEntity
import micheli.giorgio.pillsoclock.data.local.entity.PianoAssunzioneEntity
import micheli.giorgio.pillsoclock.data.local.entity.UtenteEntity

@Database(
    entities = [
        UtenteEntity::class,
        MedicinaleEntity::class,
        PianoAssunzioneEntity::class,
        OrarioAssunzioneEntity::class,
        AssunzionePrevistaEntity::class,
        AssunzioneEffettuataEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun utentiDao(): UtentiDao
    abstract fun medicinaliDao(): MedicinaliDao
    abstract fun pianiAssunzioniDao(): PianiAssunzioniDao
    abstract fun orariAssunzioniDao(): OrariAssunzioniDao
    abstract fun assunzioniPrevisteDao(): AssunzioniPrevisteDao
    abstract fun assunzioniEffettuateDao(): AssunzioniEffettuateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medicine_reminder.db"
                )
                .fallbackToDestructiveMigration(false)
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}