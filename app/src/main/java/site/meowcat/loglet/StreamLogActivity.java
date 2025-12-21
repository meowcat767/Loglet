package site.meowcat.loglet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import site.meowcat.loglet.data.AppDatabase;
import site.meowcat.loglet.viewmodel.LogViewModel;

public class StreamLogActivity extends AppCompatActivity {

    private MapView map = null;
    private boolean isTracking = false;
    private Polyline trackLine;
    private long currentTrackId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_stream_log);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        trackLine = new Polyline();
        trackLine.getOutlinePaint().setColor(android.graphics.Color.BLUE);
        trackLine.getOutlinePaint().setStrokeWidth(10f);
        map.getOverlays().add(trackLine);

        Button btnToggle = findViewById(R.id.btnToggleTracking);
        TextView txtStatus = findViewById(R.id.txtStatus);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddLog);

        btnToggle.setOnClickListener(v -> {
            if (!isTracking) {
                startTracking();
                btnToggle.setText("Stop Tracking");
                txtStatus.setText("Tracking On");
            } else {
                stopTracking();
                btnToggle.setText("Start Tracking");
                txtStatus.setText("Tracking Off");
            }
            isTracking = !isTracking;
        });

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateLogActivity.class);
            startActivity(intent);
        });

        // Observe logs to show as pins
        LogViewModel viewModel = new ViewModelProvider(this).get(LogViewModel.class);
        viewModel.getLogs().observe(this, logs -> {
            if (logs != null) {
                // Clear existing markers (pins) but keep the line
                map.getOverlays().removeIf(overlay -> overlay instanceof Marker);
                for (site.meowcat.loglet.data.ActivityLog log : logs) {
                    if (log.latitude != 0 || log.longitude != 0) {
                        Marker marker = new Marker(map);
                        marker.setPosition(new GeoPoint(log.latitude, log.longitude));
                        marker.setTitle(log.label);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        map.getOverlays().add(marker);
                    }
                }
                map.invalidate();
            }
        });

        // Observe track points
        AppDatabase.getInstance(this).trackPointDao().getAllTrackIds().observe(this, ids -> {
            if (ids != null && !ids.isEmpty()) {
                long latestId = ids.get(0);
                AppDatabase.getInstance(this).trackPointDao().getPointsForTrack(latestId).observe(this, points -> {
                    if (points != null && !points.isEmpty()) {
                        List<GeoPoint> geoPoints = new ArrayList<>();
                        for (site.meowcat.loglet.data.TrackPoint p : points) {
                            geoPoints.add(new GeoPoint(p.latitude, p.longitude));
                        }
                        trackLine.setPoints(geoPoints);
                        if (!geoPoints.isEmpty()) {
                            map.getController().animateTo(geoPoints.get(geoPoints.size() - 1));
                        }
                        map.invalidate();
                    }
                });
            }
        });
    }

    private void startTracking() {
        Intent serviceIntent = new Intent(this, TrackingService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopTracking() {
        Intent serviceIntent = new Intent(this, TrackingService.class);
        stopService(serviceIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}
