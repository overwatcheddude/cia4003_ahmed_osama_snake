package com.example.user.snakegame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class GameActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener
{
    // Declare an instance of SnakeEngine
    SnakeEngine snakeEngine;
    public GestureDetector myGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Get the pixel dimensions of the screen
        Display display = getWindowManager().getDefaultDisplay();

        // Initialize the result into a Point object
        Point size = new Point();
        display.getSize(size);

        // Create a new instance of the SnakeEngine class
        snakeEngine = new SnakeEngine(this, size);

        // Make snakeEngine the view of the Activity
        setContentView(snakeEngine);

        snakeEngine.setOnTouchListener(this);
        myGestureDetector = new GestureDetector(this, this);
    }

    // Start the thread in snakeEngine
    @Override
    protected void onResume()
    {
        super.onResume();
        snakeEngine.resume();
    }
    // Stop the thread in snakeEngine
    @Override
    protected void onPause()
    {
        super.onPause();
        snakeEngine.pause();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        myGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent downEvent, MotionEvent moveEvent, float velocityX, float velocityY) {
        float diffY = moveEvent.getY() - downEvent.getY();
        float diffX = moveEvent.getX() - downEvent.getX();
        //Which was greater? Movement across Y or X?
        if (Math.abs(diffX) > Math.abs(diffY))
        {
            //Right or left swipe.
            if (Math.abs(diffX) > 25 && Math.abs(velocityX) > 25)
            {
                if (diffX > 0)
                {
                    //Swipe is right.
                    snakeEngine.movement = SnakeEngine.Moving.RIGHT;
                }
                else
                {
                    //Swipe is left.
                    snakeEngine.movement = SnakeEngine.Moving.LEFT;
                }
            }
        }
        else
        {
            //Up or down swipe.
            if (Math.abs(diffY) > 25 && Math.abs(velocityY) > 25)
            {
                if (diffY > 0)
                {
                    //Swipe is down.
                    snakeEngine.movement = SnakeEngine.Moving.DOWN;
                }
                else
                {
                    //Swipe is up.
                    snakeEngine.movement = SnakeEngine.Moving.UP;
                }
            }
        }
        return true;
    }
}