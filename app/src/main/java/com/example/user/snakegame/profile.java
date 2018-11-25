package com.example.user.snakegame;


import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class profile extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Gets the current logged in user using Firebase authentication.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //Get instance of firebase storage.
        FirebaseStorage storage = FirebaseStorage.getInstance();

        //Gets the root reference of the firebase storage.
        StorageReference storageRef = storage.getReference();

        //Reads imageview.
        final TextView tvStatus = findViewById(R.id.tvStatus);
        ImageView ivAvatar = findViewById(R.id.ivAvatar);

        final ProgressBar progressBar = findViewById(R.id.progressBar);
        tvStatus.setText("Loading avatar, please wait.");

        //Reference to the uploaded image, which will be retrieved based on the user's email.
        StorageReference pathRef = storageRef.child("images/" + user.getEmail());

        //Clears the cache, so Glide has to fetch the image from Firebase on every request.
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true);

        //Download directly from StorageReference using Glide
        Glide.with(this)
                .applyDefaultRequestOptions(options) //Applies the above RequestOptions.
                .load(pathRef) //Loads the above path.
                .listener(new RequestListener<Drawable>() { //Sets listener to see if this was an image uploaded or not.
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        tvStatus.setText("Please click on SET AVATAR button to have your own avatar.");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        tvStatus.setText("Retrieved your avatar from the server!");
                        return false;
                    }
                })
                .into(ivAvatar); //Displays the image if found.
    }

    public void OnActivityClick(View v)
    {
        General gen = new General(getApplicationContext());
        switch (v.getId())
        {
            case R.id.btnBackToMenu:
                gen.GoToActivity(MainActivity.class); break;
            case R.id.btnSetAvatar:
                gen.GoToActivity(AvatarPreview.class); break;
            default: gen.DisplayMessage("Unknown button clicked.");
        }
    }
}