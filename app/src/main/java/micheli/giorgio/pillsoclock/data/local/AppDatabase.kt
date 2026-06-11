package micheli.giorgio.pillsoclock.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import micheli.giorgio.pillsoclock.data.local.dao.PillDao
import micheli.giorgio.pillsoclock.data.local.entity.Pill

@Database(
    entities = [Pill::class],
    version = 1
)
abstract class AppDatabase: RoomDatabase() {
    abstract val dao: PillDao
}