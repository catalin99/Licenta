package catalin.facultate.graduation.votesystem.vote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import catalin.facultate.graduation.R;
import catalin.facultate.graduation.auth.login.Login_Main;
import catalin.facultate.graduation.votesystem.admin.NewVote;
import io.reactivex.annotations.NonNull;

import static catalin.facultate.graduation.votesystem.vote.VoteSessions.SessionActive;
import static catalin.facultate.graduation.votesystem.vote.VoteSessions.SessionActiveDate;
import static catalin.facultate.graduation.votesystem.vote.VoteSessions.SessionID;
import static catalin.facultate.graduation.votesystem.vote.VoteSessions.SessionName;

public class VoteOptions extends AppCompatActivity {
    String ID = null;
    String VoteName = null;
    String Date = null;
    Boolean Active = false;

    String OptionVotedID = null;
    String OptionVotedName = null;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore fstore;
    private StorageReference storageReference;
    int counter;
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
        setContentView(R.layout.activity_vote_options);
        fstore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        Intent currIntent = getIntent();
        ID = currIntent.getStringExtra(SessionID);
        VoteName = currIntent.getStringExtra(SessionName);
        Date = currIntent.getStringExtra(SessionActiveDate);
        Active = currIntent.getBooleanExtra(SessionActive, false);
        counter = 0;

        TextView title = findViewById(R.id.TitleTxt);
        if(VoteName!=null && !VoteName.isEmpty())
        {
            title.setText(VoteName);
            LoadOptions();
        }
    }

    public void SubmitVote(View view)
    {
        Log.d("SUBMIT", OptionVotedID + " => " + OptionVotedName);
        if(OptionVotedID == null || OptionVotedName == null)
        {
            Toast.makeText(this, "Trebuie să selectezi o opțiune pentru a putea trimite votul", Toast.LENGTH_LONG).show();
            return;
        }
        String UserID = firebaseAuth.getCurrentUser().getUid();
        fstore.collection("Votes")
                .whereEqualTo("UserID", UserID)
                .whereEqualTo("VoteID", ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().isEmpty()) {
                                ApplyNewVote();
                            }
                            else {
                                Toast.makeText(VoteOptions.this, "Ai votat deja!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void ApplyNewVote()
    {
        final DocumentReference documentRefOption = fstore.collection("Votes").document();
        Map<String, Object> optionMap = new HashMap<>();
        optionMap.put("UserID", firebaseAuth.getCurrentUser().getUid());
        optionMap.put("VoteID",ID);
        Calendar rightNow = Calendar.getInstance();
        int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY);
        int currentMin = rightNow.get(Calendar.MINUTE);
        int currentSec = rightNow.get(Calendar.SECOND);
        optionMap.put("Time", ""+currentHourIn24Format+":"+currentMin+":"+currentSec);
        documentRefOption.set(optionMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("AUTH", "OnSucces: Vote submited in Votes");

                //update total number of votes
                DocumentReference documentRef= fstore.collection("Activities").document(ID);
                Map<String, Object> activityMap = new HashMap<>();
                int currentNoVotes = 1;
                if(activityMap.get("TotalVotes")!=null) {
                    currentNoVotes = Integer.parseInt(activityMap.get("TotalVotes").toString());
                    currentNoVotes = currentNoVotes + 1;
                }
                activityMap.put("TotalVotes", currentNoVotes);
                documentRef.update(activityMap);

                DocumentReference documentRefOpt= fstore.collection("Options").document(OptionVotedID);
                Map<String, Object> activityMapOpt = new HashMap<>();
                int currentNoVotesOpt = 1;
                if(activityMapOpt.get("Votes")!=null) {
                    currentNoVotesOpt = Integer.parseInt(activityMapOpt.get("Votes").toString());
                    currentNoVotesOpt = currentNoVotesOpt + 1;
                }
                activityMap.put("Votes", currentNoVotesOpt);
                documentRefOpt.update(activityMapOpt);
                Toast.makeText(VoteOptions.this, "Vot submis cu succes", Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                Log.d("AUTH", "OnFailure: " + e.toString());
                Toast.makeText(VoteOptions.this, "Eroare la încărcarea votului. Reîncearcă", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void LoadOptions()
    {
        fstore.collection("Options")
            .whereEqualTo("ActivityID", ID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("GET", document.getId() + " => " + document.getData());
                            TextView error = findViewById(R.id.Error);
                            Button submit = findViewById(R.id.SubmitBtn);

                            java.util.Date calendar = Calendar.getInstance().getTime();
                            SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yy");
                            String formattedDate = df.format(calendar);
                            if(Active == false)
                            {
                                error.setText("Sesiune de vot finalizată sau anulată");
                                error.setVisibility(View.VISIBLE);
                                submit.setEnabled(false);
                            }
                            else if(!formattedDate.equals(Date))
                            {
                                error.setText("Sesiune poate fi votate doar in data de "+Date);
                                error.setVisibility(View.VISIBLE);
                                submit.setEnabled(false);
                            }
                            FillData(document.getData(), document.getId());
                        }
                    } else {
                        Log.d("GET", "Error getting documents: ", task.getException());
                    }
                }
            });
    }

    private void FillData(final Map<String, Object> mydata, final String IDdoc)
    {


        LinearLayout currLayout = findViewById(R.id.SecLinearViewOptions);
        int leftright = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 30, getResources()
                        .getDisplayMetrics());
        int topbottom = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, getResources()
                        .getDisplayMetrics());

        int textSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 10, getResources()
                        .getDisplayMetrics());

        LinearLayout newVerticalLayout = new LinearLayout(getApplicationContext());  //layout prrinc, vertical
        RelativeLayout.LayoutParams vlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        vlp.setMargins(leftright, 0, leftright, topbottom);
        newVerticalLayout.setLayoutParams(vlp);
        newVerticalLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.backgroundColorVoteOption));
        newVerticalLayout.setOrientation(LinearLayout.VERTICAL);


        LinearLayout newHorizontalLayout = new LinearLayout(getApplicationContext()); //layout secundar, horizontal
        RelativeLayout.LayoutParams hlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        hlp.setMargins(0, 0, 0, 0);
        newHorizontalLayout.setLayoutParams(hlp);
        newHorizontalLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.backgroundColorVoteOption));
        newHorizontalLayout.setOrientation(LinearLayout.HORIZONTAL);


        RelativeLayout.LayoutParams txtANDbtn = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        txtANDbtn.setMargins(leftright, topbottom, 0, topbottom/3);
        TextView optionName = new TextView(getApplicationContext());
        optionName.setLayoutParams(txtANDbtn);
        optionName.setTextSize(textSize);
        optionName.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.textColor));
        counter++;
        optionName.setText( Integer.toString(counter) + ". " + mydata.get("OptionTitle").toString().toUpperCase());
