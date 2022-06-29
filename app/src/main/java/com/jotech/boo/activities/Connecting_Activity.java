package com.jotech.boo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jotech.boo.databinding.ActivityConnectingBinding;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public class Connecting_Activity extends AppCompatActivity {

    private FirebaseDatabase db;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    boolean isOkay = false;
    String currentUser = "";
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityConnectingBinding binding = ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        db = FirebaseDatabase.getInstance();
        rootRef = db.getReference();

        mAuth = FirebaseAuth.getInstance();
        String usernamee = mAuth.getUid();

        rootRef.child("WebRTC").child(currentUser).removeValue();

        db.getReference().child("Users").orderByChild("status").equalTo(0).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.getChildrenCount() > 0) {
                            //room available

                            isOkay = true;
                            for (DataSnapshot childSnap : snapshot.getChildren()) {
                                db.getReference().child("Users")
                                        .child(Objects.requireNonNull(childSnap.getKey())).child("incoming").setValue(usernamee);


                                db.getReference().child("Users")
                                        .child(childSnap.getKey()).child("status").setValue(1);

                                Intent intent = new Intent(Connecting_Activity.this, CallActivity.class);
                                String incoming = childSnap.child("incoming").getValue(String.class);
                                String createdBy = childSnap.child("createdby").getValue(String.class);
                                boolean isAvailable = childSnap.child("isAvailable").getValue(Boolean.class);
                                intent.putExtra("usernamee", usernamee);
                                intent.putExtra("incoming", incoming);
                                intent.putExtra("createdBy", createdBy);
                                intent.putExtra("isAvailable", isAvailable);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            HashMap<String, Object> room = new HashMap<>();
                            room.put("incoming", usernamee);
                            room.put("createdBy", usernamee);
                            room.put("isAvailable", true);
                            room.put("status", 0);

                            assert usernamee != null;
                            db.getReference("Users").child(usernamee).setValue(room)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            db.getReference().child("Users").child(usernamee).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                    if (snapshot.child("status").exists()) {
                                                        if (snapshot.child("status").getValue(Integer.class) == 1) {

                                                            if (isOkay)
                                                                return;
                                                            isOkay = true;

                                                            Intent intent = new Intent(Connecting_Activity.this, CallActivity.class);
                                                            String incoming = snapshot.child("incoming").getValue(String.class);
                                                            String createdBy = snapshot.child("createdby").getValue(String.class);
                                                            boolean isAvailable = snapshot.child("isAvailable").getValue(boolean.class);
                                                            intent.putExtra("usernamee", usernamee);
                                                            intent.putExtra("incoming", incoming);
                                                            intent.putExtra("createdBy", createdBy);
                                                            intent.putExtra("isAvailable", isAvailable);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                                    });
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {


            super.onBackPressed();
            //intent
            rootRef.child("Users").child(currentUser).removeValue();
            rootRef.child("WebRTC").child(currentUser).removeValue();

        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onStop() {
        finish();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }
}
