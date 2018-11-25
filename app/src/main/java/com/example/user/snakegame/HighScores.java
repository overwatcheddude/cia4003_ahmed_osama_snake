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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HighScores extends AppCompatActivity
{
    //Firebase variables
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
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
                //Gets all scores from firebase.
                Map<String, String> map = (Map<String, String>) dataSnapshot.getValue(); Log.i("LMAO", ("map is " + String.valueOf(map)));
                String uidAndScoreObjects = map.toString(); Log.i("LMAO", "uidAndScoreObjects is " + uidAndScoreObjects);
                List<String> leaderboard = Arrays.asList(uidAndScoreObjects.split(",")); Log.i("LMAO", "leaderboard is " + leaderboard);

                //Create a map to store the UID and score.
                Map<Integer, String> uidAndScoreMap = new TreeMap<>();

                //Goes through all the UIDs and scores.
                for (int i = 0; i < map.size(); i++)
                {
                    String uidAndScore = leaderboard.get(i);
                    String uid = uidAndScore.substring(1, 29); Log.i("LMAO", "uid is " + uid);
                    String scoreField = uidAndScore.substring(29); Log.i("LMAO", "scoreField is " + scoreField);
                    String score = scoreField.replaceAll("[^\\d]", ""); Log.i("LMAO", "score is " + score);

                    //Adds the UID and score and pair them together.
                    uidAndScoreMap.put(Integer.parseInt(score), uid);
                }

                for (Map.Entry myMap  : uidAndScoreMap.entrySet())
                {
                    //Key is score, value is UID.
                    Log.i("FINAL_LMAO",myMap.getKey() + " " + myMap.getValue());

                    tvScore[i].setText(String.valueOf(myMap.getKey())); Log.i("NOPE", "tvScore[i] is " + i);
                    tvScore[i].startAnimation(fadein);
                    i--;
                    uid = String.valueOf(myMap.getValue());
                    setEmail(uid);
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
        //Sets the path to get to the score node.
        String path = "/" + uid + "/Email";

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
                    tvEmail[k].setText(dataSnapshot.getValue(String.class)); Log.i("NOPE", "tvEmail[k] is " + k);
                    tvEmail[k].startAnimation(bounce);
                    k--;
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
                .into(imgAvatar[j]); Log.i("NOPE", "imgAvatar[j] is " + j);
                j--;
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
