package site.meowcat.loglet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import site.meowcat.loglet.data.ActivityLog;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    public interface OnLogClickListener {
        void onLogClick(ActivityLog log);
    }

    private List<ActivityLog> logs = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final OnLogClickListener listener;

    public LogAdapter(OnLogClickListener listener) {
        this.listener = listener;
    }

    public void setLogs(List<ActivityLog> logs) {
        this.logs = logs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        ActivityLog log = logs.get(position);
        holder.txtLabel.setText(log.label);
        holder.txtTime.setText(timeFormat.format(new Date(log.startTime)));
        holder.txtLocation.setVisibility(View.GONE); // Hidden by default, viewable on click

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLogClick(log);
            }
        });
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView txtLabel, txtTime, txtLocation;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLabel = itemView.findViewById(R.id.txtLabel);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtLocation = itemView.findViewById(R.id.txtLocation);
        }
    }
}
