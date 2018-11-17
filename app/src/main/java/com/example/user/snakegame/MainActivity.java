package com.example.user.snakegame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void OnActivityClick(View v)
    {
        General gen = new General(getApplicationContext());
        switch (v.getId())
        {
            case R.id.btnStartGame:
                gen.GoToActivity(GameActivity.class); break;
            case R.id.btnHighScores:
                gen.GoToActivity(HighScores.class); break;
            case R.id.btnProfile:
                gen.GoToActivity(profile.class); break;
            default: gen.DisplayMessage("Unknown button clicked.");
        }
    }
}
