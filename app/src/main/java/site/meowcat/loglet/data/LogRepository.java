package site.meowcat.loglet.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogRepository {
    private final ActivityLogDao activityLogDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LogRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        activityLogDao = db.activityLogDao();
    }

    public void insert(ActivityLog log) {
        executor.execute(() -> activityLogDao.insert(log));
    }

    public LiveData<List<ActivityLog>> getAllLogs() {
        return activityLogDao.getAllLogs();
    }
}