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
    private GestureDetector myGestureDetector;

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
    public boolean onTouch(View v, MotionEvent event) {
        myGestureDetector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_UP:
                if (event.getX() >= snakeEngine.screenX / 2)
                {
                    switch(snakeEngine.movement)
                    {
                        case UP:
                            snakeEngine.movement = SnakeEngine.Moving.RIGHT; break;
                        case RIGHT:
                            snakeEngine.movement = SnakeEngine.Moving.DOWN; break;
                        case DOWN:
                            snakeEngine.movement = SnakeEngine.Moving.LEFT; break;
                        case LEFT:
                            snakeEngine.movement = SnakeEngine.Moving.UP; break;
                        default: Log.d("", "Unknown snake heading movement.");
                    }
                }
                else
                {
                    switch(snakeEngine.movement)
                    {
                        case UP:
                            snakeEngine.movement = SnakeEngine.Moving.RIGHT; break;
                        case RIGHT:
                            snakeEngine.movement = SnakeEngine.Moving.DOWN; break;
                        case DOWN:
                            snakeEngine.movement = SnakeEngine.Moving.LEFT; break;
                        case LEFT:
                            snakeEngine.movement = SnakeEngine.Moving.UP; break;
                        default: Log.d("", "Unknown snake heading movement.");
                    }
                }
        }
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
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
