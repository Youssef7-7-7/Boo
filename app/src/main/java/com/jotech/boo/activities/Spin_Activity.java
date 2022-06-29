package com.jotech.boo.activities;

import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.bluehomestudio.luckywheel.LuckyWheel;
import com.bluehomestudio.luckywheel.WheelItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.jotech.boo.R;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class Spin_Activity extends AppCompatActivity {

    private LuckyWheel luckyWheel;
    private final List<WheelItem> wheelItems = new ArrayList<>();
    private String points;
    private RelativeLayout btnRl;
    private DatabaseReference rootRef;
    private String currentUser;
    private TextView btntxt;
    private Dialog dialog;
    private TextView nxtSpin;
    private TextView timer;
    private CountDownTimer cTimer = null;
    private static final String TAG = "Spin_Activity";
    private LottieAnimationView spinBtn;
    private LottieAnimationView load;
    private ArrayList<Integer> happy = new ArrayList<>();
    private ArrayList<Integer> sad = new ArrayList<>();
    private Boolean isRunning;
    private RelativeLayout backll;
    private ImageView warn;
    private Balloon balloon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spin);
        FirebaseUser userId = FirebaseAuth.getInstance().getCurrentUser();

        spinBtn = findViewById(R.id.spinbtn);
        load = findViewById(R.id.load);

        luckyWheel = findViewById(R.id.lwv);
        btnRl = findViewById(R.id.btnRl);
        btntxt = findViewById(R.id.btntxt);
        ImageView backbtn = findViewById(R.id.backbtn);
        ImageView warn = findViewById(R.id.warn);
        nxtSpin = findViewById(R.id.nxtSpin);
        timer = findViewById(R.id.timer);
        backll = findViewById(R.id.backll);
        rootRef = FirebaseDatabase.getInstance().getReference();
        isRunning = true;

        if (userId != null){
           currentUser = userId.getUid();
       }

       happy.add(R.layout.happy1_dialog_layout);
       happy.add(R.layout.happy2_dialog_layout);
       happy.add(R.layout.happy3_dialog_layout);

       sad.add(R.layout.sad1_dialog_layout);
       sad.add(R.layout.sad2_dialog_layout);
       sad.add(R.layout.sad3_dialog_layout);


       load.playAnimation();

       rootRef.child("Profiles").child(currentUser).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
               Long next_spin = snapshot.child("next_spin").getValue(Long.class);
               DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");

               offsetRef.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       Long offset = snapshot.getValue(Long.class);
                       if (offset != null) {
                           long estimatedServerTimeMs = System.currentTimeMillis() + offset;

                           if (next_spin != null && estimatedServerTimeMs < next_spin) {

                               rootRef.child("Profiles").child(currentUser).child("time_run").setValue(true);

                               cTimer = new CountDownTimer(next_spin - estimatedServerTimeMs,
                                       1000) {
                                   public void onTick(long millisUntilFinished) {
                                       SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                                       timer.setText(sdf.format(millisUntilFinished));
                                   }

                                   public void onFinish() {
                                       cancelTimer();
                                       rootRef.child("Profiles").child(currentUser).child("time_run").setValue(false);
                                   }
                               };
                               cTimer.start();

                               hideBtn();
                           } else {
                               rootRef.child("Profiles").child(currentUser).child("time_run").setValue(false);
                               showSpinBtn();
                               cancelTimer();

                           }
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {
                       Log.w(TAG, "Listener was cancelled");
                   }
               });
           }

           @Override
           public void onCancelled(@NonNull @NotNull DatabaseError error) {

           }
       });


        WheelItem wheelItem1 = new WheelItem(1, ResourcesCompat.getColor(getResources(), R.color.blue_card, null),
                BitmapFactory.decodeResource(getResources(), R.drawable.wangry), "0");


        WheelItem wheelItem2 = new WheelItem(2, ResourcesCompat.getColor(getResources(), android.R.color.holo_orange_dark, null),
                BitmapFactory.decodeResource(getResources(), R.drawable.whun), "8");


        WheelItem wheelItem3 = new WheelItem(3, ResourcesCompat.getColor(getResources(), R.color.yellow_card, null),
                BitmapFactory.decodeResource(getResources(), R.drawable.wwin), "25");


        WheelItem wheelItem4 = new WheelItem(4, ResourcesCompat.getColor(getResources(), R.color.green_card, null),
                BitmapFactory.decodeResource(getResources(), R.drawable.wnice), "9");


        WheelItem wheelItem5 = new WheelItem(5, ResourcesCompat.getColor(getResources(), android.R.color.holo_purple, null),
                BitmapFactory.decodeResource(getResources(), R.drawable.wangry1), "-10");


        WheelItem wheelItem6 = new WheelItem(6, ResourcesCompat.getColor(getResources(), R.color.red_card, null),
                BitmapFactory.decodeResource(getResources(), R.drawable.wsad), "0");


        WheelItem wheelItem7 = new WheelItem(7, ResourcesCompat.getColor(getResources(), R.color.brightBlue, null),
                BitmapFactory.decodeResource(getResources(), R.drawable.wdis), "-3");


        WheelItem wheelItem8 = new WheelItem(8, ResourcesCompat.getColor(getResources(), R.color.pink, null),
                BitmapFactory.decodeResource(getResources(), R.drawable.whappy), "11");

        try {
            wheelItems.add(wheelItem1);
            wheelItems.add(wheelItem2);
            wheelItems.add(wheelItem3);
            wheelItems.add(wheelItem4);
            wheelItems.add(wheelItem5);
            wheelItems.add(wheelItem6);
            wheelItems.add(wheelItem7);
            wheelItems.add(wheelItem8);

            luckyWheel.addWheelItems(wheelItems);

        } catch (Exception e) {
            e.printStackTrace();
        }


        luckyWheel.setLuckyWheelReachTheTarget(item -> {

            if (points != null) {
                if (isRunning) {
                    WheelItem itemSelected = wheelItems.get(Integer.parseInt(points) - 1);

                    String gems = itemSelected.text;
                    int gemsVal = Integer.parseInt(gems);
                    Toast.makeText(Spin_Activity.this, gems, Toast.LENGTH_LONG).show();

                    if (gemsVal > 0) {
                        rootRef.child("Profiles").child(currentUser).child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    int currCoins = snapshot.getValue(Integer.class);
                                    final int wonGems = currCoins + gemsVal;
                                    rootRef.child("Profiles").child(currentUser).child("coins").setValue(wonGems);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

                        dialog = new Dialog(this);
                        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                                WindowManager.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().getAttributes().windowAnimations
                                = android.R.style.Animation_Dialog;
                        Random random = new Random();
                        dialog.setContentView(happy.get(random.nextInt(happy.size())));

                        ImageButton imgBtn = dialog.findViewById(R.id.imgBtn);

                        imgBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                        if (isRunning)
                            dialog.show();
                    }
                    else if (gemsVal <= 0) {

                        rootRef.child("Profiles").child(currentUser).child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    int currCoins = snapshot.getValue(Integer.class);
                                    final int wonGems = currCoins + gemsVal;
                                    if (wonGems == 0){
                                        rootRef.child("Profiles").child(currentUser).child("coins").setValue(0);
                                    }
                                    else{
                                        rootRef.child("Profiles").child(currentUser).child("coins").setValue(wonGems);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });


                        dialog = new Dialog(this);

                        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                                WindowManager.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().getAttributes().windowAnimations
                                = android.R.style.Animation_Dialog;
                        Random random = new Random();
                        dialog.setContentView(sad.get(random.nextInt(sad.size())));

                        ImageButton imgBtn = dialog.findViewById(R.id.imgBtn);

                        imgBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                finish();
                            }
                        });

                        if (isRunning) {
                            dialog.show();
                        }
                    }
                }
            }
        });

        btnRl.setOnClickListener(v -> {

            Map<String, Object> value = new HashMap<>();
            value.put("last_spin", ServerValue.TIMESTAMP);
            value.put("time_run", true);
            rootRef.child("Profiles").child(currentUser).updateChildren(value);

            rootRef.child("Profiles").child(currentUser).child("last_spin").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        Long next_spin = 20000 + snapshot.getValue(Long.class);
                        rootRef.child("Profiles").child(currentUser).child("next_spin").setValue(next_spin);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

            Random random = new Random();
            points = String.valueOf(random.nextInt(8));

            if (points.equals("0")) {
                points = String.valueOf(1);
            }

            luckyWheel.rotateWheelTo(Integer.parseInt(points));
        });

        balloon = new Balloon.Builder(getApplicationContext())
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setText(getText(R.string.warn_msg))//do span
                .setTextColorResource(R.color.white)
                .setTextSize(15f)
                .setArrowOrientation(ArrowOrientation.END)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setArrowSize(10)
                .setAlpha(0.9f)
                .setDismissWhenTouchOutside(true)
                .setDismissWhenClicked(true)
                .setDismissWhenLifecycleOnPause(true)
                .setAutoDismissDuration(3000L)
                .setArrowPosition(0.5f)
                .setPadding(12)
                .setCornerRadius(8f)
                .setBackgroundColorResource(R.color.black)
                .setBalloonAnimation(BalloonAnimation.ELASTIC)
                .build();

        warn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                balloon.showAlignLeft(warn);
            }
        });
    }

    @Override
    protected void onPause() {
        isRunning = false;
        super.onPause();
    }

    void cancelTimer() {
        if(cTimer!=null) {
            cTimer.cancel();
        }
    }

    private void hideBtn() {

        if (load.getVisibility() == View.VISIBLE) {
            load.setVisibility(View.GONE);
        }
        spinBtn.setSpeed(1F);
        spinBtn.setMinAndMaxProgress(0.6f, 1.0f);
        btnRl.setVisibility(View.VISIBLE);
        btnRl.setClickable(false);
        btntxt.setVisibility(View.INVISIBLE);
        nxtSpin.setText(R.string.spin_title_2);
        nxtSpin.setVisibility(View.VISIBLE);
        spinBtn.playAnimation();
    }

    private void showSpinBtn() {

        if (load.getVisibility() == View.VISIBLE) {
            load.setVisibility(View.GONE);
        }
        spinBtn.setMinAndMaxProgress(0.0f, 0.5f);
        spinBtn.setSpeed(-1);
        btnRl.setVisibility(View.VISIBLE);
        btntxt.setVisibility(View.VISIBLE);
        nxtSpin.setText(R.string.spin_ready_txt);
        nxtSpin.setVisibility(View.VISIBLE);
        btnRl.setClickable(true);
        spinBtn.playAnimation();
    }

    @Override
    protected void onStop() {
        points = null;
        isRunning = false;
        super.onStop();
    }
}