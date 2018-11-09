package com.example.user.snakegame;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


class SnakeEngine extends SurfaceView implements Runnable
{
    //A thread for looping the game.
    Thread myThread = null;

    //Plays sound effects.
    SoundPool soundPool;
    int eat_bob = -1;
    int snake_crash = -1;

    //The snake has 4 moving directions: Up, right down and left.
    public enum Moving {UP, RIGHT, DOWN, LEFT}

    // The snake starts moving up at the beginning of the game.
    Moving movement = Moving.UP;

    // To hold the screen size in pixels
    int screenX;
    int screenY;

    // How long is the snake
    int snakeLength;

    //XY coordinates of the apple.
    int appleX;
    int appleY;

    // The size in pixels of a snake segment
    int blockSize;

    // The size in segments of the playable area
    final int NUM_BLOCKS_WIDE = 40;
    int numBlocksHigh;

    // Control pausing between updates
    long nextFrameTime;
    // Update the game 10 times per second
    final long FPS = 10;
    // There are 1000 milliseconds in a second
    final long MILLIS_PER_SECOND = 1000;
    // We will draw the frame much more often

    // How many points does the player have
    int score;

    // The location in the grid of all the segments
    int[] snakeXs;
    int[] snakeYs;

    // Everything we need for drawing
    // Is the game currently playing?
    volatile boolean isPlaying;

    // A canvas for our paint
    Canvas canvas;

    // Required to use canvas
    SurfaceHolder surfaceHolder;

    // Some paint for our canvas
    Paint paint;

    public SnakeEngine(Context context, Point size)
    {
        super(context);

        screenX = size.x;
        screenY = size.y;

        // Work out how many pixels each block is
        blockSize = screenX / NUM_BLOCKS_WIDE;
        // How many blocks of the same size will fit into the height
        numBlocksHigh = screenY / blockSize;

        // Set the sound up
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try
        {
            // Create objects of the 2 required classes
            // Use m_Context because this is a reference to the Activity
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Prepare the two sounds in memory
            descriptor = assetManager.openFd("get_mouse_sound.ogg");
            eat_bob = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("death_sound.ogg");
            snake_crash = soundPool.load(descriptor, 0);

        } catch (Exception e)
        {
            Log.i("", e.getMessage());
        }


        // Initialize the drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        // If you score 200 you are rewarded with a crash achievement!
        snakeXs = new int[200];
        snakeYs = new int[200];

        // Start the game
        newGame();
    }

    @Override
    public void run()
    {
        while (isPlaying)
        {
            // Update 10 times a second
            if(updateRequired())
            {
                update();
                draw();
            }
        }
    }

    public void pause()
    {
        isPlaying = false;
        try
        {
            myThread.join();
        } catch (Exception e)
        {
            Log.d("", e.getMessage());
        }
    }

    public void resume()
    {
        isPlaying = true;
        myThread = new Thread(this);
        myThread.start();
    }

    public void newGame()
    {
        // Start with a single snake segment
        snakeLength = 1;
        snakeXs[0] = NUM_BLOCKS_WIDE / 2;
        snakeYs[0] = numBlocksHigh / 2;

        // Get Bob ready for dinner
        spawnBob();

        // Reset the score
        score = 0;

        // Setup nextFrameTime so an update is triggered
        nextFrameTime = System.currentTimeMillis();
    }

    public void spawnBob()
    {
        Random random = new Random();
        appleX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        appleY = random.nextInt(numBlocksHigh - 1) + 1;
    }

    private void eatBob()
    {
        //  Got him!
        // Increase the size of the snake
        snakeLength++;
        //replace Bob
        // This reminds me of Edge of Tomorrow. One day Bob will be ready!
        spawnBob();
        //add to the score
        score++;
        soundPool.play(eat_bob, 1, 1, 0, 0, 1);
    }

