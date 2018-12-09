package com.example.user.snakegame;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HighScores extends AppCompatActivity
{
    //Get the current loggen in user from Firebase authentication.
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    //Gets the instance of Firebase storage.
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //Gets the reference to firebase storage.
    StorageReference storageRef = storage.getReference();

    //Get the instance and reference of the firebase database.
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    //Variables used in multiple methods.
    String email;
    String uid;
    Integer i = 4;
    Integer j = 4;
    Integer k = 4;

    //View variables
    TextView[] tvScore = new TextView[5];
    TextView[] tvEmail = new TextView[5];
    ImageView[] imgAvatar = new ImageView[5];

    //Animation gloal variables
    Animation bounce;
    Animation fadein;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        //Gives the 5 view variables the resouce IDs of the input items.
        assignResourcesToInputs();

        //Loads animations
        bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);

        databaseReference.child("Leaderboard").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Gets all scores from the Leaderboard.
                Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();

                //Convert from hashmap to string.
                String uidAndScoreObjects = map.toString(); System.out.println("uidAndScoreObjects is " + uidAndScoreObjects);

                //Splits the objects by comma, and place them in a list.
                List<String> leaderboard = Arrays.asList(uidAndScoreObjects.split(",")); System.out.println("leaderboard is " + leaderboard);

                //Create a tree map to store the UID and score in an organized way (desc).
                TreeMap<Integer, String> uidAndScoreMap = new TreeMap(Collections.reverseOrder()); System.out.println("uidAndScoreMap created");
                uidAndScoreMap.putAll(uidAndScoreMap);

                //Goes through all the UIDs and scores.
                System.out.println("map.size() is " + map.size());
                for (int i = 0; i < map.size(); i++)
                {
                    //Gets the UID&Score object by index.
                    String uidAndScore = leaderboard.get(i);

                    //Get the first 28 characters (UID)
                    String uid = uidAndScore.substring(1, 29);

                    //Gets the characters after the UID.
                    String scoreField = uidAndScore.substring(29);

                    //Removes all characters except for digits.
                    String score = scoreField.replaceAll("[^\\d]", "");

                    //Adds the UID and score and pair them together.
                    uidAndScoreMap.put(Integer.parseInt(score), uid); System.out.println("uidAndScoreMap is " + uidAndScoreMap);
                }

                //Gets only the first 5 players
                TreeMap<Integer, String> myNewMap = uidAndScoreMap.entrySet().stream()
                        .limit(5)
                        .collect(TreeMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

                //Foreach, Key is score, value is UID.
                for (Map.Entry myMap  : myNewMap.entrySet())
                {
                    System.out.println("i is " + i);
                    System.out.println("myMap is " + myMap);
                    System.out.println("myNewMap.entrySet() is " + myNewMap.entrySet());
                    //Displays a player's score to the user.
                    tvScore[i].setText(String.valueOf(myMap.getKey()));

                    //The score will fade in.
                    tvScore[i].startAnimation(fadein);

                    //Continue to the next index.
                    i--;

                    //Gets the UID and call the setEmail method in-order to display the user's email.
                    uid = String.valueOf(myMap.getValue()); System.out.println("uid is " + uid);
                    setEmail(uid); System.out.println("setEmail called with uid: " + uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("databaseError", databaseError.getMessage());
            }
        });
    }

    private void setEmail(String uid)
    {
        //Sets the path to get to the user's email node.
        String path = "/" + uid + "/Email";

        //Read the player's email from Firebase realtime DB.
        final DatabaseReference NameScoreRef = FirebaseDatabase.getInstance().getReference(path);

        NameScoreRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                //If the email exists, then retrieve it.
                if (dataSnapshot.exists())
                {
                    //Displays the player's email in the high scores list.
                    tvEmail[k].setText(dataSnapshot.getValue(String.class));

                    //The email text will bounce when they appear.
                    tvEmail[k].startAnimation(bounce);

                    //Move on to the next email.
                    k--;

                    //Hold the email value, which will be used for setAvatar method.
                    email = dataSnapshot.getValue(String.class);
                    setAvatar(email);
                }
            }
            @Override
            public void onCancelled(DatabaseError error)
            {
                //Failed to read value
                Log.i("", "Failed to read value.", error.toException());
            }
        });
    }

    private void setAvatar(String email)
    {
        // Reference to an image file in Cloud Storage
        StorageReference pathRef = storageRef.child("images/" + email);

        // Clears the cache, so Glide has to fetch the image from Firebase on every request.
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true);

        // Download directly from StorageReference using Glide
        Glide.with(this)
                .applyDefaultRequestOptions(options) //Applies the above RequestOptions.
                .load(pathRef)
                .into(imgAvatar[j]);
                j--; //Moves on to the next imageView.
    }

    public void BackToMenu(View v)
    {
        General gen = new General(getApplicationContext());
        gen.GoToActivity(MainActivity.class);
    }

    public void assignResourcesToInputs()
    {
        //1
        tvEmail[0] = findViewById(R.id.tvName1);
        tvScore[0] = findViewById(R.id.tvScore1);
        imgAvatar[0] = findViewById(R.id.imgAvatar1);
        //2
        tvEmail[1] = findViewById(R.id.tvName2);
        tvScore[1] = findViewById(R.id.tvScore2);
        imgAvatar[1] = findViewById(R.id.imgAvatar2);
        //3
        tvEmail[2] = findViewById(R.id.tvName3);
        tvScore[2] = findViewById(R.id.tvScore3);
        imgAvatar[2] = findViewById(R.id.imgAvatar3);
        //4
        tvEmail[3] = findViewById(R.id.tvName4);
        tvScore[3] = findViewById(R.id.tvScore4);
        imgAvatar[3] = findViewById(R.id.imgAvatar4);
        //5
        tvEmail[4] = findViewById(R.id.tvName5);
        tvScore[4] = findViewById(R.id.tvScore5);
        imgAvatar[4] = findViewById(R.id.imgAvatar5);
    }
}
