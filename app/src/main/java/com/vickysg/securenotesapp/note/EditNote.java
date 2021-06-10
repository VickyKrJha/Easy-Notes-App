package com.vickysg.securenotesapp.note;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vickysg.securenotesapp.MainActivity;
import com.vickysg.securenotesapp.R;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditNote extends AppCompatActivity {
    Intent data;
    EditText editNoteTitle,editNoteContent;
    ImageView showImage ;
    FirebaseFirestore fStore;
    ProgressBar spinner;
    FirebaseUser user;

    private final int REQ = 101 ;

    // this variable is store the image download url
    String downloadUrl = "";

    // Variable for bitmap
    private Bitmap bitmap;

    private DatabaseReference reference;
    private StorageReference storageReference;

    String nTitle , nContent ;

    String date , time ;

    String dateTime ;

    private AdView mAdView;

    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        //    Ads Code Start Here

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest1);

//    Ads Code Ending Here

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-3912259549278001/1934792726", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                mInterstitialAd.show(EditNote.this);


            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error

                mInterstitialAd = null;
            }
        });

        ///


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        fStore = FirebaseFirestore.getInstance();
        spinner = findViewById(R.id.progressBar2);
        user = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();


        data = getIntent();

        editNoteContent = findViewById(R.id.editNoteContent);
        editNoteTitle = findViewById(R.id.editNoteTitle);

        showImage = findViewById(R.id.displayImageDetails);
        View addImage = findViewById(R.id.addImage);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calling Function for Open Gallery
                openGallery();
            }
        });


        String noteTitle = data.getStringExtra("Title");
        String noteContent = data.getStringExtra("Content");
        String noteImage = data.getStringExtra("ImageUrl");

        editNoteTitle.setText(noteTitle);
        editNoteContent.setText(noteContent);
        if (data.getStringExtra("ImageUrl").isEmpty()){

        }else {
            showImage.setVisibility(View.VISIBLE);
            Glide.with(EditNote.this).load(data.getStringExtra("ImageUrl"))
                    .into(showImage);
        }

        //  For Date and Time Both
        dateTime = new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a", Locale.getDefault())
                .format(new Date());

        // For Finding Date and Time
        Calendar calForDate = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yy");
        date = currentDate.format(calForDate.getTime());

        // for Finding Time
        Calendar calForTime = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        time = currentTime.format(calForTime.getTime());


        FloatingActionButton fab = findViewById(R.id.saveEditedNote);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nTitle = editNoteTitle.getText().toString();
                nContent = editNoteContent.getText().toString();



                if(nTitle.isEmpty() || nContent.isEmpty()){
                    Toast.makeText(EditNote.this, "Can not Save note with Empty Field.", Toast.LENGTH_SHORT).show();
                    return;
                }else if (bitmap == null ){   // Image is Stored in bitmap
//                        downloadUrl = data.getStringExtra("ImageUrl");

                    uploadData();

//                    Toast.makeText(EditNote.this, "Please Select Image", Toast.LENGTH_SHORT).show();
                }else{
                    // Calling Function or method for Uploading Image
                    uploadImage();

                }



            }
        });
    }


    // Calling Function or method for Uploading Image
    private void uploadImage() {

        spinner.setVisibility(View.VISIBLE);

        // first Compressing the image then upload image

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] finalImage = baos.toByteArray();

        final StorageReference filePath = storageReference.child("NotesImage").child(finalImage + "jpg");

        final UploadTask uploadTask = filePath.putBytes(finalImage);

        uploadTask.addOnCompleteListener(EditNote.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    // For getting the Path of Image
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl = String.valueOf(uri);
                                    // Calling uploadData()  function or method
                                    uploadData();
                                }
                            });
                        }
                    });
                }else {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(EditNote.this, "Something went Wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
// Ending here , Calling Function or method for Uploading Image



    // Calling Function or method for Uploading Data in FireBase
    private void uploadData() {


        spinner.setVisibility(View.VISIBLE);

        if (bitmap == null){
            downloadUrl = data.getStringExtra("ImageUrl");
        }

        // save note
        String noteId = data.getStringExtra("noteId");

        DocumentReference docref = fStore.collection("Notes").document(user.getUid()).collection("MyNotes").document(noteId);

        Map<String,Object> note = new HashMap<>();
        note.put("Title",nTitle);
        note.put("Content",nContent);
        note.put("ImageUrl",downloadUrl);
        note.put("Date",date);
        note.put("Time",time);
        note.put("DateTime",dateTime);

        docref.update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                spinner.setVisibility(View.GONE);
                Toast.makeText(EditNote.this, "Note Updated Successfully.", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(EditNote.this , MainActivity.class));


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditNote.this, "Error, Try again."+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                spinner.setVisibility(View.GONE);
            }
        });


    }
// Ending here , Calling Function for Uploading Data in FireBase


    // Making or Creating Function for Open Gallery
    private void openGallery() {

        Intent pickImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImage,REQ);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ && resultCode == RESULT_OK){
            Uri uri = data.getData();

            // We Store Image in Bitmap
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            showImage.setVisibility(View.VISIBLE);
            showImage.setImageBitmap(bitmap);
        }
    }
    // End here , Making or Creating Function for Open Gallery


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}

