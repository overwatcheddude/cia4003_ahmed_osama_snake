package com.example.user.snakegame;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity
{
    private General general;
    private FirebaseAuth mAuth;
    private String email;
    private String password;
    private EditText etEmail;
    private EditText etPassword;

    private boolean isValidationSuccessful()
    {
        //If validation is true, then the information is correct.
        //If validation is false, then the user has to enter valid information.
        boolean isValid = true;

        //Get text from the edittext and place them in email and password global variables.
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();

        if (!email.matches("^\\S+@\\S+\\.\\S+$"))
        {
            etEmail.setError("Insert a valid email!");
            isValid = false;
        }
        if (password.length() < 6)
        {
            etPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        general = new General(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();

        //Read edittext
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }

    public void login(View v)
    {
        if (isValidationSuccessful())
        {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                general.DisplayMessage("Logged in!");
                                general.GoToActivity(MainActivity.class);
                            } else {
                                // If sign in fails, display a message to the user.
                                general.DisplayMessage("Login failed.");
                            }
                        }
                    });
        }
        else
        {
            general.DisplayMessage("Failed validation.");
        }
    }

    public void register(View v)
    {
        if (isValidationSuccessful())
        {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                general.DisplayMessage("Account successfully registered!");
                            } else {
                                // If sign in fails, display a message to the user.
                                general.DisplayMessage("Account registeration failed.");
                            }
                        }
                    });
        }
        else
        {
            general.DisplayMessage("Failed validation.");
        }
    }
}
