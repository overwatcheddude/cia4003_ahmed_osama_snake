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
import java.util.Map;
import java.util.Random;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


class SnakeEngine extends SurfaceView implements Runnable
{
    //For debugging
    private void myLog(String msg)
    {
        Log.i("", msg);
    }

    private DatabaseReference databaseReference; //Used to add data to firebase.

    Thread myThread = null; //A thread for looping the game.

    public enum Moving {UP, RIGHT, DOWN, LEFT} //The snake has 4 moving directions: Up, right down and left.
    public Moving movement = Moving.UP; //The snake will start moving up in the beginning.

    //Used to get the size of the screen.
    public int screenX;
    int screenY;

    int snakeLength; //The length of the snake becomes longer after eating apples.

    //XY coordinates of the apple.
    int appleX;
    int appleY;

    int blockSize; //The size of every block that makes up the snake.

    //The size in segments of the playable area
    final int NUM_BLOCKS_WIDE = 40;
    int numBlocksHigh;

    long nextFrameTime; //The pause between updates.
    final long FPS = 10; //Updates the game 10 times every second.
    final long MILLIS_PER_SECOND = 1000; //Represents 1 second.

    //Get the user's name/email.
    String name = "Guest";

    int score; //Used to hold the score of the player.
    int highScore;

    //Will hold the length of the snake.
    int[] snakeXs;
    int[] snakeYs;

    volatile boolean isPlaying; //Is the game paused or resumed?

    Canvas canvas; //Canvas for the paint.
    SurfaceHolder surfaceHolder; //Part of the canvas.
    Paint paint; //Paint for the canvas.

    //Sound effects
    MediaPlayer eatApple = MediaPlayer.create(getContext(), R.raw.eat_apple);
    MediaPlayer snakeDeath = MediaPlayer.create(getContext(), R.raw.snake_dies);

    public SnakeEngine(Context context, Point size)
    {
        super(context);

        screenX = size.x;
        screenY = size.y;

        //Determine the block size based on the device screen dimensions.
        blockSize = screenX / NUM_BLOCKS_WIDE;
        numBlocksHigh = screenY / blockSize;

        //Initialize objects needed to draw the game.
        surfaceHolder = getHolder();
        paint = new Paint();

        //The max amount of apple the user can eat is 300. Anything more will crash the game.
        snakeXs = new int[300];
        snakeYs = new int[300];

        newGame(); //(Re)Starts the game.
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

    private void getFromDB()
    {
        //Sets the path to get to the score node.
        String path = "/" + name + "/Score";

        //Read the player's score from Firebase.
        final DatabaseReference NameScoreRef = FirebaseDatabase.getInstance().getReference(path);

        NameScoreRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                //If the score exists, then get it from Firebase.
                if (dataSnapshot.exists())
                {
                    highScore = dataSnapshot.getValue(Integer.class);
                }
                else
                {
                    myLog("dataSnapshot does not exist.");
                }
            }
            @Override
            public void onCancelled(DatabaseError error)
            {
                //Failed to read value
                Log.w("", "Failed to read value.", error.toException());
            }
        });
    }

    private void addToDB()
    {
        //Get root reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //Read the score before adding.
        getFromDB();

        //If the user does not have a high score, then set new score.
        if (highScore == 0)
        {
            databaseReference.child(name).child("Score").setValue(score);
        }
        //If the player score is higher than the score in the database, then add it.
        else if (score > highScore)
        {
            databaseReference.child(name).child("Score").setValue(score);
        }
        else
        {
            myLog("Score is not higher than high score.");
        }
    }

    public void newGame()
    {
        snakeLength = 1; //The snake length starts at 1.
        snakeXs[0] = NUM_BLOCKS_WIDE / 2;
        snakeYs[0] = numBlocksHigh / 2;

        //Read the score from the database.
        getFromDB();

        //If the score is not zero, then add the score to firebase.
        if (score != 0)
        {
            addToDB();
        }

        spawnApple(); //Spawns the first apple.
        score = 0; //Resets the score.

        //Helps determine when a frame update is needed.
        nextFrameTime = System.currentTimeMillis();
    }

    public void spawnApple()
    {
        Random random = new Random();
        appleX = random.nextInt(NUM_BLOCKS_WIDE - 10);
        appleY = random.nextInt(numBlocksHigh - 10);
    }

    private void moveSnake()
    {
        //Moves the snake.
        for (int i = snakeLength; i > 0; i--)
        {
            //Begins at the back of the snake, and move the snake to the position of the block in-front of it.
            snakeXs[i] = snakeXs[i - 1];
            snakeYs[i] = snakeYs[i - 1];
        }

        //Move the snake head in the required direction.
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

        //Checks if the snake has eaten itself or not.
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
            snakeLength++; //Increases the length of the snake.
            score++; //Increases the score.
            spawnApple(); //Spawn another apple.

            //Play apple eating sound
            eatApple.setAudioStreamType(AudioManager.STREAM_MUSIC);
            eatApple.start();
        }

        moveSnake(); //The snake movement is updated.

        //If the snake dies, then the game restarts.
        if (detectDeath())
        {
            //Plays snake dying sound.
            snakeDeath.setAudioStreamType(AudioManager.STREAM_MUSIC);
            snakeDeath.start();

            newGame(); //Restarts the game
        }
    }

    public void draw()
    {
        //Interacts with the canvas.
        if (surfaceHolder.getSurface().isValid())
        {
            canvas = surfaceHolder.lockCanvas();

            //Set the canvas (background) color to black.
            canvas.drawColor(Color.argb(255, 0, 0, 0));

            //Set the snake color to green.
            paint.setColor(Color.argb(255, 0, 255, 0));

            //Set the score text.
            paint.setTextSize(90);
            canvas.drawText("Score: " + score, 10, 70, paint);

            //Draws each block of the snake.
            for (int i = 0; i < snakeLength; i++)
            {
                canvas.drawRect(snakeXs[i] * blockSize,
                        (snakeYs[i] * blockSize),
                        (snakeXs[i] * blockSize) + blockSize,
                        (snakeYs[i] * blockSize) + blockSize,
                        paint);
            }

            //Set the apple color to red.
            paint.setColor(Color.argb(255, 255, 0, 0));

            //Draws the apple in the canvas.
            canvas.drawRect(appleX * blockSize,
                    (appleY * blockSize),
                    (appleX * blockSize) + blockSize,
                    (appleY * blockSize) + blockSize,
                    paint);

            //Posts (displays) the canvas.
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean updateRequired()
    {
        //If the frame needs to be updated.
        if(nextFrameTime <= System.currentTimeMillis())
        {
            //1/10 of a second has been passed, setup the next update to be triggered.
            nextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / FPS;

            //Update and draw methods will be executed because it was returned true.
            return true;
        }
        return false;
    }
}
