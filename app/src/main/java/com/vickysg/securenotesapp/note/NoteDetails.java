package com.vickysg.securenotesapp.note;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vickysg.securenotesapp.MainActivity;
import com.vickysg.securenotesapp.R;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;


public class NoteDetails extends AppCompatActivity {
    Intent data;

    private AdView mAdView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        //    Ads Code Start Here


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

//    Ads Code Ending Here

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        data = getIntent();


        TextView content = findViewById(R.id.noteDetailsContent);
        TextView title = findViewById(R.id.noteDetailsTitle);
        ImageView displayImageDetails = findViewById(R.id.displayImageDetails);
        TextView dateText = findViewById(R.id.dateText);
        TextView textView = findViewById(R.id.timeText);
        content.setMovementMethod(new ScrollingMovementMethod());

        content.setText(data.getStringExtra("Content"));
        title.setText(data.getStringExtra("Title"));
        dateText.setText("Date :: "+data.getStringExtra("Date"));
        textView.setText("Time :: "+data.getStringExtra("Time"));

        if (data.getStringExtra("ImageUrl").isEmpty()){

        }else {
            displayImageDetails.setVisibility(View.VISIBLE);
            Glide.with(NoteDetails.this).load(data.getStringExtra("ImageUrl"))
                    .into(displayImageDetails);
        }

        content.setBackgroundColor(getResources().getColor(data.getIntExtra("code",0),null));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(view.getContext(),EditNote.class);
                i.putExtra("Title",data.getStringExtra("Title"));
                i.putExtra("Content",data.getStringExtra("Content"));
                i.putExtra("ImageUrl",data.getStringExtra("ImageUrl"));
                i.putExtra("noteId",data.getStringExtra("noteId"));
                startActivity(i);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent intent = new Intent(NoteDetails.this , MainActivity.class);
            overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
