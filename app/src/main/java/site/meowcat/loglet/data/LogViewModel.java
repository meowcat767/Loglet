package site.meowcat.loglet.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class LogViewModel {
    private final LogRepository repository;
    private final LiveData<List<ActivityLog>> allLogs;

    public LogViewModel(Application application) {
        super();
        repository = new LogRepository(application);
        allLogs = repository.getAllLogs();
    }

    public LiveData<List<ActivityLog>> getAllLogs() { return allLogs; }
    public void insert(ActivityLog log) {repository.insert(log); }
}
