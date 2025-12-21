package site.meowcat.loglet.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TripDao {
    @Insert
    long insert(Trip trip);

    @Update
    void update(Trip trip);

    @Query("SELECT * FROM Trip ORDER BY startTime DESC")
    LiveData<List<Trip>> getAllTrips();

    @Query("SELECT * FROM Trip WHERE id = :id")
    Trip getTripById(int id);
}
