package com.example.user.snakegame;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class General
{   //Context is required in order to use method like DisplayMessage and startactivity
    private Context context;

    public General(Context context)
    {
        this.context = context;
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
