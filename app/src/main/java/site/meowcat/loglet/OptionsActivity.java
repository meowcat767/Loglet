package site.meowcat.loglet;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import site.meowcat.loglet.data.LogRepository;

public class OptionsActivity extends AppCompatActivity {

    private LogRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);

        repository = new LogRepository(getApplicationContext());

        Button clearDataButton = findViewById(R.id.clear_data_button);
        clearDataButton.setOnClickListener(v -> {
            repository.clearAllLogs();
        });
    }
}
