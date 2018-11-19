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

        //Firebase data
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        //Reads imageview.
        final TextView tvStatus = findViewById(R.id.tvStatus);
        ImageView ivAvatar = findViewById(R.id.ivAvatar);

        final ProgressBar progressBar = findViewById(R.id.progressBar);
        tvStatus.setText("Loading avatar, please wait.");

        // Reference to an image file in Cloud Storage
        StorageReference pathRef = storageRef.child("images/" + user.getEmail());

        // Clears the cache, so Glide has to fetch the image from Firebase on every request.
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true);

        // Download directly from StorageReference using Glide
        Glide.with(this)
                .applyDefaultRequestOptions(options) //Applies the above RequestOptions.
                .load(pathRef)
                .listener(new RequestListener<Drawable>() {
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
                .into(ivAvatar);
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