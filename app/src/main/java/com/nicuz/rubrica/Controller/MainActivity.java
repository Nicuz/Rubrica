package com.nicuz.rubrica.Controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.nicuz.rubrica.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    @Override
    public void onBackPressed() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        EditText searchbar = findViewById(R.id.searchbar);

        if (toolbar != null && toolbar.getVisibility() == View.GONE) {
            searchbar.setText(null);
            toolbar.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
