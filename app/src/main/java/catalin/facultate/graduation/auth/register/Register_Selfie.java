package catalin.facultate.graduation.auth.register;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import catalin.facultate.graduation.R;
import catalin.facultate.graduation.model.User;
import id.zelory.compressor.Compressor;

import static catalin.facultate.graduation.auth.register.Register_Main.REGISTER_USER;

public class Register_Selfie extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private int number = 0;
    private Uri image_uri1 = null;
    private Uri image_uri2 = null;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register__selfie);
    }

    public void GoToCIActivity(View view)
    {
        if(image_uri1 == null || image_uri2 == null)
        {
            Toast.makeText(this, "Trebuie sa incarci doua selfie-uri!", Toast.LENGTH_LONG).show();
            return;
        }
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        ImageView imgView1 = findViewById(R.id.imageView1);
        ImageView imgView2 = findViewById(R.id.imageView2);

        BitmapDrawable drawable = (BitmapDrawable) imgView1.getDrawable();
        Bitmap bitmap1 = drawable.getBitmap();

        drawable = (BitmapDrawable) imgView2.getDrawable();
        Bitmap bitmap2 = drawable.getBitmap();

        Bitmap tempbitmap1 = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas1 = new Canvas(tempbitmap1);
        canvas1.drawBitmap(bitmap1, 0, 0, null);
        Bitmap tempbitmap2 = Bitmap.createBitmap(bitmap2.getWidth(), bitmap2.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas2 = new Canvas(tempbitmap2);
        canvas2.drawBitmap(bitmap2, 0, 0, null);

        Paint rectPaint = new Paint();
        rectPaint.setStrokeWidth(5);
        rectPaint.setColor(Color.WHITE);
        rectPaint.setStyle(Paint.Style.STROKE);
        FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .build();
        if(!faceDetector.isOperational())
        {
            Toast.makeText(this, "Face detector nu funcționează pentru deviceul dumneavoastră",Toast.LENGTH_SHORT).show();
            return;
        }
        Frame frame = new Frame.Builder().setBitmap(bitmap1).build();
        SparseArray<Face> sparseArray = faceDetector.detect(frame);
        if(sparseArray.size() == 0)
        {
            Toast.makeText(this, "Față nedetectată în poza 1. Realizează o nouă fotografie!",Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        for(int i = 0; i<sparseArray.size(); i++)
        {
            Face face = sparseArray.valueAt(i);
            float x1 = face.getPosition().x;
            float y1 = face.getPosition().y;
            float x2 = x1+face.getWidth();
            float y2 = y1+face.getHeight();
            RectF rectf = new RectF(x1, y1, x2, y2);
            canvas1.drawRoundRect(rectf, 2, 2,rectPaint);
        }
        imgView1.setImageDrawable(new BitmapDrawable(getResources(), tempbitmap1));

        Frame frame2 = new Frame.Builder().setBitmap(bitmap2).build();
        SparseArray<Face> sparseArray2 = faceDetector.detect(frame2);
        if(sparseArray2.size() == 0)
        {
            Toast.makeText(this, "Față nedetectată în poza 2. Realizează o nouă fotografie!",Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        for(int i = 0; i<sparseArray2.size(); i++)
        {
            Face face = sparseArray2.valueAt(i);
            float x1 = face.getPosition().x;
            float y1 = face.getPosition().y;
            float x2 = x1+face.getWidth();
            float y2 = y1+face.getHeight();
            RectF rectf = new RectF(x1, y1, x2, y2);
            canvas2.drawRoundRect(rectf, 2, 2,rectPaint);
        }
        imgView2.setImageDrawable(new BitmapDrawable(getResources(), tempbitmap2));

        Intent currentIntent = getIntent();
        User user = (User) currentIntent.getSerializableExtra(REGISTER_USER);
        user.setUriImg1(image_uri1);
        user.setUriImg2(image_uri2);
        Toast.makeText(this, "Detectare facială completă! Stocare imagini...",Toast.LENGTH_LONG).show();
        storageReference = FirebaseStorage.getInstance().getReference();
        uploadImagesToStorage(user.getCNP(), user);


    }

    public void TakePhoto(View view)
    {
        Button captureBtn = null;
        ImageView imageView = null;

        switch(view.getId())
        {
            case R.id.TakePhoto1:
                captureBtn = findViewById(R.id.TakePhoto1);
                imageView = findViewById(R.id.imageView1);
                number = 1;
                break;
            case R.id.TakePhoto2:
                captureBtn = findViewById(R.id.TakePhoto2);
                imageView = findViewById(R.id.imageView2);
                number = 2;
                break;
            default:
                throw new RuntimeException("A button which has an unknown id has been pressed");
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //system os is >= marshmallow
            if(checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED) {
                //request permissions
                String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_CODE);
            }
            else{
                //we already have permission
                OpenPhoneCamera();
            }
        }
        else{
            OpenPhoneCamera();
        }
    }

    private void uploadImagesToStorage(final String CNP, final User user)
    {
        final StorageReference fileReference = storageReference.child("user/"+CNP+"/facial1.jpg");
        fileReference.putFile(image_uri1).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(Register_Selfie.this, "Imaginea 1 s-a uploadat cu succes!", Toast.LENGTH_SHORT).show();
                final StorageReference fileReference2 = storageReference.child("user/"+CNP+"/facial2.jpg");
                fileReference2.putFile(image_uri1).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(Register_Selfie.this, "Imaginea 1 s-a uploadat cu succes!", Toast.LENGTH_SHORT).show();
                        Intent ciIntent = new Intent(Register_Selfie.this, Register_CI.class);
                        ciIntent.putExtra(REGISTER_USER, user);
                        ProgressBar progressBar = findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.INVISIBLE);
                        startActivity(ciIntent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register_Selfie.this, "Imaginea 1 nu a putut fi uploadata!", Toast.LENGTH_SHORT).show();
                        ProgressBar progressBar = findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Register_Selfie.this, "Imaginea 1 nu a putut fi uploadata!", Toast.LENGTH_SHORT).show();
                ProgressBar progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void OpenPhoneCamera()
    {
        ContentValues values = new ContentValues();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String strDate = dateFormat.format(date);
        values.put(MediaStore.Images.Media.TITLE, "Photo"+"-"+number+"-"+strDate);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Photo NR "+number);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        if(number == 1) {
            image_uri1 = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri1);
        }
        else if (number == 2) {
            image_uri2 = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri2);
        }
        else
            throw new RuntimeException("No valid button pressed");
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    OpenPhoneCamera();
                }
                else
                {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            ImageView imageView = null;
            if (number == 1) {
                imageView = findViewById(R.id.imageView1);
                imageView.setImageURI(image_uri1);
            } else if (number == 2) {
                imageView = findViewById(R.id.imageView2);
                imageView.setImageURI(image_uri2);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
