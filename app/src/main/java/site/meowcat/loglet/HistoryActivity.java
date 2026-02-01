package site.meowcat.loglet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import site.meowcat.loglet.data.ActivityLog;
import site.meowcat.loglet.data.LogViewModel;

public class HistoryActivity extends AppCompatActivity {
    private LogViewModel viewModel;
    private HistoryAdapter adapter;
    private ExpandableListView expandableListView;
    private List<String> listDataHeader;
    private Map<String, List<ActivityLog>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        viewModel = new ViewModelProvider(this).get(LogViewModel.class);

        expandableListView = findViewById(R.id.expandableListView);

        viewModel.getAllLogs().observe(this, this::prepareDataAndSetAdapter);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            ActivityLog log = (ActivityLog) adapter.getChild(groupPosition, childPosition);
            showLogOptionsDialog(log);
            return true;
        });

        expandableListView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                showDeleteDayDialog(groupPosition);
                return true;
            }
            return false;
        });
    }

    private void prepareDataAndSetAdapter(List<ActivityLog> logs) {
        if (logs == null || logs.isEmpty()) {
            Toast.makeText(this, "No history to show.", Toast.LENGTH_SHORT).show();
            return;
        }

        listDataHeader = new ArrayList<>();
        listDataChild = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

        for (ActivityLog log : logs) {
            String dateKey = sdf.format(new Date(log.startTime));
            if (!listDataChild.containsKey(dateKey)) {
                listDataChild.put(dateKey, new ArrayList<>());
            }
            listDataChild.get(dateKey).add(log);
        }

        listDataHeader.addAll(listDataChild.keySet());
        Collections.sort(listDataHeader, (d1, d2) -> {
            try {
                Date date1 = sdf.parse(d1);
                Date date2 = sdf.parse(d2);
                return date2.compareTo(date1); // Sort descending
            } catch (ParseException e) {
                return 0;
            }
        });

        adapter = new HistoryAdapter(this, listDataHeader, listDataChild);
        expandableListView.setAdapter(adapter);
    }

    private void showLogOptionsDialog(ActivityLog log) {
        new AlertDialog.Builder(this)
                .setTitle("Log Options")
                .setItems(new CharSequence[]{"View Details", "Delete Log"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // View Details
                            Intent intent = new Intent(this, LogDetailActivity.class);
                            intent.putExtra("logId", log.id);
                            startActivity(intent);
                            break;
                        case 1: // Delete Log
                            viewModel.delete(log);
                            Toast.makeText(this, "Log deleted", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .show();
    }

    private void showDeleteDayDialog(int groupPosition) {
        String dateStr = listDataHeader.get(groupPosition);
        new AlertDialog.Builder(this)
                .setTitle("Clear Day")
                .setMessage("Delete all logs for " + dateStr + "?")
                .setPositiveButton("Delete All", (d, w) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                        Date date = sdf.parse(dateStr);
                        long startTime = date.getTime();
                        long endTime = startTime + (24 * 60 * 60 * 1000);
                        viewModel.deleteLogsByDateRange(startTime, endTime);
                        Toast.makeText(this, "Cleared logs for " + dateStr, Toast.LENGTH_SHORT).show();
                    } catch (ParseException e) {
                        Toast.makeText(this, "Error deleting logs", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
