package site.meowcat.loglet.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ActivityLog {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String label;
    public long startTime;
    
    // New fields for lifelogging
    public double latitude;
    public double longitude;
    public String imageUri;
    public String note;

    public ActivityLog(String label, long startTime) {
        this.label = label;
        this.startTime = startTime;
    }
}
