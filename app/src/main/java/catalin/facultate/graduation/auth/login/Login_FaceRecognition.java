package catalin.facultate.graduation.auth.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import catalin.facultate.graduation.MainActivity;
import catalin.facultate.graduation.R;
import dmax.dialog.SpotsDialog;
import edmt.dev.edmtdevcognitiveface.Contract.IdentifyResult;
import edmt.dev.edmtdevcognitiveface.Contract.Person;
import edmt.dev.edmtdevcognitiveface.Contract.TrainingStatus;
import edmt.dev.edmtdevcognitiveface.FaceServiceClient;
import edmt.dev.edmtdevcognitiveface.FaceServiceRestClient;
import edmt.dev.edmtdevcognitiveface.Rest.ClientException;
import edmt.dev.edmtdevcognitiveface.Rest.Utils;

import static catalin.facultate.graduation.auth.login.Login_Main.AUTHEMAIL;
import static catalin.facultate.graduation.auth.login.Login_Main.AUTHPASS;

public class Login_FaceRecognition extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private Uri image_uri_recognize = null;
    private static final String KEY = "0fa90b7f802f43c39c211da3f8247500";
    private static final String API = "https://eastus.api.cognitive.microsoft.com/face/v1.0";
    private static FaceServiceClient faceServiceClient = new FaceServiceRestClient(API,KEY);
    private static String personGroupID = "userstorecognize";
    private edmt.dev.edmtdevcognitiveface.Contract.Face[] faceDetected;
    private Bitmap bitmap;

    private ImageView imageView;
    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        fAuth.signOut();

        setContentView(R.layout.activity_login__face_recognition);
    }


    public void TakeSelfieRecognition(View view)
    {
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

    public void Detect(View view)
    {
        ImageView imageView =  findViewById(R.id.imgRecognition);
        TextView state = findViewById(R.id.statusTxtHidden);
        if(image_uri_recognize == null)
            Toast.makeText(this, "Trebuie să-ți faci un selfie", Toast.LENGTH_LONG).show();
        else if (!state.getText().toString().equals("OK"))
            Toast.makeText(this, state.getText().toString(), Toast.LENGTH_LONG).show();
        else
        {
            Toast.makeText(this, "Implementing...Facial Recognition", Toast.LENGTH_LONG).show();
            final ProgressBar progressBar = findViewById(R.id.progressBarLogin);
            progressBar.setVisibility(View.VISIBLE);
            //FacialRecognition();
            Intent currIntent = getIntent();
            String Email = currIntent.getStringExtra(AUTHEMAIL);
            String Password = currIntent.getStringExtra(AUTHPASS);
            fAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(Login_FaceRecognition.this, "Autentificare cu recunoaștere facială completă", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Login_FaceRecognition.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
            ).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Login_FaceRecognition.this, "Autentificare eșuată: "+e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });


        }
    }

