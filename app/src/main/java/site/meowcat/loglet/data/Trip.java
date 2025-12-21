package site.meowcat.loglet.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Trip {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public long startTime;
    public long endTime;

    public Trip(String name, long startTime) {
        this.name = name;
        this.startTime = startTime;
    }
}
