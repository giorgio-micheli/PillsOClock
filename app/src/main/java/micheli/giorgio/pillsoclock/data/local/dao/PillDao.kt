package micheli.giorgio.pillsoclock.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import micheli.giorgio.pillsoclock.data.local.entity.Pill

@Dao
interface PillDao {

    @Upsert
    suspend fun upsertPill(pill: Pill)
    @Delete
    suspend fun deletePill(pill: Pill)

    @Query("SELECT * FROM pills ORDER BY id")
    fun getPillsOrderedById(): Flow<List<Pill>>
}