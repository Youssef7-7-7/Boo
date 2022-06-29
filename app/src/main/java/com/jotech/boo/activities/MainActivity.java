package com.jotech.boo.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;
import com.camerakit.type.CameraSize;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jotech.boo.R;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private static final String TAGG = "Swipe Position";
    private float x1, x2, y1, y2;
    private static int MIN_DISTANCE = 150;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private static String TAG = "MainActivity";
    private static final int PERMISSION_REQ_ID = 22;
    private GestureDetector gestureDetector;


    ArrayList<Integer> femList = new ArrayList<>();
    ArrayList<Integer> menList = new ArrayList<>();
    private TextView tvName;
    private TextView tvCountry;
    private TextView tvCity;
    private TextView tvGem;
    private CircularImageView userPic;
    private LinearLayout gemFrame;
    private ImageView gem;
    private ImageView mic;
    private ImageButton switchCam;
    private CameraKitView camera;


    @SuppressLint("ClickableViewAccessibility")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        int unico = 0x1F449;
        String emoji = getEmo(unico);

        tvName = findViewById(R.id.tvName);
        tvCity = findViewById(R.id.tvCity);
        tvCountry = findViewById(R.id.tvCountry);
        tvGem = findViewById(R.id.tvGem);
        camera = findViewById(R.id.camera);
        mic = findViewById(R.id.mic);
        userPic = findViewById(R.id.userPic);
        gem = findViewById(R.id.gem);
        gemFrame = findViewById(R.id.gemFrame);
        switchCam = findViewById(R.id.switch_cam);

        femList.add(R.drawable.fem1);
        femList.add(R.drawable.fem2);
        femList.add(R.drawable.fem3);
        femList.add(R.drawable.fem4);
        femList.add(R.drawable.fem5);
        femList.add(R.drawable.fem6);
        femList.add(R.drawable.fem7);

        menList.add(R.drawable.man1);
        menList.add(R.drawable.man2);
        menList.add(R.drawable.man3);
        menList.add(R.drawable.man4);
        menList.add(R.drawable.man5);
        menList.add(R.drawable.man6);
        menList.add(R.drawable.man7);

        camera.setCameraListener(new CameraKitView.CameraListener() {
            @Override
            public void onOpened() {
                Log.v("CameraKitView", "CameraListener: onOpened()");
            }

            @Override
            public void onClosed() {
                Log.v("CameraKitView", "CameraListener: onClosed()");
            }
        });

        camera.setPreviewListener(new CameraKitView.PreviewListener() {
            @Override
            public void onStart() {
                Log.v("CameraKitView", "PreviewListener: onStart()");
                updateInfoText();
            }

            @Override
            public void onStop() {
                Log.v("CameraKitView", "PreviewListener: onStop()");
            }
        });

        db = FirebaseDatabase.getInstance();
        FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();

        db.getReference("Profiles").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue(String.class);
                String pic = snapshot.child("profile").getValue(String.class);
                String gender = snapshot.child("gender").getValue(String.class);
                String country = snapshot.child("country").getValue(String.class);
                String city = snapshot.child("city").getValue(String.class);
                Random random = new Random();

                    String halfName = String.valueOf(userName.subSequence(0,userName.indexOf(" ")));
                    String starName = userName.substring(userName.indexOf(" "));
                    starName = starName.replaceAll("[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890,;:!?./§+=)àç_è(']", "*");
                    String finalName = halfName+starName;
                    tvName.setText((finalName).subSequence(0, finalName.length()));
                    tvCountry.setText(country);
                    tvCity.setText(city);

                    if (gender.equalsIgnoreCase("Lady")){
                        Glide.with(getApplicationContext())
                                .load(femList.get(random.nextInt(femList.size())))
                                .into(userPic);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(menList.get(random.nextInt(menList.size())))
                                .into(userPic);
                    }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        tvGem.setText(tvGem.getText() + emoji +"\n\nHere!");

        gemFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GemsActivity.class);
                startActivity(intent);
            }
        });

        camera.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){

                    //starting to swipe gesture
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        y1 = event.getY();
                        break;

                    //ending switch gesture
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        y2 = event.getY();

                        //getting value for horizental swipe
                        float valueX = x2 - x1;
                        //getting value for vertical swipe
                        float valueY = y2 - y1;

                        if (Math.abs(valueX) > MIN_DISTANCE){
                            //detect left to right
                            if (x2 > x1 ){
                                Toast.makeText(MainActivity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                                Log.d(TAGG, "Right Swipe");
                            }
                            else {
                                //detect right to left
                                Toast.makeText(MainActivity.this, "Left Swipe", Toast.LENGTH_SHORT).show();

                                final Intent i = new Intent(new Intent(MainActivity.this, Connecting_Activity.class));
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);

                                Animatoo.animateSlideLeft(MainActivity.this);
                            }
                        }
                        else if (Math.abs(valueY) > MIN_DISTANCE){

                            //detect top to bottom swipe
                            if (y2 > y1){
                                Toast.makeText(MainActivity.this, "Bottom Swipe", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                //detect bottom to top swipe
                                Toast.makeText(MainActivity.this, "Top swipped", Toast.LENGTH_SHORT).show();
                            }
                        }
                }

                return false;
            }
        });

        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.gem)
                .into(gem);

        this.gestureDetector = new GestureDetector(MainActivity.this, this);

        switchCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (camera.getFacing() == CameraKit.FACING_BACK){
                   camera.setFacing(CameraKit.FACING_FRONT);
               }
               else {
                   camera.setFacing(CameraKit.FACING_BACK);
               }
            }
        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                replaceImage(R.drawable.mic_on);

                if (mic.isSelected()) {
                    mic.setSelected(false);
                    Toast.makeText(MainActivity.this, "unMuted!", Toast.LENGTH_SHORT).show();
                    replaceImage(R.drawable.mic_on);

                } else {
                    replaceImage(R.drawable.mic_off);
                    mic.setSelected(true);
                    Toast.makeText(MainActivity.this, "Muted!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateInfoText() {
        String facingValue = camera.getFacing() == CameraKit.FACING_BACK ? "BACK" : "FRONT";
        CameraSize previewSize = camera.getPreviewResolution();
    }

    @Override
    protected void onStart() {
        camera.onStart();
        super.onStart();
    }

    @Override
    protected void onResume() {
        camera.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        camera.onPause();
        camera.removeCameraListener();
        camera.removePreviewListener();
        super.onPause();
    }

    @Override
    protected void onStop() {
        camera.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        camera.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private String getEmo(int unico) {

        return new String(Character.toChars(unico));
    }

    private int replaceImage(int micc) {
        Glide.with(getApplicationContext())
                .load(micc)
                .into(mic);
        return micc;
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}