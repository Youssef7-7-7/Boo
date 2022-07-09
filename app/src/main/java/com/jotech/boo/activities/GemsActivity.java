package com.jotech.boo.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.jotech.boo.R;

public class GemsActivity extends AppCompatActivity {

    LinearLayout topBar;
    ViewGroup menuLl;
    AnimationDrawable anim;
    ImageView backBtn;
    private VideoView vidBg;
    MediaPlayer mMediaPlayer;
    int mCurrentVideoPosition;
    private boolean isBig = false;
    private RelativeLayout spinRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gems);

        topBar = findViewById(R.id.topBar);
        backBtn = findViewById(R.id.back);
        vidBg = findViewById(R.id.vidBg);
        menuLl = findViewById(R.id.menuLl);
        spinRl = findViewById(R.id.spinRl);

        new Thread(new Runnable() {

            @Override
            public void run() {
                Uri uri = Uri.parse("android.resource://" + getPackageName() +"/" + R.raw.gemsact);

                //set uri to video
                vidBg.setVideoURI(uri);
                vidBg.start();
                vidBg.setOnPreparedListener((mediaPlayer) -> {
                    mMediaPlayer = mediaPlayer;
                    mMediaPlayer.setLooping(true);

                    //seek current position and play it
                    if (mCurrentVideoPosition !=0){
                        mMediaPlayer.seekTo(mCurrentVideoPosition);
                        mMediaPlayer.start();
                    }
                });

                anim = (AnimationDrawable) topBar.getBackground();
                anim.setEnterFadeDuration(3000);
                anim.setExitFadeDuration(2000);
            }
        }).start();



        spinRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GemsActivity.this, Spin_Activity.class);
                startActivity(intent);
            }
        });

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                if (menuLl.getVisibility() == View.GONE) {
                    menuLl.setVisibility(View.VISIBLE);

                    getViewScaleAnimator(menuLl).setDuration(4000).start();
                }
            }
        }).start();*/
    }

    private Animator getViewScaleAnimator(final View target) {
        // height resize animation
        AnimatorSet animatorSet = new AnimatorSet();
        int desiredHeight = 630;
        int currentHeight = target.getHeight();
        ValueAnimator heightAnimator = ValueAnimator.ofInt(1, desiredHeight);
        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) target.getLayoutParams();
                params.height = (int) animation.getAnimatedValue();
                target.setLayoutParams(params);
            }
        });
        animatorSet.play(heightAnimator);
        return animatorSet;
    }

    @Override
    protected void onStart() {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // yourMethod();
                        if (menuLl.getVisibility() == View.GONE) {
                            menuLl.setVisibility(View.VISIBLE);

                            getViewScaleAnimator(menuLl).setDuration(500).start();
                        }                    }
                }, 3000);

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (anim != null && !anim.isRunning()) {
            anim.start();
        }
        vidBg.resume();
        isBig = true;
    }

    @Override
    protected void onPause() {
        isBig = false;
        super.onPause();
        if (anim != null && anim.isRunning())
            anim.stop();

        //find current position and pause it
        if (mMediaPlayer != null) {
            int currPos = mMediaPlayer.getCurrentPosition();
            mCurrentVideoPosition = currPos;
            vidBg.pause();

        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        //set mediaplayer to null on destroy

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            isBig = false;
        }
    }
}