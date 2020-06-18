package catalin.facultate.graduation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import catalin.facultate.graduation.auth.login.Login_Main;
import catalin.facultate.graduation.auth.register.Register_Main;
import catalin.facultate.graduation.votesystem.admin.NewVote;
import catalin.facultate.graduation.votesystem.vote.VoteSessions;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;
    String userID;
    String CNP;

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
