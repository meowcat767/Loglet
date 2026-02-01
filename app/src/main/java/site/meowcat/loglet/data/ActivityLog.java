package site.meowcat.loglet.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "activity_logs") // Naming the table explicitly is better practice
public class ActivityLog {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String label;
    public long startTime;

    // Using Double objects allows them to be null if GPS is off
    public Double latitude;
    public Double longitude;

    public String imageUri;
    public String note;

    // Room needs one constructor it can use.
    // If you have multiple, use @Ignore on the ones Room should skip.
    public ActivityLog(String label, long startTime) {
        this.label = label;
        this.startTime = startTime;
    }

}