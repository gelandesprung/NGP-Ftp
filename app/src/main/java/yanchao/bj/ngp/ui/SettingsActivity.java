package yanchao.bj.ngp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import yanchao.bj.ngp.R;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences perference;
    private EditText usernameET;
    private EditText passwordET;
    private EditText portET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        perference = getSharedPreferences("default", MODE_PRIVATE);

        usernameET = (EditText) findViewById(R.id.username_edit);
        passwordET = (EditText) findViewById(R.id.password_edit);
        portET = (EditText) findViewById(R.id.port_edit);

        ImageView back = (ImageView) findViewById(R.id.cancel);
        TextView save = (TextView) findViewById(R.id.save);
        back.setOnClickListener(this);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.save:
                String username = usernameET.getText().toString();
                String password = passwordET.getText().toString();
                String port = portET.getText().toString();
                if (!username.isEmpty() && !password.isEmpty() && !port.isEmpty()) {
                    SharedPreferences.Editor editor = perference.edit();
                    editor.putString("username", username);
                    editor.putString("password", password);
                    editor.putInt("port", Integer.parseInt(port));
                    editor.apply();
                    finish();
                }
                break;
            default:
                break;
        }
    }
}
