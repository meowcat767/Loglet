package site.meowcat.loglet.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TrackPoint {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long trackId;
    public double latitude;
    public double longitude;
    public long timestamp;

    public TrackPoint(long trackId, double latitude, double longitude, long timestamp) {
        this.trackId = trackId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}