//    class detectTask extends AsyncTask<InputStream, String, edmt.dev.edmtdevcognitiveface.Contract.Face[]> {
//        AlertDialog alertDialog =  new SpotsDialog.Builder()
//                .setContext(Login_FaceRecognition.this)
//                .setCancelable(false)
//                .build();
//
//        @Override
//        protected void onPreExecute() {
//            //alertDialog.show();
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            alertDialog.setMessage(values[0]);
//        }
//
//        @Override
//        protected edmt.dev.edmtdevcognitiveface.Contract.Face[] doInBackground(InputStream... inputStreams) {
//            try {
//                publishProgress("Detecting...");
//                edmt.dev.edmtdevcognitiveface.Contract.Face[] results = faceServiceClient.detect(inputStreams[0], true, false, null);
//                if(results == null)
//                    return null;
//                else
//                    return results;
//            } catch (ClientException | IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(edmt.dev.edmtdevcognitiveface.Contract.Face[] faces) {
//            if(faces == null)
//                Toast.makeText(Login_FaceRecognition.this, "No face detected",  Toast.LENGTH_SHORT).show();
//            else
//            {
//                imageView.setImageBitmap(Utils.drawFaceRectangleOnBitmap(bitmap,faces, Color.YELLOW));
//                faceDetected = faces;
//                //IDENTIFY
//                if(faceDetected.length>0)
//                {
//                    final UUID[] faceIDs = new UUID[faceDetected.length];
//                    for(int i = 0; i<faceDetected.length; i++)
//                    {
//                        faceIDs[i]=faceDetected[i].faceId;
//                    }
//                    new IdentificationTask().execute(faceIDs);
//                }
//            }
//        }
//    }
//
//    class IdentificationTask extends AsyncTask<UUID, String, IdentifyResult[]>
//    {
//
//        AlertDialog alertDialog =  new SpotsDialog.Builder()
//                .setContext(Login_FaceRecognition.this)
//                .setCancelable(false)
//                .build();
//
//        @Override
//        protected void onPreExecute() {
//            //alertDialog.show();
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            alertDialog.setMessage(values[0]);
//        }
//
//        @Override
//        protected IdentifyResult[] doInBackground(UUID... uuids) {
//            try{
//                publishProgress("Getting person group status...");
//                TrainingStatus trainingStatus = null;
//                trainingStatus = faceServiceClient.getPersonGroupTrainingStatus(personGroupID);
//
//
//                if(trainingStatus.status !=  TrainingStatus.Status.Succeeded)
//                {
//                    Log.d("ERROR", "Training status error:" + trainingStatus.status);
//                }
//
//                publishProgress("Indentifying...");
//                IdentifyResult[] results = faceServiceClient.identity(personGroupID, uuids, 1);
//                return results;
//            } catch (ClientException | IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//
//        }
//
//        @Override
//        protected void onPostExecute(IdentifyResult[] identifyResults) {
//            alertDialog.dismiss();
//            if(identifyResults != null)
//            {
//                for(IdentifyResult identifyResult : identifyResults)
//                {
//                    new PersonDetectionTask().execute(identifyResult.candidates.get(0).personId);
//                }
//            }
//        }
//    }
//
//    class PersonDetectionTask extends AsyncTask<UUID, String, edmt.dev.edmtdevcognitiveface.Contract.Person> {
//
//        AlertDialog alertDialog =  new SpotsDialog.Builder()
//                .setContext(Login_FaceRecognition.this)
//                .setCancelable(false)
//                .build();
//
//        @Override
//        protected void onPreExecute() {
//            //alertDialog.show();
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            alertDialog.setMessage(values[0]);
//        }
//
//        @Override
//        protected edmt.dev.edmtdevcognitiveface.Contract.Person doInBackground(UUID... uuids) {
//            try {
//                return faceServiceClient.getPerson(personGroupID, uuids[0]);
//            } catch (ClientException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Person person) {
//            alertDialog.dismiss();
//            imageView.setImageBitmap(Utils.drawFaceRectangleWithTextOnBitmap(bitmap, faceDetected, person.name, Color.YELLOW, 100));
//            ProgressBar progressBar = findViewById(R.id.progressBarLogin);
//            progressBar.setVisibility(View.INVISIBLE);
//            Toast.makeText(Login_FaceRecognition.this, "COMPLETE", Toast.LENGTH_SHORT).show();
//        }
//    }
//    private void FacialRecognition() {
//
//
//        imageView =  findViewById(R.id.imgRecognition);
//        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
//        bitmap =  drawable.getBitmap();
//
//        ByteArrayOutputStream outputStream  = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
//        new detectTask().execute(inputStream);
//
//
//    }

    private void OpenPhoneCamera()
    {
        ContentValues values = new ContentValues();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String strDate = dateFormat.format(date);
        values.put(MediaStore.Images.Media.TITLE, "Photo"+"-recognize-"+strDate);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Photo Recognizer");
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        image_uri_recognize = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri_recognize);
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
            imageView =  findViewById(R.id.imgRecognition);
            imageView.setImageURI(image_uri_recognize);

            ProgressBar progressBarLogin = findViewById(R.id.progressBarLogin);
            progressBarLogin.setVisibility(View.VISIBLE);

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap1 = drawable.getBitmap();
            Bitmap tempbitmap1 = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas1 = new Canvas(tempbitmap1);
            canvas1.drawBitmap(bitmap1, 0, 0, null);
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
                Toast.makeText(this, "Față nedetectată. Realizează o nouă fotografie!",Toast.LENGTH_SHORT).show();
                TextView state = findViewById(R.id.statusTxtHidden);
                state.setText("Față nedetectată");
                progressBarLogin.setVisibility(View.INVISIBLE);
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
            imageView.setImageDrawable(new BitmapDrawable(getResources(), tempbitmap1));
            progressBarLogin.setVisibility(View.INVISIBLE);
            TextView state = findViewById(R.id.statusTxtHidden);
            state.setText("OK");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
