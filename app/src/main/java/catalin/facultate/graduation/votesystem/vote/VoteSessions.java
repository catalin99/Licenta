package catalin.facultate.graduation.votesystem.vote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import catalin.facultate.graduation.R;
import catalin.facultate.graduation.auth.login.Login_Main;
import io.reactivex.annotations.NonNull;

public class VoteSessions extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore fstore;
    public final static String SessionID = "catalin.facultate.graduation.votesystem.vote.sessuionid";
    public final static String SessionName = "catalin.facultate.graduation.votesystem.vote.sessuionname";
    public final static String SessionActiveDate = "catalin.facultate.graduation.votesystem.vote.sessuionactivedate";
    public final static String SessionActive = "catalin.facultate.graduation.votesystem.vote.sessuionactive";
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
        setContentView(R.layout.activity_vote_sessions);
        fstore = FirebaseFirestore.getInstance();

        final Spinner dropdown = findViewById(R.id.FilterSpinner);

        String[] items = new String[]{"TOATE", "ACTIVE", "FINALIZATE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView spinner = (TextView)dropdown.getSelectedView();
                LoadSessionsList(spinner.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void LoadSessionsList(final String filter)
    {

        fstore.collection("Activities")
                .whereEqualTo("Finalized", true)
                .orderBy("ActiveDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            LinearLayout currLayout = findViewById(R.id.SecLinearView);
                            currLayout.removeAllViews();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("GET", document.getId() + " => " + document.getData());
                                if(filter.equals("ACTIVE") && (boolean) document.getData().get("Active"))
                                    AddItem(document, filter);
                                else if(filter.equals("FINALIZATE") && !((boolean) document.getData().get("Active")))
                                    AddItem(document, filter);
                                else if (filter.equals("TOATE"))
                                    AddItem(document, filter);
                            }
                        } else {
                            Log.d("GET", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void AddItem(QueryDocumentSnapshot document, String filter)
    {
        final String docID = document.getId();
        final Map<String, Object> currentData = document.getData();
        int colorID = 0;
        if((boolean)currentData.get("Active"))
            colorID = R.color.votecoloractive;
        else
        {
            if(currentData.get("Winner").equals("NONE"))
                colorID = R.color.votecolorold;
            else
                colorID = R.color.votecolorsucces;
        }

        LinearLayout currLayout = findViewById(R.id.SecLinearView);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, // Width
                RelativeLayout.LayoutParams.WRAP_CONTENT); // Height

        int leftright = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 30, getResources()
                        .getDisplayMetrics());
        int topbottom = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, getResources()
                        .getDisplayMetrics());

        int textSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 10, getResources()
                        .getDisplayMetrics());
        lp.setMargins(0, topbottom, 0, 0);

        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // Width
                RelativeLayout.LayoutParams.WRAP_CONTENT); // Height
        lp2.setMargins(leftright, topbottom, leftright, topbottom);

        TextView voteName = new TextView(getApplicationContext());
        voteName.setLayoutParams(lp);
        voteName.setTextSize(textSize);
        voteName.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.textColor));
        voteName.setText(currentData.get("VoteTitle").toString().toUpperCase());
        voteName.setTypeface(null, Typeface.BOLD);
        voteName.setGravity(Gravity.CENTER_HORIZONTAL);


        TextView voteDate = new TextView(getApplicationContext());
        voteDate.setLayoutParams(lp2);
        voteDate.setTextSize(textSize);
        voteDate.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.textColor));
        voteDate.setText(currentData.get("ActiveDate").toString());
        //voteDate.setGravity(Gravity.LEFT);

        Space space = new Space(getApplicationContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));


        TextView winnerOrVotes = new TextView(getApplicationContext());
        winnerOrVotes.setLayoutParams(lp2);
        winnerOrVotes.setTextSize(textSize);
        winnerOrVotes.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.textColor));
        if((boolean) currentData.get("Active"))
        {
            winnerOrVotes.setText(currentData.get("TotalVotes").toString() + " voturi");
            //winnerOrVotes.setText("0" + " voturi");
        }
        else if(currentData.get("Winner").equals("NONE"))
        {
            winnerOrVotes.setText("Anulat");
        }
        else
        {
            winnerOrVotes.setText(currentData.get("Winner").toString());
        }
        //winnerOrVotes.setGravity(Gravity.RIGHT);


        LinearLayout newVerticalLayout = new LinearLayout(getApplicationContext());
        RelativeLayout.LayoutParams hlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        hlp.setMargins(leftright, 0, leftright, topbottom);
        newVerticalLayout.setLayoutParams(hlp);
        newVerticalLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),colorID));
        newVerticalLayout.setOrientation(LinearLayout.VERTICAL);
        newVerticalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent OptionsIntent = new Intent(VoteSessions.this, VoteOptions.class);
                OptionsIntent.putExtra(SessionID, docID);
                OptionsIntent.putExtra(SessionName, currentData.get("VoteTitle").toString());
                OptionsIntent.putExtra(SessionActiveDate, currentData.get("ActiveDate").toString());
                OptionsIntent.putExtra(SessionActive, (Boolean) currentData.get("Active"));
                startActivity(OptionsIntent);
            }
        });

        newVerticalLayout.addView(voteName);

        LinearLayout newHorizontalLayout = new LinearLayout(getApplicationContext());
        RelativeLayout.LayoutParams vlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        vlp.setMargins(0, 0, 0, 0);
        newHorizontalLayout.setLayoutParams(vlp);
        newHorizontalLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),colorID));
        newHorizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        newHorizontalLayout.addView(voteDate);
        newHorizontalLayout.addView(space);
        newHorizontalLayout.addView(winnerOrVotes);

        newVerticalLayout.addView(newHorizontalLayout);
        currLayout.addView(newVerticalLayout);
    }
}