//        optionName.setGravity();

        RelativeLayout.LayoutParams txtANDbtn2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        txtANDbtn2.setMargins(leftright, topbottom/3, 0, topbottom);
        Button button = new Button(getApplicationContext());
        button.setLayoutParams(txtANDbtn2);
        button.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.textColor));
        button.setText("Votează");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OptionVotedID = IDdoc;
                OptionVotedName = mydata.get("OptionTitle").toString();
                TextView votedOption = findViewById(R.id.VotedOptionName);
                votedOption.setText("Opțiune votată: " + OptionVotedName);
            }
        });

        Space space = new Space(getApplicationContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));

        final ImageView Picture = new ImageView(getApplicationContext());
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

        LinearLayout leftVerticalLayout = new LinearLayout(getApplicationContext());  //layout prrinc, vertical
        RelativeLayout.LayoutParams lvl = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lvl.setMargins(0, 0, 0, 0);
        leftVerticalLayout.setLayoutParams(lvl);
        leftVerticalLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.backgroundColorVoteOption));
        leftVerticalLayout.setOrientation(LinearLayout.VERTICAL);

        Picture.setLayoutParams(lpicture);
        final StorageReference ref1 = storageReference.child(mydata.get("ImgLocation").toString());
        ref1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri)
                        .into(Picture);
            }
        });
        Picture.setForegroundGravity(Gravity.CENTER);

        leftVerticalLayout.addView(optionName);
        leftVerticalLayout.addView(button);

        newHorizontalLayout.addView(leftVerticalLayout);
        newHorizontalLayout.addView(space);
        newHorizontalLayout.addView(Picture);

        newVerticalLayout.addView(newHorizontalLayout);

        currLayout.addView(newVerticalLayout);
    }
}
