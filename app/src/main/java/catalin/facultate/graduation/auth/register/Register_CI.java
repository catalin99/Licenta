package catalin.facultate.graduation.auth.register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import catalin.facultate.graduation.R;
import catalin.facultate.graduation.auth.login.Login_Main;
import catalin.facultate.graduation.model.User;

import static catalin.facultate.graduation.auth.register.Register_Main.REGISTER_USER;

public class Register_CI extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1000;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    String userID;
    StorageReference storageReference;
    Uri resultUriCI = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register__ci);
    }

    public void OpenPhoneCameraCI()
    {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowRotation(true)
                .start(this);
    }

    public void TakePhotoCI(View view)
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
                OpenPhoneCameraCI();
            }
        }
        else{
            OpenPhoneCameraCI();
        }
    }

    public void GoToLogin(View view)
    {
        TextView status = findViewById(R.id.statusTxt);
        if(status.getText().equals("Status: Date corecte!"))
        {
            final Intent currentIntent = getIntent();
            final User user = (User)currentIntent.getSerializableExtra(REGISTER_USER);
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            storageReference = FirebaseStorage.getInstance().getReference();


            uploadImageCIToStorageAndSaveUserData(user.getCNP(), user, progressBar);

        }
        else
        {
            Toast.makeText(this, "Error: Documente încărcate greșit/Date invalide", Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void uploadImageCIToStorageAndSaveUserData(String CNP, final User user, final ProgressBar progressBar) {
        final StorageReference fileReference = storageReference.child("user/"+CNP+"/ci.jpg");
        fileReference.putFile(resultUriCI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(Register_CI.this, "Imaginea s-a uploadat cu succes!", Toast.LENGTH_SHORT).show();
                try {
                    firebaseAuth=FirebaseAuth.getInstance();
                    fstore = FirebaseFirestore.getInstance();
                    firebaseAuth.createUserWithEmailAndPassword(user.getEmail(),user.getPASSWORD()).addOnCompleteListener(
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(Register_CI.this, "User created", Toast.LENGTH_LONG).show();
                                        userID = firebaseAuth.getCurrentUser().getUid();
                                        DocumentReference documentReference = fstore.collection("users").document(userID);
                                        Map<String, Object> usrMap = new HashMap<>();
                                        usrMap.put("Email", user.getEmail());
                                        usrMap.put("CNP", user.getCNP());
                                        usrMap.put("LastName", user.getNume());
                                        usrMap.put("FirstName", user.getPrenume());
                                        usrMap.put("Serie", user.getSerie());
                                        usrMap.put("Number", user.getNumar());
                                        usrMap.put("Nationality", user.getNationalitate());
                                        usrMap.put("County", user.getJudet());
                                        usrMap.put("Town", user.getOras());
                                        usrMap.put("Locality", user.getLocalitate());
                                        usrMap.put("Gender", user.getGender());
                                        usrMap.put("Birthday", user.getDataNastere().toString());
                                        usrMap.put("TYPE", user.getUserType());
                                        usrMap.put("APPROVED", false);
                                        documentReference.set(usrMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("AUTH", "OnSucces: user created");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("AUTH", "OnFailure: " + e.toString());
                                            }
                                        });


                                        Intent loginIntent = new Intent(Register_CI.this, Login_Main.class);
                                        startActivity(loginIntent);
                                    }
                                    else
                                    {
                                        Toast.makeText(Register_CI.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                    );

                }
                catch(Exception e)
                {
                    Toast.makeText(Register_CI.this, "Error: Database register error. Try Again", Toast.LENGTH_LONG).show();
                }
                finally {
                    progressBar.setVisibility(View.INVISIBLE);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Register_CI.this, "Imaginea 1 nu a putut fi uploadata!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    OpenPhoneCameraCI();
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

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                ImageView imageView = null;
                imageView = findViewById(R.id.imgCI);
                imageView.setImageURI(resultUri);
                TextView status = findViewById(R.id.statusTxt);
                status.setText("Status : Imagine încărcată. Se verifică...");
                resultUriCI = resultUri;

                Boolean recognizeResult = RecognizeTextFromCI(imageView, resultUri);
                if(recognizeResult)
                {
                    ValidateUserData();
                }
                else
                {
                    status.setText("Datele nu au putut fi extrase. Te rugăm să încarci o nouă fotografie");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Boolean RecognizeTextFromCI(ImageView imgViewCI, Uri resultUri)
    {
        BitmapDrawable bitmapCI = (BitmapDrawable)imgViewCI.getDrawable();
        Bitmap bitmap = bitmapCI.getBitmap();
        TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if(!recognizer.isOperational())
        {
            Toast.makeText(this, "Text Recognizer error", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = recognizer.detect(frame);
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<items.size(); i++)
            {
                TextBlock myItem = items.valueAt(i);
                sb.append(myItem.getValue());
                sb.append("\n");
            }
            String text = sb.toString();
            String[] parts = text.split("\n");
            Boolean makeUser = MakeUser(parts, resultUri);
            TextView status = findViewById(R.id.statusTxt);
            if(makeUser) {
                status.setText("Status: Date extrase cu succes");
                return true;
            }
        }
        return false;
    }

    private Boolean MakeUser(String[] lines, Uri resultUri)
    {
        Intent currentIntent = getIntent();
        User user = (User)currentIntent.getSerializableExtra(REGISTER_USER);
        user.setUriImgCI(resultUri);
        try {
            for (int i = 0; i < lines.length - 1; i++) {
                String temp = lines[i].toUpperCase();
                if (temp.contains("SERIA") && temp.contains("NR")) {
                    String splitLine[] = temp.split(" ");
                    user.setSerie(splitLine[1]);
                    user.setNumar(Integer.parseInt(splitLine[3]));

                }
                else if(temp.contains("NATIONALITY"))
                {
                    String splitLine[] = lines[i+1].toUpperCase().split(" ");
                    user.setNationalitate(splitLine[0]);
                }else if (temp.contains("NUME/NOM/LAST NAME")) {
                    user.setNume(lines[i + 1].toUpperCase());
                } else if (temp.contains("PRENUME/PRENOM/FIRST NAME")) {
                    user.setPrenume(lines[i + 1].toUpperCase());
                } else if (i > 0 && !lines[i - 1].toUpperCase().contains("PLACE OF BIRTH")) {
                    if (temp.contains("JUD") && temp.contains("SAT") && temp.contains("COM")) {
                        //localitate
                        String splitLine[] = temp.split(" ");
                        user.setJudet(splitLine[0].substring(4));
                        user.setOras("-");
                        user.setLocalitate(splitLine[2].substring(5, splitLine[2].length()-1));
                    } else if (temp.contains("JUD") && temp.contains("MUN")) {
                        //oras
                        String splitLine[] = temp.split(" ");
                        user.setJudet(splitLine[0].substring(4));
                        user.setOras(splitLine[1].substring(4));
                        user.setLocalitate("-");
                    } else if (temp.contains("MUN") && temp.contains("SEC")) {
                        //bucharest
                        String splitLine[] = temp.split(" ");
                        user.setJudet("BUC");
                        user.setOras(splitLine[0].substring(4));
                        user.setLocalitate("SECTOR " + splitLine[1].substring(4, 5));
                    }
                }

                else if(temp.contains("CNP"))
                {
                    String splitLine[] = temp.split(" ");
                    if(!splitLine[1].equals(user.getCNP()) && splitLine[1].length()==13)
                        return false;
                }

            }
        }
        catch (Exception e)
        {
            return false;
        }

        currentIntent.putExtra(REGISTER_USER, user);
        return true;
    }

    private void ValidateUserData()
    {
        TextView status = findViewById(R.id.statusTxt);
        Intent currentIntent = getIntent();
        User user = (User)currentIntent.getSerializableExtra(REGISTER_USER);
        if(user.getCNP().length()!=13) {
            status.setText("Status: CNP invalid!");
            return;
        }
        if(user.getJudet()==null || user.getJudet().isEmpty() || user.getLocalitate().isEmpty() || user.getOras().isEmpty()) {
            status.setText("Status: Adresa domiciliu ivalida!");
            return;
        }
        if(user.getNume()==null || user.getNume().isEmpty()) {
            status.setText("Status: Nume invalid");
            return;
        }
        if(user.getPrenume()==null || user.getPrenume().isEmpty()) {
            status.setText("Status: Prenume invalid");
            return;
        }
        if(user.getNationalitate()==null || user.getNationalitate().isEmpty())
        {
            status.setText("Status: Nationalitate invalidă");
            return;
        }
        status.setText("Status: Date corecte!");
    }
}
