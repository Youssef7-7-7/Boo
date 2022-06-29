package com.jotech.boo.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jotech.boo.R;
import com.jotech.boo.models.User;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class Login_activity extends AppCompatActivity {

    private VideoView videoBg;
    MediaPlayer mMediaPlayer;
    int mCurrentVideoPosition;
    Animation scaleUp, scaleDown;
    private static final String TAG = "GoogleActivity";
    private CallbackManager mCallbackManager;
    private static final int RC_SIGN_IN = 9001;
    private ImageView glog;
    private ImageView fblog;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference rootRef;
    Location location;
    Geocoder geocoder;
    List<Address> addresses;
    private String country;
    private String city;



    private GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String currentUser;
            currentUser = user.getUid();
            rootRef.child("Profiles").child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        long allChilds = snapshot.getChildrenCount();
                        if (allChilds >= 8){
                            Intent intent = new Intent(Login_activity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Intent intent = new Intent(Login_activity.this, FinishLoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }

        else {
        }

        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        location = locationManager
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        geocoder = new Geocoder(this);



        glog = findViewById(R.id.glog);
        fblog = findViewById(R.id.fblog);

        mCallbackManager = CallbackManager.Factory.create();


        fblog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginButton loginButton = new LoginButton(v.getContext());
                loginButton.setReadPermissions("email","public_profile");
                loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(Login_activity.this, "Login Cancelled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NotNull FacebookException e) {

                    }
                });
                loginButton.performClick();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);



        glog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        scaleUp = AnimationUtils.loadAnimation(this,R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this,R.anim.scale_down);

        videoBg = findViewById(R.id.VView);
        //hook the video to uri
        Uri uri = Uri.parse("android.resource://" + getPackageName() +"/" + R.raw.callmain);
        //set uri to video
        videoBg.setVideoURI(uri);
        videoBg.start();
        videoBg.setOnPreparedListener((mediaPlayer) -> {
            mMediaPlayer = mediaPlayer;
            mMediaPlayer.setLooping(true);
            //seek current position and play it
            if (mCurrentVideoPosition !=0){
                mMediaPlayer.seekTo(mCurrentVideoPosition);
                mMediaPlayer.start();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        if(location!= null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            addresses = geocoder.getFromLocation(latitude, longitude, 10);
                            Address address = addresses.get(0);
                            country = address.getCountryCode();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //not granted
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void handleFacebookAccessToken(AccessToken accessToken) {

        AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            FirebaseUser user = mAuth.getCurrentUser();
                            User firebaseUser = new User(user.getUid(), user.getDisplayName(), user.getPhotoUrl().toString(), city, 50, country);

                            //save user data
                            database.getReference().child("Profiles").child(user.getUid()).setValue(firebaseUser);

                            Toast.makeText(Login_activity.this, "Facebook: Login succeeded", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login_activity.this, FinishLoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(Login_activity.this, "Facebook: Login Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            User firebaseUser = new User(user.getUid(), user.getDisplayName(), user.getPhotoUrl().toString(), city, 50, country);

                            //save user data
                            database.getReference().child("Profiles").child(user.getUid()).setValue(firebaseUser);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }

    //on google sign in
    private void updateUI(FirebaseUser user) {
        if(user != null) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Login_activity.this, FinishLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            this.finish();
        } else {
            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        videoBg.resume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            if(location!= null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                addresses = geocoder.getFromLocation(latitude, longitude, 10);
                Address address = addresses.get(0);
                country = address.getCountryCode();
                city = address.getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //find current position and pause it
        if (mMediaPlayer != null) {
            int currPos = mMediaPlayer.getCurrentPosition();
            mCurrentVideoPosition = currPos;
            videoBg.pause();
        }
    }



    @Override
    protected void onDestroy() {


        super.onDestroy();

        //set mediaplayer to null on destroy

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}