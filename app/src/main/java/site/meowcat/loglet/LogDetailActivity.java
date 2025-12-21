package site.meowcat.loglet;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogDetailActivity extends AppCompatActivity {

    private MapView map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Required for OSMDroid
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_log_detail);

        String label = getIntent().getStringExtra("label");
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);
        long time = getIntent().getLongExtra("time", 0);

        TextView txtLabel = findViewById(R.id.txtDetailLabel);
        TextView txtTime = findViewById(R.id.txtDetailTime);
        TextView txtLocation = findViewById(R.id.txtDetailLocation);

        txtLabel.setText(label);
        txtTime.setText(new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date(time)));
        txtLocation.setText(String.format(Locale.getDefault(), "Location: %.4f, %.4f", lat, lng));

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        GeoPoint startPoint = new GeoPoint(lat, lng);
        map.getController().setZoom(18.0);
        map.getController().setCenter(startPoint);

        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle(label);
        map.getOverlays().add(startMarker);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
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
