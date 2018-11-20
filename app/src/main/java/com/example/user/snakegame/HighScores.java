package com.example.user.snakegame;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    //View variables
    String email;
    ImageView imgAvatar1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        imgAvatar1 = findViewById(R.id.imgAvatar1);
        TextView tvName1 = findViewById(R.id.tvName1);
        TextView tvScore1 = findViewById(R.id.tvScore1);

        setName(tvName1);
        setScore(tvScore1);

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
                    Log.i("FINAL_LMAO",myMap.getKey() + " " + myMap.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("databaseError", databaseError.getMessage());
            }
        });
    }

    private void setName(final TextView tvName)
    {
        //Sets the path to get to the score node.
        String path = "/" + user.getUid() + "/Email";

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
                    tvName.setText(dataSnapshot.getValue(String.class));
                    email = dataSnapshot.getValue(String.class);
                    setAvatar(imgAvatar1, email);
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

    private void setScore(final TextView tvScore)
    {
        //Sets the path to get to the score node.
        String path = "/" + user.getUid() + "/Score";

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
                    int score = dataSnapshot.getValue(Integer.class);
                    tvScore.setText(String.valueOf(score));
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

    private void setAvatar(ImageView imgAvatar, String email)
    {
        // Reference to an image file in Cloud Storage
        Log.i("", "Path is: " + "images/" + email);
        StorageReference pathRef = storageRef.child("images/" + email);

        // Clears the cache, so Glide has to fetch the image from Firebase on every request.
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true);

        // Download directly from StorageReference using Glide
        Glide.with(this)
                .applyDefaultRequestOptions(options) //Applies the above RequestOptions.
                .load(pathRef)
                .into(imgAvatar);
    }

    public void BackToMenu(View v)
    {
        General gen = new General(getApplicationContext());
        gen.GoToActivity(MainActivity.class);
    }
}
