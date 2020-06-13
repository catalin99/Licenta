package catalin.facultate.graduation.auth.register;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import catalin.facultate.graduation.R;
import catalin.facultate.graduation.model.User;

import static catalin.facultate.graduation.auth.register.Register_Main.REGISTER_USER;

public class Register_Selfie extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1000;
    private int number = 0;
    private Uri image_uri1 = null;
    private Uri image_uri2 = null;

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
        Intent currentIntent = getIntent();
        User user = (User) currentIntent.getSerializableExtra(REGISTER_USER);
        user.setUriImg1(image_uri1);
        user.setUriImg2(image_uri2);
        Intent ciIntent = new Intent(this, Register_CI.class);
        ciIntent.putExtra(REGISTER_USER, user);
        startActivity(ciIntent);
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
