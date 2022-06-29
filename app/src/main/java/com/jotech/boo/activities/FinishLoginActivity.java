package com.jotech.boo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jotech.boo.R;
import com.jotech.boo.databinding.FinishLoginBinding;
import com.jotech.boo.models.gender_age;


public class FinishLoginActivity extends AppCompatActivity {

    private FinishLoginBinding binding;
    private int bdpick;
    private String Gender;
    com.jotech.boo.models.gender_age gender_age = new gender_age();
    private FirebaseDatabase db;
    private DatabaseReference ref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FinishLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        binding.startBtn.setClickable(false);
        binding.startBtn.setEnabled(false);

        binding.bdPicker.setMinValue(18);
        binding.bdPicker.setMaxValue(70);
        binding.bdPicker.setWrapSelectorWheel(false);

        binding.frb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.mrb.setChecked(false);
                binding.frb.setChecked(true);
                Gender = binding.frb.getText().toString();
                gender_age.setGender(Gender);
                enableBtn();
            }
        });

        binding.mrb.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                    binding.frb.setChecked(false);
                    binding.mrb.setChecked(true);
                    Gender = binding.mrb.getText().toString();
                    gender_age.setGender(Gender);
                    enableBtn();
            }
        });


            binding.startBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    bdpick = binding.bdPicker.getValue();
                    gender_age.setAge(bdpick);
                    gender_age = new gender_age(gender_age.getGender(), gender_age.getAge());

                    db = FirebaseDatabase.getInstance();
                    FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
                    ref = db.getReference("Profiles");
                    ref.child(mAuth.getUid()).child("gender").setValue(gender_age.getGender());

                    ref.child(mAuth.getUid()).child("age").setValue(gender_age.getAge());
                    Toast.makeText(FinishLoginActivity.this, ""+ gender_age.getGender()+" "+gender_age.getAge(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(FinishLoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
    }






    private void enableBtn() {
        binding.startBtn.setBackgroundResource(R.drawable.btnsub);
        binding.startBtn.isClickable();
        binding.startBtn.setClickable(true);
        binding.startBtn.isEnabled();
        binding.startBtn.setEnabled(true);
        binding.startBtn.setTextColor(getResources().getColor(R.color.white));
    }

}