package com.example.user.snakegame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class HighScores extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);
    }

    public void BackToMenu(View v)
    {
        General gen = new General(getApplicationContext());
        gen.GoToActivity(MainActivity.class);
    }
}
