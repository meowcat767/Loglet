package site.meowcat.loglet.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TrackPointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TrackPoint trackPoint);

    @Query("SELECT DISTINCT trackId FROM TrackPoint ORDER BY trackId DESC")
    LiveData<List<Long>> getAllTrackIds();

    @Query("SELECT * FROM TrackPoint WHERE trackId = :trackId ORDER BY timestamp ASC")
    LiveData<List<TrackPoint>> getPointsForTrack(long trackId);
}
