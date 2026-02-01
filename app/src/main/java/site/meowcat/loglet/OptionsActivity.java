package site.meowcat.loglet;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import site.meowcat.loglet.data.LogRepository;
import site.meowcat.loglet.data.LogViewModel;
import site.meowcat.loglet.data.TrackPoint;

public class OptionsActivity extends AppCompatActivity {

    private LogRepository repository;
    private Switch unitSwitch;
    private TextView totalDistanceValue;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);

        repository = new LogRepository(getApplicationContext());
        sharedPreferences = getSharedPreferences("LogletPrefs", MODE_PRIVATE);

        unitSwitch = findViewById(R.id.unit_switch);
        totalDistanceValue = findViewById(R.id.total_distance_value);

        boolean useKilometers = sharedPreferences.getBoolean("useKilometers", false);
        unitSwitch.setChecked(useKilometers);

        LogViewModel viewModel = new ViewModelProvider(this).get(LogViewModel.class);
        viewModel.getAllTrackPoints().observe(this, trackPoints -> {
            if (trackPoints != null) {
                updateTotalDistance(trackPoints);
            }
        });

        unitSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("useKilometers", isChecked).apply();
            List<TrackPoint> currentPoints = viewModel.getAllTrackPoints().getValue();
            if (currentPoints != null) {
                updateTotalDistance(currentPoints);
            }
        });

        Button clearDataButton = findViewById(R.id.clear_data_button);
        clearDataButton.setOnClickListener(v -> {
            repository.clearAllLogs();
            repository.clearAllTrackPoints();
        });
    }

    private void updateTotalDistance(List<TrackPoint> trackPoints) {
        float totalDistance = calculateTotalDistance(trackPoints);
        boolean useKilometers = sharedPreferences.getBoolean("useKilometers", false);

        if (useKilometers) {
            float distanceInKm = totalDistance / 1000;
            totalDistanceValue.setText(String.format("%.2f km", distanceInKm));
        } else {
            float distanceInMiles = totalDistance * 0.000621371f;
            totalDistanceValue.setText(String.format("%.2f mi", distanceInMiles));
        }
    }

    private float calculateTotalDistance(List<TrackPoint> trackPoints) {
        float totalDistance = 0;
        if (trackPoints == null || trackPoints.isEmpty()) {
            return 0;
        }

        Map<Long, List<TrackPoint>> tracks = new HashMap<>();
        for (TrackPoint p : trackPoints) {
            List<TrackPoint> track = tracks.get(p.trackId);
            if (track == null) {
                track = new ArrayList<>();
                tracks.put(p.trackId, track);
            }
            track.add(p);
        }

        for (List<TrackPoint> track : tracks.values()) {
            if (track.size() > 1) {
                for (int i = 0; i < track.size() - 1; i++) {
                    TrackPoint p1 = track.get(i);
                    TrackPoint p2 = track.get(i + 1);
                    float[] results = new float[1];
                    Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results);
                    totalDistance += results[0];
                }
            }
        }
        return totalDistance;
    }
}
