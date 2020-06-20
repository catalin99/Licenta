package catalin.facultate.graduation.votesystem.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.Line;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import catalin.facultate.graduation.MainActivity;
import catalin.facultate.graduation.R;
import catalin.facultate.graduation.auth.login.Login_FaceRecognition;
import catalin.facultate.graduation.auth.login.Login_Main;
import catalin.facultate.graduation.auth.register.Register_CI;
import dmax.dialog.SpotsDialog;

public class NewVote extends AppCompatActivity {

    private Calendar myCalendar;
    private EditText edittext;
    private String VoteID = null;
    private int counter;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore fstore;
    private StorageReference storageReference;
    private AlertDialog alertDialog;
    private Uri ActualImg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth= FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null)
        {
            Intent loginIntent = new Intent(this, Login_Main.class);
            Toast.makeText(this, "Trebuie să fii autentificat pentru a putea accesa această pagină", Toast.LENGTH_LONG).show();
            startActivity(loginIntent);
        }
//        else if (IsAdmin() == false)
//        {
//            Intent mainIntent = new Intent(this, MainActivity.class);
//            Toast.makeText(this, "Trebuie să fii admin pentru a putea accesa această pagină", Toast.LENGTH_LONG).show();
//            startActivity(mainIntent);
//        }

        setContentView(R.layout.activity_new_vote);

        counter = 0;
        myCalendar = Calendar.getInstance();
        edittext= (EditText) findViewById(R.id.DatePickerInfo);


        fstore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        alertDialog =  new SpotsDialog.Builder()
                .setContext(NewVote.this)
                .setCancelable(false)
                .build();

        final Spinner dropdown = findViewById(R.id.VisibilitySpinner);

        String[] items = new String[]{"ȚARĂ", "JUDEȚ", "LOCALITATE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView exactLocation = findViewById(R.id.LocatieExactaInfo);
                TextView spinner = (TextView)dropdown.getSelectedView();
                if(spinner.getText().toString().equals("ȚARĂ")) {
                    exactLocation.setText("Disabled");
                    exactLocation.setEnabled(false);
                }
                else
                {
                    exactLocation.setEnabled(true);
                    exactLocation.setText("");
                    exactLocation.setHint("Ex: {2}=Sector 2, Buc; {Constanța} = Județ");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        edittext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(NewVote.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private boolean IsAdmin()
    {
        final boolean[] admin = new boolean[1];
        final DocumentReference documentRef = fstore.collection("users").document(firebaseAuth.getCurrentUser().getUid());
        documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> user = document.getData();

                    if(user.get("TYPE").toString().equals("Admin"))
                        admin[0] = true;
                    else
                        admin[0] = false;
                }
            }
        });
        return admin[0];
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        edittext.setText(sdf.format(myCalendar.getTime()));
    }

    public void PickPhoto(View view){
        Intent intentPhoto = new Intent();
        intentPhoto.setType("image/*");
        intentPhoto.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentPhoto, "Select Picture"), 200);
    }

    public void AcceptVote(View view)
    {
        if(counter < 2)
        {
            Toast.makeText(this, "Imposibil! Sunt mai puțin de două opțiuni!", Toast.LENGTH_LONG).show();
            return;
        }

        DocumentReference documentRef= fstore.collection("Activities").document(VoteID);
        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("Finalized", true);
        documentRef.update(activityMap);
        Toast.makeText(this, "Sesiune de vota salvată", Toast.LENGTH_LONG).show();
    }

    public void SetDetalis(View view)
    {
        TextView title, locatie, data;
        Spinner vizibilitate;
        title = findViewById(R.id.TitleInfo);
        vizibilitate = findViewById(R.id.VisibilitySpinner);
        TextView spinner = (TextView)vizibilitate.getSelectedView();
        locatie = findViewById(R.id.LocatieExactaInfo);
        data = findViewById(R.id.DatePickerInfo);
        if(title.getText()==null || locatie.getText()==null || data.getText()==null || title.getText().toString().isEmpty() || locatie.getText().toString().isEmpty() || data.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Date necompletate sau incorecte", Toast.LENGTH_LONG).show();
            return;
        }
        alertDialog.setMessage("Se salvează datele...");
        alertDialog.show();


        final DocumentReference documentReference = fstore.collection("Activities").document();

        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("VoteTitle",title.getText().toString());
        activityMap.put("Visibility",spinner.getText().toString());
        activityMap.put("Location", locatie.getText().toString());
        activityMap.put("ActiveDate", data.getText().toString());
        activityMap.put("UserCreator", firebaseAuth.getCurrentUser().getUid());
        activityMap.put("Active", true);
        activityMap.put("Finalized", false);
        activityMap.put("TotalVotes", 0);
        activityMap.put("Winner", "NONE");
        documentReference.set(activityMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("AUTH", "OnSucces: Vote Activity added");
                alertDialog.dismiss();
                VoteID = documentReference.getId();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("AUTH", "OnFailure: " + e.toString());
                alertDialog.dismiss();
            }
        });
    }

    public void AddOption(View view)
    {
        if(VoteID == null)
        {
            Toast.makeText(this, "Trebuie să setezi detaliile pentru început", Toast.LENGTH_LONG).show();
            return;
        }
        if(ActualImg == null)
        {
            Toast.makeText(this, "Trebuie să setezi o imagine pentru opțiune", Toast.LENGTH_LONG).show();
            return;
        }
        TextView optionInfoTemp = findViewById(R.id.OptionInfo);
        if(optionInfoTemp.getText().toString().isEmpty() || optionInfoTemp.getText() == null)
        {
            Toast.makeText(this, "Trebuie să setezi un nume pentru opțiune", Toast.LENGTH_LONG).show();
            return;
        }

        alertDialog.setMessage("Se încarcă opțiunea în baza de date");
        alertDialog.show();
        final int contextCount = counter+1;
        final StorageReference fileReference = storageReference.child("Options/"+VoteID+"/"+contextCount+".jpg");
        fileReference.putFile(ActualImg).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                TextView optionInfo = findViewById(R.id.OptionInfo);
                final DocumentReference documentRefOption = fstore.collection("Options").document();
                Map<String, Object> optionMap = new HashMap<>();
                optionMap.put("ActivityID", VoteID);
                optionMap.put("OptionTitle",optionInfo.getText().toString());
                optionMap.put("ImgLocation","Options/"+VoteID+"/"+contextCount+".jpg");
                optionMap.put("Votes", 0);
                optionMap.put("Cancelled", false);
                documentRefOption.set(optionMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("AUTH", "OnSucces: Vote Activity added");
                        alertDialog.dismiss();

                        //add data on frontend
                        AddNewOption();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("AUTH", "OnFailure: " + e.toString());

                        alertDialog.dismiss();
                        Toast.makeText(NewVote.this, "Eroare la încărcarea datelor. Reîncearcă", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                alertDialog.dismiss();
                Toast.makeText(NewVote.this, "Eroare la încărcarea pozei. Reîncearcă", Toast.LENGTH_LONG).show();
            }
        });
        alertDialog.dismiss();

    }

    private void AddNewOption()
    {
        LinearLayout currLayout = findViewById(R.id.FinalLayout);
        TextView OptionInfo = findViewById(R.id.OptionInfo);
        ImageView ImageOption = findViewById(R.id.pictureVote);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // Width
                RelativeLayout.LayoutParams.MATCH_PARENT); // Height

        int leftright = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 30, getResources()
                        .getDisplayMetrics());
        int topbottom = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, getResources()
                        .getDisplayMetrics());

        int textSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 18, getResources()
                        .getDisplayMetrics());

        lp.setMargins(0, topbottom, 0, topbottom);

        TextView optionName = new TextView(getApplicationContext());
        optionName.setLayoutParams(lp);
        optionName.setTextSize(textSize);
        optionName.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.textColor));
        optionName.setText(OptionInfo.getText().toString().toUpperCase());
        optionName.setGravity(Gravity.CENTER_VERTICAL);

        lp.setMargins(leftright, topbottom, 0, topbottom);
        TextView number = new TextView(getApplicationContext());
        number.setLayoutParams(lp);
        number.setTextSize(textSize);
        number.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.textColor));
        counter++;
        String strNumber = Integer.toString(counter) + ".";
        number.setText(strNumber);
        number.setGravity(Gravity.CENTER_VERTICAL);

        Space space = new Space(getApplicationContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));

        ImageView Picture = new ImageView(getApplicationContext());
        int width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 80, getResources()
                        .getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 95, getResources()
                        .getDisplayMetrics());
        RelativeLayout.LayoutParams lpicture = new RelativeLayout.LayoutParams(
                width,
                height);
        lpicture.setMargins(0, topbottom, leftright, topbottom);

        Picture.setLayoutParams(lpicture);
        Picture.setImageDrawable(ImageOption.getDrawable());
        Picture.setForegroundGravity(Gravity.RIGHT);

        LinearLayout newVerticalLayout = new LinearLayout(getApplicationContext());
        RelativeLayout.LayoutParams vlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        vlp.setMargins(0, 0, 0, topbottom);
        newVerticalLayout.setLayoutParams(vlp);
        newVerticalLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.backgroundColorVoteOption));
        newVerticalLayout.setOrientation(LinearLayout.HORIZONTAL);

        newVerticalLayout.addView(number);
        newVerticalLayout.addView(optionName);
        newVerticalLayout.addView(space);
        newVerticalLayout.addView(Picture);

        currLayout.addView(newVerticalLayout);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (resultCode == RESULT_OK) {
            if (requestCode == 200) {
                Uri selectedImageUri = imageReturnedIntent.getData();
                if (null != selectedImageUri) {
                    ImageView picture =  findViewById(R.id.pictureVote);
                    picture.setImageURI(selectedImageUri);
                    ActualImg = selectedImageUri;
                }
            }
        }
    }

}
