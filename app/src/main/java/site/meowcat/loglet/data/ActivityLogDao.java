package site.meowcat.loglet.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ActivityLogDao {
    // Adding OnConflictStrategy prevents crashes if a log with the same ID is inserted
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ActivityLog activityLog);

    @Query("SELECT * FROM activity_logs WHERE startTime >= :start AND startTime < :end ORDER BY startTime DESC")
    LiveData<List<ActivityLog>> getLogsByDateRange(long start, long end);

    @Query("SELECT * FROM activity_logs ORDER BY startTime DESC")
    LiveData<List<ActivityLog>> getAllLogs();

    @Delete
    void delete(ActivityLog log);

    // It's safer to return an int to see how many rows were actually deleted
    @Query("DELETE FROM activity_logs WHERE startTime >= :start AND startTime < :end")
    int deleteLogsByDateRange(long start, long end);

    @Query("DELETE FROM activity_logs")
    void clearAll();
}
