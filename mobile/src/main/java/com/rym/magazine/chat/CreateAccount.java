package com.rym.magazine.chat;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.rym.magazine.R;
import com.rym.magazine.chat.OnlineChatTabs.OnlineChatMain;
import com.rym.magazine.chat.model.UserModel;
import com.rym.magazine.chat.util.SharedPrefUtil;

public class CreateAccount extends AppCompatActivity {
    Toolbar mToolbar;
    Uri uri;
    Intent GalIntent, CropIntent ;
    private Bitmap bitmap,b;
    FileOutputStream out;

    //firebase objects
    private StorageReference storageReference;
    private DatabaseReference mDatabase;
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference();

    private EditText Surname,OtherNames, Password;
    private ImageView profPics;
    private Button Signup, Signin, ChooseImage;
    private String password;

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Showing and Enabling clicks on the Home/Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //getting firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //Initialise Firebase
        storageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_PROFILE);

        //if the objects getcurrentuser method is not null
        //means user is already logged in
        if(firebaseAuth.getCurrentUser() != null) {
            //close this activity
            finish();
            //opening profile activity
            startActivity(new Intent(getApplicationContext(), OnlineChatMain.class));
        }
            progressDialog = new ProgressDialog(this);


            //initializing views
            Surname = (EditText) findViewById(R.id.surname);
            OtherNames = (EditText) findViewById(R.id.other_name);
            Password = (EditText) findViewById(R.id.password);
            profPics = (ImageView)findViewById(R.id.profPics);
            Signin = (Button)findViewById(R.id.signin);
            Signup = (Button)findViewById(R.id.signup);
           Button ChooseImage = (Button)findViewById(R.id.chooseImage);

            //Choose Image
            ChooseImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFileChooser();
                }
            });

            //attaching click listener
            Signin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation animFadein = AnimationUtils.loadAnimation(CreateAccount.this, R.anim.fade_in);
                    Signin.startAnimation(animFadein);
                    userLogin();
            }
            });

            Signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation animFadein = AnimationUtils.loadAnimation(CreateAccount.this, R.anim.fade_in);
                    Signup.startAnimation(animFadein);
                    userSignUp();
                }
            });


    }

    //method for user login
    private void userLogin(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        final String regFullName = Surname.getText().toString().replace(" ", "").trim()+OtherNames.getText().toString().replace(" ", "").trim()+"@gmail.com";
        password  = Password.getText().toString().trim();
        editor.putString("currentPassword",password);
        editor.apply();


        //checking if email and passwords are empty
        if(TextUtils.isEmpty(Surname.getText().toString().trim())){
            Toast.makeText(this, "Enter Surname!", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(OtherNames.getText().toString().trim())){
            Toast.makeText(this, "Enter Other names!", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Enter password!",Toast.LENGTH_LONG).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog

        progressDialog.setMessage("Verifying! Please Wait...");
        progressDialog.show();

        //logging in the user
        firebaseAuth.signInWithEmailAndPassword(regFullName, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        //if the task is successfull
                        if(task.isSuccessful()){
                            //save the user
                            editor.putString("RegUser", regFullName);
                            editor.putString("Surname",Surname.getText().toString().replace(" ", "").trim());
                            editor.putString("OtherName",OtherNames.getText().toString().replace(" ", "").trim());
                            editor.apply();
                            //start the profile activity
                            uploadFile();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Login failed, please review your login details",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void userSignUp(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        final String regFullName = Surname.getText().toString().replace(" ", "")+OtherNames.getText().toString().replace(" ", "").trim()+"@gmail.com";
        String password  = Password.getText().toString().trim();
        editor.putString("currentPassword",password);
        editor.apply();
        if (TextUtils.isEmpty(Surname.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), "Enter Surname!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(OtherNames.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), "Enter other names!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, minimum of 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.equals("rymfamily")) {
            Toast.makeText(getApplicationContext(), "Access denied, please contact RYM!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Verifying! Please Wait...");
        progressDialog.show();
        //create user
        firebaseAuth.createUserWithEmailAndPassword(regFullName, password)
                .addOnCompleteListener(CreateAccount.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(CreateAccount.this, "Failed to create account. Try again!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            //save the user
                            editor.putString("RegUser", regFullName);
                            editor.putString("Surname",Surname.getText().toString().replace(" ", "").trim());
                            editor.putString("OtherName",OtherNames.getText().toString().replace(" ", "").trim());
                            editor.apply();
                            uploadFile();
                        }
                    }
                });
    }

    private void showFileChooser() {
        GalIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(GalIntent, "Select Image From Gallery"), 2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            ImageCropFunction();
        }
        else if (requestCode == 2) {
            if (data != null) {
                uri = data.getData();
                ImageCropFunction();
            }
        }
        else if (requestCode == 1) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = bundle.getParcelable("data");

                profPics.setImageBitmap(bitmap);
                editor.putBoolean("profPics", true);
                editor.apply();
            }
        }
    }

    public void ImageCropFunction() {
        // Image Crop Code
        try {
            CropIntent = new Intent("com.android.camera.action.CROP");
            CropIntent.setDataAndType(uri, "image/*");
            CropIntent.putExtra("crop", "true");
            CropIntent.putExtra("outputX", 200);
            CropIntent.putExtra("outputY", 200);
            CropIntent.putExtra("aspectX", 3);
            CropIntent.putExtra("aspectY", 3);
            CropIntent.putExtra("scale", true);
            CropIntent.putExtra("return-data", true);

            startActivityForResult(CropIntent, 1);
        } catch (ActivityNotFoundException e) {
        }
    }

    //Save image in phone memory
    public void saveImage(Context context, Bitmap bitmap,String name,String extension){
        name=name+"."+extension;
        try {
            out = context.openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Retrieve image and put in imageView
    public Bitmap getImageBitmap(Context context,String name,String extension){
        name=name+"."+extension;
        try{
            FileInputStream fis = context.openFileInput(name);
            b = BitmapFactory.decodeStream(fis);
            profPics.setImageBitmap(b);
            fis.close();
            return b;
        }
        catch(Exception e){
        }
        return null;
    }

    //Upload to firebase
    private void uploadFile() {
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        //checking if file is available
        Boolean savedProfPics = pref.getBoolean("profPics",false);
        if (savedProfPics) {
            //displaying progress dialog while image is uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Creating profile...");
            progressDialog.show();

            //getting the storage reference
            StorageReference sRef = storageReference.child(Constants.STORAGE_PATH_PROFILE + Surname.getText().toString().trim()+" "+OtherNames.getText().toString().trim());

            //adding the file to reference
            profPics.setDrawingCacheEnabled(true);
            profPics.buildDrawingCache();
            bitmap = profPics.getDrawingCache();
            ByteArrayOutputStream cropImg = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, cropImg);
            byte[] data = cropImg.toByteArray();

            UploadTask uploadTask = sRef.putBytes(data);

            uploadTask
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //dismissing the progress dialog
                            progressDialog.dismiss();

                            //displaying success toast
                            Toast.makeText(getApplicationContext(), "Account created", Toast.LENGTH_LONG).show();
                            if(Password.getText().toString().trim().equals("rymfamily")){
                                editor.putBoolean("Counsellor",true);
                                editor.apply();
                            }
                            startActivity(new Intent(CreateAccount.this, OnlineChatMain.class));
                            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                            finish();
                            //Save profile pics
                            saveImage(getApplicationContext(), bitmap, "profile", "pics");

                            //creating the upload object to store uploaded image details
                            UserModel userProfile = new UserModel(Surname.getText().toString().trim()+" "+OtherNames.getText().toString().trim(), taskSnapshot.getDownloadUrl().toString(),"", new SharedPrefUtil(getApplication()).getString(Constants.ARG_FIREBASE_TOKEN));
                            editor.putString("userPicsUrl",taskSnapshot.getDownloadUrl().toString());
                            editor.apply();
                            //adding an upload to firebase database
                            mDatabase.child(Surname.getText().toString().trim()+" "+OtherNames.getText().toString().trim()).setValue(userProfile);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //displaying the upload progress
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Creating profile " + ((int) progress) + "%...");
                        }
                    });
        } else {
            //display an error if no file is selected
            Toast.makeText(getApplicationContext(), "Choose image first", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
        return;
    }
}
