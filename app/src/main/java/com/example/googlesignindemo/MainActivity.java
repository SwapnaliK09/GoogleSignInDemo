package com.example.googlesignindemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.googlesignindemo.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "GOOGLE SIGN IN TAG";
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        dbHelper = new DBHelper(MainActivity.this);
        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        binding.btnGoogleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG,"onclick : begin google Signin");

                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent,RC_SIGN_IN);
            }
        });

    }



    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser!= null){
            startActivity(new Intent(this,ProfileActivity.class));
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Log.d(TAG,"onActivityResult: Google Signin intent result");
            Task<GoogleSignInAccount> accountnTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account= accountnTask.getResult(ApiException.class);
                firebaseAuthwithGoogleAccount(account);
            }
            catch (Exception e){
                Log.d(TAG,"onActivityResult :"+e.getMessage());
            }
        }
    }

    private void firebaseAuthwithGoogleAccount(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG,"onSuccess : Logged IN");

                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        updateUI(firebaseUser);

                        String uid = firebaseUser.getUid();
                        String email = firebaseUser.getEmail();
                        Uri photo = firebaseUser.getPhotoUrl();

                        Log.d(TAG,"onSuccess Email : "+email);
                        Log.d(TAG,"onSuccess uid : "+uid);

                        if (authResult.getAdditionalUserInfo().isNewUser()){
                            Toast.makeText(MainActivity.this,"Account created "+email,Toast.LENGTH_SHORT).show();

                        }
                        else {
                            Toast.makeText(MainActivity.this,"Existing user "+email,Toast.LENGTH_SHORT).show();

                        }

                        startActivity(new Intent(MainActivity.this,ProfileActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"onFailure : log in failed "+e.getMessage());
                    }
                });
    }

    private void updateUI(FirebaseUser firebaseUser) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
            dbHelper.insertNewInfo(personName,personEmail);

            Toast.makeText(this,"person name : "+personName +" \n " +"Person Email :"+personEmail,Toast.LENGTH_SHORT).show();
        }

    }
}