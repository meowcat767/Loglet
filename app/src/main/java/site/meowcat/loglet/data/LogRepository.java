package site.meowcat.loglet.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogRepository {
    private final ActivityLogDao activityLogDao;
    private final TrackPointDao trackPointDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LogRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        activityLogDao = db.activityLogDao();
        trackPointDao = db.trackPointDao();
    }

    public void insert(ActivityLog log) {
        executor.execute(() -> activityLogDao.insert(log));
    }

    public void insertTrackPoint(TrackPoint point) {
        executor.execute(() -> trackPointDao.insert(point));
    }

    public LiveData<List<ActivityLog>> getAllLogs() {
        return activityLogDao.getAllLogs();
    }

    public LiveData<List<Long>> getAllTrackIds() {
        return trackPointDao.getAllTrackIds();
    }

    public LiveData<List<TrackPoint>> getPointsForTrack(long trackId) {
        return trackPointDao.getPointsForTrack(trackId);
    }

    public void clearAllLogs() {
        executor.execute(activityLogDao::clearAll);
    }
}
