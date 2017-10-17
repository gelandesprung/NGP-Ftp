package yanchao.bj.ngp.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import yanchao.bj.ngp.R;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences perference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        perference = getSharedPreferences("ftp_settings", MODE_PRIVATE);
    }

}
