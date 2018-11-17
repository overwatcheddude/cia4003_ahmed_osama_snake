package com.example.user.snakegame;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class profile extends AppCompatActivity {
    private General general;
    private TextView tvUploadRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUploadRate = findViewById(R.id.tvUploadRate);
        general = new General(getApplicationContext());
    }

    public void BackToMenu(View v)
    {
        General gen = new General(getApplicationContext());
        gen.GoToActivity(MainActivity.class);
    }

    public void uploadImage(View v)
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // File or Blob
        Uri file = Uri.parse("android.resource://com.example.user.snakegame/" + R.drawable.avatar);

        // Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();

        // Upload file and metadata to the path 'images/mountains.jpg'
        UploadTask uploadTask = storageRef.child("images/"+file.getLastPathSegment()).putFile(file, metadata);

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
}
