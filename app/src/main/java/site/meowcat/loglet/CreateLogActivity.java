package site.meowcat.loglet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import site.meowcat.loglet.viewmodel.LogViewModel;

public class CreateLogActivity extends AppCompatActivity {
    private LogViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText editCustomLog;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (!result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_log);

        viewModel = new ViewModelProvider(this).get(LogViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        editCustomLog = findViewById(R.id.editCustomLog);
        Button btnSave = findViewById(R.id.btnSaveLog);

        btnSave.setOnClickListener(v -> {
            String label = editCustomLog.getText().toString().trim();

            if (!label.isEmpty()) {
                Toast.makeText(this, "Saving log...", Toast.LENGTH_SHORT).show();
                logWithLocation(label);
            } else {
                Toast.makeText(this, "Please enter an activity name", Toast.LENGTH_SHORT).show();
            }
        });

        checkLocationPermissions();
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void logWithLocation(String label) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        saveAndExit(label, location != null ? location.getLatitude() : 0, location != null ? location.getLongitude() : 0);
                    })
                    .addOnFailureListener(e -> {
                        fusedLocationClient.getLastLocation().addOnSuccessListener(loc -> {
                            saveAndExit(label, loc != null ? loc.getLatitude() : 0, loc != null ? loc.getLongitude() : 0);
                        });
                    });
        } else {
            saveAndExit(label, 0, 0);
        }
    }

    private void saveAndExit(String label, double lat, double lng) {
        viewModel.logActivity(label, lat, lng);
        Toast.makeText(this, "Logged: " + label, Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
