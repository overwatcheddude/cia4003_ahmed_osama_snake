package com.example.user.snakegame;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.io.IOException;

public class AvatarPreview extends AppCompatActivity
{
    //Holds the request code for the camera.
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private General general;
    private TextView tvStatus;
    private String setOption;
    private ImageView ivAvatar;

    //Gets the current logged in user using Firebase authentication.
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    //Gets instance of Firebase storage.
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //Gets root reference to Firebase storage.
    StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_preview);

        //Gets view data.
        tvStatus = findViewById(R.id.tvStatus);
        general = new General(getApplicationContext());
        ivAvatar = findViewById(R.id.ivAvatar);
    }

    public void uploadImage(View v)
    {
        //If image view is empty, then display error message.
        if (ivAvatar.getDrawable() == null)
        {
            tvStatus.setText("Please set an image before uploading!");
            return;
        }
        //Set imageview options.
        ivAvatar.setDrawingCacheEnabled(true);
        ivAvatar.buildDrawingCache();

        //Convert imageview to bitmap.
        Bitmap bitmap = ((BitmapDrawable) ivAvatar.getDrawable()).getBitmap();

        //Converts bitmap of the image view to JPG.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        //Creates metadata for the file.
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();

        //Uploads the image and its metadata to the path 'images/user_email'.
        UploadTask uploadTask = storageRef.child("images/"+ user.getEmail()).putBytes(data, metadata);

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                tvStatus.setText("Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                tvStatus.setText("Image upload paused.");
                general.DisplayMessage("The upload process has been paused.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                tvStatus.setText("Image upload failed.");
                general.DisplayMessage("Failed to upload image.");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                tvStatus.setText("Image uploaded!");
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
                tvStatus.setText("Avatar set! Please upload it now.");
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

    public void backToProfile(View v)
    {
        General gen = new General(getApplicationContext());
        gen.GoToActivity(profile.class);
    }
}
