package site.meowcat.loglet.data;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LogViewModel extends AndroidViewModel {
    private final LogRepository repository;
    private final LiveData<List<ActivityLog>> allLogs;

    public LogViewModel(Application application) {
        super(application);
        repository = new LogRepository(application);
        allLogs = repository.getAllLogs();
    }

    public LiveData<List<ActivityLog>> getAllLogs() { return allLogs; }

    public LiveData<List<Long>> getAllTrackIds() {
        return repository.getAllTrackIds();
    }

    public void insert(ActivityLog log) {repository.insert(log); }

    // use a switchmap to get points whenever id changes
    public LiveData<List<TrackPoint>> getPointsForTrack(long trackId) {
        return repository.getPointsForTrack(trackId);
    }
}
