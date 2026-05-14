package mx.hmng.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mx.hmng.app.data.local.entity.PendingOperationEntity

@Dao
interface PendingOperationDao {

    @Query("SELECT * FROM pending_operations ORDER BY created_at ASC")
    suspend fun getAll(): List<PendingOperationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: PendingOperationEntity)

    @Query("DELETE FROM pending_operations WHERE id=:id")
    suspend fun delete(id: Int)

    @Query("UPDATE pending_operations SET retry_count = retry_count + 1 WHERE id=:id")
    suspend fun incrementRetry(id: Int)
}
