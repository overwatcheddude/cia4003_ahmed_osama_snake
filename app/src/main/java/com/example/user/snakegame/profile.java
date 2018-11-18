package com.example.user.snakegame;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class profile extends AppCompatActivity
{
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private General general;
    private TextView tvUploadRate;
    private ImageView ivAvatar;
    private String setOption;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Gets view data.
        tvUploadRate = findViewById(R.id.tvUploadRate);
        general = new General(getApplicationContext());
        ivAvatar = findViewById(R.id.ivAvatar);

        loadImageFromDB();
    }

    private void loadImageFromDB()
    {
        final ProgressBar progressBar = findViewById(R.id.progressBar);

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
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(ivAvatar);
    }

    public void BackToMenu(View v)
    {
        General gen = new General(getApplicationContext());
        gen.GoToActivity(MainActivity.class);
    }

    public void uploadImage(View v)
    {
        ivAvatar.setDrawingCacheEnabled(true);
        ivAvatar.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) ivAvatar.getDrawable()).getBitmap();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();

        // Upload file and metadata to the path 'images/user_email'
        UploadTask uploadTask = storageRef.child("images/"+ user.getEmail()).putBytes(data, metadata);

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                tvUploadRate.setText("Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                tvUploadRate.setText("Image upload paused.");
                general.DisplayMessage("The upload process has been paused.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                tvUploadRate.setText("Image upload failed.");
                general.DisplayMessage("Failed to upload image.");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                tvUploadRate.setText("Image uploaded!");
                general.DisplayMessage("Image uploaded successfully!");
            }
        });

    }

    public  void setImage(View v)
    {
        Intent i = new Intent();
        switch (v.getId())
        {

            case R.id.btnSetByCamera:
                i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                setOption = "Camera";
                break;
            case R.id.btnSetByGallery:
                i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                setOption = "Gallery";
                break;
            default: general.DisplayMessage("Unknown set button clicked.");
        }
        startActivityForResult(i, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                if (setOption.equals("Camera"))
                {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    ivAvatar.setImageBitmap(photo);
                }
                if (setOption.equals("Gallery"))
                {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = null;
                    try
                    {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    ivAvatar.setImageBitmap(bitmap);
                }
            }
            else if (resultCode == RESULT_CANCELED)
            {
                general.DisplayMessage("User cancelled image capture");
            }
            else
            {
                general.DisplayMessage("Sorry! Failed to capture image");
            }
        }
    }
}