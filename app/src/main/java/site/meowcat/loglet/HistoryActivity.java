package site.meowcat.loglet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import site.meowcat.loglet.data.ActivityLog;
import site.meowcat.loglet.viewmodel.LogViewModel;

public class HistoryActivity extends AppCompatActivity implements LogAdapter.OnLogClickListener {
    private LogViewModel viewModel;
    private LogAdapter adapter;
    private TextView txtDateHeader;
    private long selectedDateStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        viewModel = new ViewModelProvider(this).get(LogViewModel.class);

        txtDateHeader = findViewById(R.id.txtDateHeader);
        ImageView imgCalendar = findViewById(R.id.imgCalendar);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewLogs);

        adapter = new LogAdapter(this);
        recyclerView.setAdapter(adapter);

        // Default to today
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        updateDateSelection(cal.getTimeInMillis());

        imgCalendar.setOnClickListener(v -> showDatePicker());
        txtDateHeader.setOnClickListener(v -> showDatePicker());

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        
        // Long click to delete the entire day
        txtDateHeader.setOnLongClickListener(v -> {
            showDeleteDayDialog();
            return true;
        });
    }

    private void updateDateSelection(long timeInMillis) {
        selectedDateStart = timeInMillis;
        long endOfDay = selectedDateStart + (24 * 60 * 60 * 1000);

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        txtDateHeader.setText(sdf.format(new Date(selectedDateStart)));

        viewModel.getLogs().observe(this, logs -> {
            if (logs != null) {
                List<ActivityLog> filtered = new ArrayList<>();
                for (ActivityLog log : logs) {
                    if (log.startTime >= selectedDateStart && log.startTime < endOfDay) {
                        filtered.add(log);
                    }
                }
                adapter.setLogs(filtered);
            }
        });
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(selectedDateStart)
                .build();

        picker.addOnPositiveButtonClickListener(this::updateDateSelection);
        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    @Override
    public void onLogClick(ActivityLog log) {
        new AlertDialog.Builder(this)
                .setTitle("Log Options")
                .setMessage("Choose an action for this log.")
                .setPositiveButton("View on Map", (d, w) -> {
                    Intent intent = new Intent(this, LogDetailActivity.class);
                    intent.putExtra("label", log.label);
                    intent.putExtra("lat", log.latitude);
                    intent.putExtra("lng", log.longitude);
                    intent.putExtra("time", log.startTime);
                    startActivity(intent);
                })
                .setNegativeButton("Delete Log", (d, w) -> {
                    viewModel.deleteLog(log);
                    Toast.makeText(this, "Log deleted", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void showDeleteDayDialog() {
        String dateStr = new SimpleDateFormat("MMMM dd", Locale.getDefault()).format(new Date(selectedDateStart));
        new AlertDialog.Builder(this)
                .setTitle("Clear Day")
                .setMessage("Delete all logs for " + dateStr + "?")
                .setPositiveButton("Delete All", (d, w) -> {
                    long endOfDay = selectedDateStart + (24 * 60 * 60 * 1000);
                    viewModel.deleteLogsByDateRange(selectedDateStart, endOfDay);
                    Toast.makeText(this, "Cleared logs for " + dateStr, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
