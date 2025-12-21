package site.meowcat.loglet.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ActivityLogDao {
    @Insert
    void insert(ActivityLog activityLog);

    @Query("SELECT * FROM ActivityLog WHERE startTime >= :start AND startTime < :end ORDER BY startTime DESC")
    LiveData<List<ActivityLog>> getLogsByDateRange(long start, long end);

    @Query("SELECT * FROM ActivityLog ORDER BY startTime DESC")
    LiveData<List<ActivityLog>> getAllLogs();

    @Delete
    void delete(ActivityLog log);

    @Query("DELETE FROM ActivityLog WHERE startTime >= :start AND startTime < :end")
    void deleteLogsByDateRange(long start, long end);

    @Query("DELETE FROM ActivityLog")
    void clearAll();
}