    private void moveSnake()
    {
        // Move the body
        for (int i = snakeLength; i > 0; i--)
        {
            // Start at the back and move it
            // to the position of the segment in front of it
            snakeXs[i] = snakeXs[i - 1];
            snakeYs[i] = snakeYs[i - 1];

            // Exclude the head because
            // the head has nothing in front of it
        }

        // Move the head in the appropriate heading
        switch (movement)
        {
            case UP:
                snakeYs[0]--;
                break;

            case RIGHT:
                snakeXs[0]++;
                break;

            case DOWN:
                snakeYs[0]++;
                break;

            case LEFT:
                snakeXs[0]--;
                break;
        }
    }

    private boolean detectDeath()
    {
        // Has the snake died?
        boolean dead = false;

        // Hit the screen edge
        if (snakeXs[0] == -1)
        {
            dead = true;
        }
        if (snakeXs[0] >= NUM_BLOCKS_WIDE)
        {
            dead = true;
        }
        if (snakeYs[0] == -1)
        {
            dead = true;
        }
        if (snakeYs[0] == numBlocksHigh)
        {
            dead = true;
        }

        // Eaten itself?
        for (int i = snakeLength - 1; i > 0; i--)
        {
            if ((i > 4) && (snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i]))
            {
                dead = true;
            }
        }

        return dead;
    }

    public void update()
    {
        // Did the head of the snake eat Bob?
        if (snakeXs[0] == appleX && snakeYs[0] == appleY)
        {
            eatBob();
        }

        moveSnake();

        if (detectDeath())
        {
            //start again
            soundPool.play(snake_crash, 1, 1, 0, 0, 1);

            newGame();
        }
    }

    public void draw()
    {
        // Get a lock on the canvas
        if (surfaceHolder.getSurface().isValid())
        {
            canvas = surfaceHolder.lockCanvas();

            // Fill the screen with Game Code School blue
            canvas.drawColor(Color.argb(255, 26, 128, 182));

            // Set the color of the paint to draw the snake white
            paint.setColor(Color.argb(255, 255, 255, 255));

            // Scale the HUD text
            paint.setTextSize(90);
            canvas.drawText("Score: " + score, 10, 70, paint);

            // Draw the snake one block at a time
            for (int i = 0; i < snakeLength; i++)
            {
                canvas.drawRect(snakeXs[i] * blockSize,
                        (snakeYs[i] * blockSize),
                        (snakeXs[i] * blockSize) + blockSize,
                        (snakeYs[i] * blockSize) + blockSize,
                        paint);
            }

            // Set the color of the paint to draw Bob red
            paint.setColor(Color.argb(255, 255, 0, 0));

            // Draw Bob
            canvas.drawRect(appleX * blockSize,
                    (appleY * blockSize),
                    (appleX * blockSize) + blockSize,
                    (appleY * blockSize) + blockSize,
                    paint);

            // Unlock the canvas and reveal the graphics for this frame
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean updateRequired()
    {
        // Are we due to update the frame
        if(nextFrameTime <= System.currentTimeMillis())
        {
            // Tenth of a second has passed
            // Setup when the next update will be triggered
            nextFrameTime =System.currentTimeMillis() + MILLIS_PER_SECOND / FPS;

            // Return true so that the update and draw
            // functions are executed
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= screenX / 2)
                {
                    switch(movement)
                    {
                        case UP:
                            movement = Moving.RIGHT; break;
                        case RIGHT:
                            movement = Moving.DOWN; break;
                        case DOWN:
                            movement = Moving.LEFT; break;
                        case LEFT:
                            movement = Moving.UP; break;
                        default: Log.d("", "Unknown snake heading movement.");
                    }
                }
                else
                    {
                    switch(movement)
                    {
                        case UP:
                            movement = Moving.LEFT; break;
                        case LEFT:
                            movement = Moving.DOWN; break;
                        case DOWN:
                            movement = Moving.RIGHT; break;
                        case RIGHT:
                            movement = Moving.UP; break;
                        default: Log.d("", "Unknown snake heading movement.");
                    }
                    }
        }
        return true;
    }
}
