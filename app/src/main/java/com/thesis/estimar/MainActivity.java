package com.thesis.estimar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Delayed navigation after 5 seconds
        new Handler().postDelayed(this::openMainActivity2, 5000);
    }

    public void openMainActivity2(){
        Intent intent  = new Intent(this, MainActivity2.class);
        startActivity(intent);
        finish(); // Optional: finish the current activity if you don't want to keep it in the back stack
    }
}