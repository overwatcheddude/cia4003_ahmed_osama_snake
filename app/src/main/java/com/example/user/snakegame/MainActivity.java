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
    General gen = new General();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get the context of this activity
        gen.SetContext(getApplicationContext());
    }

    public void OnActivityClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnStartGame:
                gen.GoToActivity(GameActivity.class); break;
            default: gen.DisplayMessage("Unknown button clicked.");
        }
    }
}
