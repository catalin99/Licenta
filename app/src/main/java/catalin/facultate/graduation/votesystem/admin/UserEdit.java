package catalin.facultate.graduation.votesystem.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import catalin.facultate.graduation.MainActivity;
import catalin.facultate.graduation.R;
import catalin.facultate.graduation.auth.login.Login_Main;
import catalin.facultate.graduation.votesystem.vote.VoteSessions;

import static catalin.facultate.graduation.votesystem.admin.UserList.USERID;

public class UserEdit extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore fstore;
    private StorageReference storageReference;

    private EditText nume, prenume, email, cnp, judet, oras, localitate, nationalitate;
    private TextView tip;
    private Spinner tipSpinner;
    private String UserID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
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

        setContentView(R.layout.activity_user_edit);
        fstore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        Intent currIntent = getIntent();
        UserID = currIntent.getStringExtra(USERID);

        PopulateUserDetails();
    }

    public void ApproveUser(View view) {
        SaveData(true);
    }

    public void RejectUser(View view) {
        SaveData(false);
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

    private void SaveData(boolean approved)
    {
        final DocumentReference documentRef= fstore.collection("users").document(UserID);
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("LastName", nume.getText().toString());
        userMap.put("FirstName", prenume.getText().toString());
        userMap.put("Email", email.getText().toString());
        userMap.put("CNP", cnp.getText().toString());
        userMap.put("County", judet.getText().toString());
        userMap.put("Town", oras.getText().toString());
        userMap.put("Locality", localitate.getText().toString());
        tip = (TextView)tipSpinner.getSelectedView();
        userMap.put("TYPE", tip.getText().toString());
        userMap.put("Nationality", nationalitate.getText().toString());
        userMap.put("APPROVED", approved);
        documentRef.update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent userListIntent = new Intent(UserEdit.this, UserList.class);
                startActivity(userListIntent);
            }
        });
    }

    private void PopulateUserDetails() {
        final DocumentReference documentRef = fstore.collection("users").document(UserID);
        documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> user = document.getData();
                    PopulateFields(user);
                }
            }
        });
    }

    private void PopulateFields(Map<String, Object> user) {
        tipSpinner = findViewById(R.id.TypeSpinner);
        String[] items = new String[]{"Normal", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        tipSpinner.setAdapter(adapter);

        nume = findViewById(R.id.LastName);
        prenume = findViewById(R.id.FirstName);
        email = findViewById(R.id.EmailInfo);
        cnp = findViewById(R.id.CNPInfo);
        judet = findViewById(R.id.JUDETInfo);
        oras = findViewById(R.id.ORASInfo);
        localitate = findViewById(R.id.LOCALITATEInfo);

        nationalitate = findViewById(R.id.NationalitateInfo);
        nume.setText(user.get("LastName").toString());
        prenume.setText(user.get("FirstName").toString());
        email.setText(user.get("Email").toString());
        cnp.setText(user.get("CNP").toString());
        judet.setText(user.get("County").toString());
        oras.setText(user.get("Town").toString());
        localitate.setText(user.get("Locality").toString());
        String getType = user.get("TYPE").toString();
        if(getType.equals("Normal"))
            tipSpinner.setSelection(0);
        else
            tipSpinner.setSelection(1);
        nationalitate.setText(user.get("Nationality").toString());

        LoadImages(user.get("CNP").toString());
    }

    private void LoadImages(String CNP) {
        final StorageReference ref1 = storageReference.child("user/" + CNP.trim() + "/facial1.jpg");
        ref1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageView img1 = findViewById(R.id.profile1edit);
                Picasso.get().load(uri)
                        .rotate(-90)
                        .into(img1);
            }
        });
        final StorageReference ref2 = storageReference.child("user/" + CNP.trim() + "/facial2.jpg");
        ref2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageView img2 = findViewById(R.id.profile2edit);
                Picasso.get().load(uri)
                        .rotate(-90)
                        .into(img2);
            }
        });

        final StorageReference ref3 = storageReference.child("user/" + CNP.trim() + "/ci.jpg");
        ref3.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageView img3 = findViewById(R.id.ciphoto);
                Picasso.get().load(uri)
                        .into(img3);
            }
        });
    }
}

