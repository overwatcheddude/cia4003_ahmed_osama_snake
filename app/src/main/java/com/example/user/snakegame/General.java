package com.example.user.snakegame;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class General
{
    private Context context;

    //Context is required in order to use method like DisplayMessage and startactivity
    public void SetContext(Context con)
    {
        context = con;
    }

    public void DisplayMessage(String msg)
    {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public void GoToActivity(Class nextActivity)
    {
        Intent i = new Intent(context, nextActivity);
        context.startActivity(i);
    }
}
