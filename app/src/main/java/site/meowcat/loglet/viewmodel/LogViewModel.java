package site.meowcat.loglet.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import site.meowcat.loglet.data.ActivityLog;
import site.meowcat.loglet.data.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogViewModel extends AndroidViewModel {

    private final LiveData<List<ActivityLog>> logs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LogViewModel(@NonNull Application application) {
        super(application);
        logs = AppDatabase.getInstance(application)
                .activityLogDao()
                .getAllLogs();
    }

    public LiveData<List<ActivityLog>> getLogs() {
        return logs;
    }

    public void logActivity(String label, double lat, double lng) {
        executor.execute(() -> {
            ActivityLog log = new ActivityLog(label, System.currentTimeMillis());
            log.latitude = lat;
            log.longitude = lng;
            AppDatabase.getInstance(getApplication())
                    .activityLogDao()
                    .insert(log);
        });
    }

    public void deleteLog(ActivityLog log) {
        executor.execute(() -> {
            AppDatabase.getInstance(getApplication())
                    .activityLogDao()
                    .delete(log);
        });
    }

    public void deleteLogsByDateRange(long start, long end) {
        executor.execute(() -> {
            AppDatabase.getInstance(getApplication())
                    .activityLogDao()
                    .deleteLogsByDateRange(start, end);
        });
    }

    public void clearAllLogs() {
        executor.execute(() -> {
            AppDatabase.getInstance(getApplication())
                    .activityLogDao()
                    .clearAll();
        });
    }
}
