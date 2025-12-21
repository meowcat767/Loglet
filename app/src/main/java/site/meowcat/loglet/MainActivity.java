package site.meowcat.loglet;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import site.meowcat.loglet.data.AppDatabase;
import site.meowcat.loglet.data.Trip;
import site.meowcat.loglet.viewmodel.LogViewModel;

public class MainActivity extends AppCompatActivity {

    private MapView map = null;
    private MyLocationNewOverlay locationOverlay;
    private Polyline trackLine;
    private boolean isTracking = false;
    private long currentTripId = -1;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                
                if (fineLocationGranted || coarseLocationGranted) {
                    initMap();
                } else {
                    Toast.makeText(this, "Location permission required for map and tracking", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Required for OSMDroid
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_main_map);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            initMap();
        }

        setupButtons();
    }

    private void initMap() {
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        
        // Default center
        map.getController().setCenter(new GeoPoint(51.505, -0.09));

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        map.getOverlays().add(locationOverlay);

        trackLine = new Polyline();
        trackLine.getOutlinePaint().setColor(android.graphics.Color.BLUE);
        trackLine.getOutlinePaint().setStrokeWidth(12f);
        map.getOverlays().add(trackLine);

        observeLogs();
    }

    private void setupButtons() {
        Button btnToggle = findViewById(R.id.btnToggleTracking);
        FloatingActionButton fabLog = findViewById(R.id.fabAddLog);
        Button btnHistory = findViewById(R.id.btnHistory);

        btnToggle.setOnClickListener(v -> {
            if (!isTracking) {
                showStartTripDialog();
            } else {
                stopTracking();
            }
        });

        fabLog.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateLogActivity.class);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });
    }

    private void showStartTripDialog() {
        EditText input = new EditText(this);
        input.setHint("Trip Name");
        new AlertDialog.Builder(this)
                .setTitle("Start New Trip")
                .setView(input)
                .setPositiveButton("Start", (dialog, which) -> {
                    String name = input.getText().toString();
                    if (name.isEmpty()) name = "Unnamed Trip";
                    startTracking(name);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startTracking(String name) {
        executor.execute(() -> {
            Trip trip = new Trip(name, System.currentTimeMillis());
            currentTripId = AppDatabase.getInstance(this).tripDao().insert(trip);
            
            runOnUiThread(() -> {
                isTracking = true;
                findViewById(R.id.btnToggleTracking).setBackgroundColor(android.graphics.Color.RED);
                ((Button)findViewById(R.id.btnToggleTracking)).setText("Stop Trip");
                
                Intent serviceIntent = new Intent(this, TrackingService.class);
                serviceIntent.putExtra("tripId", currentTripId);
                ContextCompat.startForegroundService(this, serviceIntent);
                
                observeCurrentTrack();
            });
        });
    }

    private void stopTracking() {
        isTracking = false;
        findViewById(R.id.btnToggleTracking).setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
        ((Button)findViewById(R.id.btnToggleTracking)).setText("Start Trip");
        
        Intent serviceIntent = new Intent(this, TrackingService.class);
        stopService(serviceIntent);
        
        executor.execute(() -> {
            Trip trip = AppDatabase.getInstance(this).tripDao().getTripById((int)currentTripId);
            if (trip != null) {
                trip.endTime = System.currentTimeMillis();
                AppDatabase.getInstance(this).tripDao().update(trip);
            }
            currentTripId = -1;
        });
        
        trackLine.setPoints(new ArrayList<>());
        map.invalidate();
    }

    private void observeCurrentTrack() {
        AppDatabase.getInstance(this).trackPointDao().getPointsForTrack(currentTripId).observe(this, points -> {
            if (isTracking && points != null) {
                List<GeoPoint> geoPoints = new ArrayList<>();
                for (site.meowcat.loglet.data.TrackPoint p : points) {
                    geoPoints.add(new GeoPoint(p.latitude, p.longitude));
                }
                trackLine.setPoints(geoPoints);
                map.invalidate();
            }
        });
    }

    private void observeLogs() {
        LogViewModel viewModel = new ViewModelProvider(this).get(LogViewModel.class);
        viewModel.getLogs().observe(this, logs -> {
            if (logs != null) {
                map.getOverlays().removeIf(o -> o instanceof Marker && !(o.equals(locationOverlay)));
                for (site.meowcat.loglet.data.ActivityLog log : logs) {
                    Marker marker = new Marker(map);
                    marker.setPosition(new GeoPoint(log.latitude, log.longitude));
                    marker.setTitle(log.label);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    map.getOverlays().add(marker);
                }
                map.invalidate();
            }
        });
    }

    @Override
    public void onResume() { super.onResume(); if (map != null) map.onResume(); }
    @Override
    public void onPause() { super.onPause(); if (map != null) map.onPause(); }
}
