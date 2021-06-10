package com.vickysg.securenotesapp.note;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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

public class AddNote extends AppCompatActivity {
    FirebaseFirestore fStore;
    EditText noteTitle,noteContent;
    ImageView addImg;
    ProgressBar progressBarSave;
    FirebaseUser user;

    View addImage ;

    private final int REQ = 1 ;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //    Ads Code Start Here

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

//    Ads Code Ending Here


        fStore = FirebaseFirestore.getInstance();
        noteContent = findViewById(R.id.addNoteContent);
        noteTitle = findViewById(R.id.addNoteTitle);
        progressBarSave = findViewById(R.id.progressBar);

        reference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        user = FirebaseAuth.getInstance().getCurrentUser();

        //  For Date and Time Both
        dateTime = new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a", Locale.getDefault())
                .format(new Date());

        // For Finding Date and Time
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yy");
        date = currentDate.format(calForDate.getTime());

        // for Finding Time
        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        time = currentTime.format(calForTime.getTime());



        addImg = findViewById(R.id.displayImageDetails);
        addImage = findViewById(R.id.addImage);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calling Function for Open Gallery
                openGallery();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nTitle = noteTitle.getText().toString();
                nContent = noteContent.getText().toString();

                if(nTitle.isEmpty() || nContent.isEmpty()){
                    Toast.makeText(AddNote.this, "Can not Save note with Empty Field.", Toast.LENGTH_SHORT).show();
                    return;
                }else if (bitmap == null){   // Image is Stored in bitmap
                    uploadData();
//                    Toast.makeText(AddNote.this, "Please Select Image", Toast.LENGTH_SHORT).show();
                }else{
                    // Calling Function or method for Uploading Image
                    uploadImage();
                }



            }
        });
    }


    // Calling Function or method for Uploading Image
    private void uploadImage() {

      progressBarSave.setVisibility(View.VISIBLE);

        // first Compressing the image then upload image

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] finalImage = baos.toByteArray();

        final StorageReference filePath = storageReference.child("NotesImage").child(finalImage + "jpg");

        final UploadTask uploadTask = filePath.putBytes(finalImage);

        uploadTask.addOnCompleteListener(AddNote.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
                    progressBarSave.setVisibility(View.GONE);
                    Toast.makeText(AddNote.this, "Something went Wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
// Ending here , Calling Function or method for Uploading Image



    // Calling Function or method for Uploading Data in FireBase
    private void uploadData() {


        progressBarSave.setVisibility(View.VISIBLE);

        // save note

        DocumentReference docref = fStore.collection("Notes").document(user.getUid()).collection("MyNotes").document();
        Map<String,Object> note = new HashMap<>();
        note.put("Title",nTitle);
        note.put("Content",nContent);
        note.put("ImageUrl",downloadUrl);
        note.put("Date",date);
        note.put("Time",time);
        note.put("DateTime",dateTime);

        docref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBarSave.setVisibility(View.GONE);

                Toast.makeText(AddNote.this, "Note Added.", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(AddNote.this , MainActivity.class);

                startActivity(i);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddNote.this, "Error, Try again.", Toast.LENGTH_SHORT).show();
                progressBarSave.setVisibility(View.GONE);
            }
        });

    }
// Ending here , Calling Function for Uploading Data in FireBase

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.close_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.close){
            Toast.makeText(this,"Not Saved.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddNote.this , MainActivity.class);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }


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
            addImg.setVisibility(View.VISIBLE);
            addImg.setImageBitmap(bitmap);
        }
    }
    // End here , Making or Creating Function for Open Gallery


}
