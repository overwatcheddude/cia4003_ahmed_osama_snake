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

import java.util.Map;

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

        databaseReference.orderByChild("Score").limitToFirst(5).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                Log.i("orderByMe", "Map is: " + map);
                Log.i("orderByMe", "Your object is: " + map.get(user.getUid()));
                //Log.i("orderByMe", "Your score is: " + )
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("DatabaseError", String.valueOf(databaseError));
            }
        });

        setName(tvName1);
        setScore(tvScore1);
    }

    private void setName(final TextView tvName)
    {
        Log.i("Hey", String.valueOf(databaseReference.orderByChild("Score").limitToFirst(5)));

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
