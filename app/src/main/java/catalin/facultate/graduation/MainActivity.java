package catalin.facultate.graduation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Map;

import catalin.facultate.graduation.auth.login.Login_Main;
import catalin.facultate.graduation.auth.register.Register_Main;
import catalin.facultate.graduation.votesystem.admin.NewVote;
import catalin.facultate.graduation.votesystem.admin.UserList;
import catalin.facultate.graduation.votesystem.vote.VoteSessions;
import io.reactivex.annotations.NonNull;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;
    String userID;
    String CNP;
    FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        //            fAuth.signOut();

        if(fAuth.getCurrentUser() == null)
        {
            Intent startIntent = new Intent(this, Login_Main.class);
            startActivity(startIntent);
        }
        else {
            userID = fAuth.getCurrentUser().getUid();
            setContentView(R.layout.activity_main);
            AdminCheck();
            LoadProfileDetails();

        }
    }


    public void GoToAdminPanel(View view)
    {
        Intent adminIntent = new Intent(this, NewVote.class);
        startActivity(adminIntent);
    }

    public void GoToVoteSessions(View view)
    {
        Intent adminIntent = new Intent(this, VoteSessions.class);
        startActivity(adminIntent);
    }

    public void GoToUserPanel(View view)
    {
        Intent adminUserIntent = new Intent(this, UserList.class);
        startActivity(adminUserIntent);
    }

    public void LogOut(View view)
    {
        fAuth.signOut();
        Intent loginIntent = new Intent(this, Login_Main.class);
        startActivity(loginIntent);
    }

    private void AdminCheck()
    {
        final DocumentReference documentRef = fStore.collection("users").document(userID);
        documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> user = document.getData();
                    if(user == null)
                    {
                        fAuth.signOut();
                        Intent startIntent = new Intent(MainActivity.this, Login_Main.class);
                        startActivity(startIntent);
                    }
                    boolean admin;
                    if(user.get("TYPE").toString().equals("Admin"))
                        admin = true;
                    else
                        admin = false;
                    if (admin == false)
                    {
                        Button adminNewVote = findViewById(R.id.AdminBtm);
                        Button adminUserManagement = findViewById(R.id.AdminUSERBtn);
                        adminNewVote.setVisibility(View.INVISIBLE);
                        adminUserManagement.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    private void LoadProfileDetails()
    {
        final TextView name, email, cnp, judet, oras, localitate, type;
        name = findViewById(R.id.FirstLastName);
        email = findViewById(R.id.EmailInfo);
        cnp = findViewById(R.id.CNPInfo);
        judet = findViewById(R.id.JUDETInfo);
        oras = findViewById(R.id.ORASInfo);
        localitate = findViewById(R.id.LOCALITATEInfo);
        type = findViewById(R.id.TYPEInfo);

        DocumentReference docReference = fStore.collection("users").document(userID);
        docReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                String nameFinal = documentSnapshot.getString("LastName") + " " + documentSnapshot.getString("FirstName");
                name.setText(nameFinal);
                email.setText(documentSnapshot.getString("Email"));
                cnp.setText(documentSnapshot.getString("CNP"));
                judet.setText(documentSnapshot.getString("County"));
                oras.setText(documentSnapshot.getString("Town"));
                localitate.setText(documentSnapshot.getString("Locality"));
                type.setText("Normal user");

                //Load profile pictures
                LoadImages(documentSnapshot.getString("CNP"));
            }
        });

    }

    private void LoadImages(String CNP)
    {
        final StorageReference ref1 = storageReference.child("user/"+CNP.trim()+"/facial1.jpg");
        ref1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageView img1 = findViewById(R.id.profile1);
                Picasso.get().load(uri)
                        .rotate(-90)
                        .into(img1);
            }
        });
        final StorageReference ref2 = storageReference.child("user/"+CNP.trim()+"/facial2.jpg");
        ref2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageView img1 = findViewById(R.id.profile2);
                Picasso.get().load(uri)
                        .rotate(-90)
                        .into(img1);
            }
        });
    }
}
