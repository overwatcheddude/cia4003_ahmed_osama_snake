package com.example.user.snakegame;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
    Thread myThread = null; //A thread for looping the game.

    //The snake has 4 moving directions: Up, right down and left.
    public enum Moving {UP, RIGHT, DOWN, LEFT}

    Moving movement = Moving.UP; // The snake starts moving up at the beginning of the game.

    //Used to get the size of the screen.
    int screenX;
    int screenY;

    int snakeLength; //The length of the snake becomes longer after eating apples.

    //XY coordinates of the apple.
    int appleX;
    int appleY;

    int blockSize; //The size of every block that makes up the snake.

    // The size in segments of the playable area
    final int NUM_BLOCKS_WIDE = 40;
    int numBlocksHigh;

    long nextFrameTime; //The pause between updates.
    final long FPS = 10; //Updates the game 10 times every second.
    final long MILLIS_PER_SECOND = 1000; //Represents 1 second.

    int score; //Used to hold the score of the player.

    // The location in the grid of all the segments
    int[] snakeXs;
    int[] snakeYs;

    volatile boolean isPlaying; //Is the game paused or resumed?

    Canvas canvas; //Canvas for the paint.
    SurfaceHolder surfaceHolder; //Part of the canvas.
    Paint paint; //Paint for the canvas.

    //Sound effects
    MediaPlayer eatApple = MediaPlayer.create(getContext(), R.raw.eat_bob);
    MediaPlayer snakeDeath = MediaPlayer.create(getContext(), R.raw.snake_crash);

    public SnakeEngine(Context context, Point size)
    {
        super(context);

        screenX = size.x;
        screenY = size.y;

        // Work out how many pixels each block is
        blockSize = screenX / NUM_BLOCKS_WIDE;
        // How many blocks of the same size will fit into the height
        numBlocksHigh = screenY / blockSize;

        // Initialize the drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        // If you score 200 you are rewarded with a crash achievement!
        snakeXs = new int[200];
        snakeYs = new int[200];

        newGame(); // (Re)Starts the game.
    }

    @Override
    public void run()
    {
        while (isPlaying) //While the game is resumed.
        {
            if(updateRequired()) //Update the game.
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
        snakeLength = 1; //The snake length starts at 1.
        snakeXs[0] = NUM_BLOCKS_WIDE / 2;
        snakeYs[0] = numBlocksHigh / 2;

        spawnApple(); //Spawns the first apple.
        score = 0; //Resets the score.

        // Setup nextFrameTime so an update is triggered
        nextFrameTime = System.currentTimeMillis();
    }

    public void spawnApple()
    {
        Random random = new Random();
        appleX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        appleY = random.nextInt(numBlocksHigh - 1) + 1;
    }

    private void eatApple()
    {
        snakeLength++; //Increases the length of the snake.
        score++; //Increases the score.
        spawnApple(); //Spawn another apple.

        //Play apple eating sound
        eatApple.setAudioStreamType(AudioManager.STREAM_MUSIC);
        eatApple.start();
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
                snakeYs[0]--; break;
            case RIGHT:
                snakeXs[0]++; break;
            case DOWN:
                snakeYs[0]++; break;
            case LEFT:
                snakeXs[0]--; break;
            default: Log.d("", "Unknown snake movement.");
        }
    }

    //This method checks whether the snake died or not.
    private boolean detectDeath()
    {
        boolean dead = false; //False means the the snake is still alive.

        //If the snake hits the borders of the screen, the snake dies.
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
        //If the snake XY position is the same as the apple XY position, then apple is eaten.
        if (snakeXs[0] == appleX && snakeYs[0] == appleY)
        {
            eatApple();
        }

        moveSnake(); //The snake movement is updated.

        //If the snake dies, then the game restarts.
        if (detectDeath())
        {
            //Plays snake dying sound.
            snakeDeath.setAudioStreamType(AudioManager.STREAM_MUSIC);
            snakeDeath.start();

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

            //Draws the apple in the canvas.
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

            // Return true so that the update and draw functions are executed
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
