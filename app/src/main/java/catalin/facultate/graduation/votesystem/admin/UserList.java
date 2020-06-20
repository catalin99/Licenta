package catalin.facultate.graduation.votesystem.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

import catalin.facultate.graduation.MainActivity;
import catalin.facultate.graduation.R;
import catalin.facultate.graduation.auth.login.Login_Main;
import catalin.facultate.graduation.votesystem.vote.VoteOptions;
import catalin.facultate.graduation.votesystem.vote.VoteSessions;
import io.reactivex.annotations.NonNull;

public class UserList extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore fstore;
    public final static String USERID = "catalin.facultate.graduation.votesystem.vote.userID";
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

        setContentView(R.layout.activity_user_list);
        fstore = FirebaseFirestore.getInstance();

        fstore.collection("users")
            .orderBy("APPROVED")
            .orderBy("LastName")
            .orderBy("FirstName")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        LinearLayout currLayout = findViewById(R.id.SecLinearView);
                        currLayout.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("GET", document.getId() + " => " + document.getData());
                            AddUser(document);
                        }
                    } else {
                        Log.d("GET", "Error getting documents: ", task.getException());
                    }
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

    private void AddUser(QueryDocumentSnapshot document)
    {
        final String IDuser = document.getId();
        Map<String, Object> userData = document.getData();

        int colorID = 0;
        if((boolean)userData.get("APPROVED"))
            colorID = R.color.votecoloractive;
        else
        {
            colorID = R.color.votecolorold;
        }

        LinearLayout currLayout = findViewById(R.id.SecLinearView);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, // Width
                RelativeLayout.LayoutParams.WRAP_CONTENT); // Height

        int leftright = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 30, getResources()
                        .getDisplayMetrics());
        int topbottom = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 30, getResources()
                        .getDisplayMetrics());

        int textSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 10, getResources()
                        .getDisplayMetrics());
        lp.setMargins(0, topbottom/2, 0, topbottom/2);

        TextView userName = new TextView(getApplicationContext());
        userName.setLayoutParams(lp);
        userName.setTextSize(textSize);
        userName.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.textColor));
        userName.setText(userData.get("LastName").toString().toUpperCase() + " " + userData.get("FirstName").toString().toUpperCase());
        userName.setTypeface(null, Typeface.BOLD);
        userName.setGravity(Gravity.CENTER_HORIZONTAL);




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
                Intent OptionsIntent = new Intent(UserList.this, UserEdit.class);
                OptionsIntent.putExtra(USERID, IDuser);
                startActivity(OptionsIntent);
                Toast.makeText(UserList.this, "Succes!", Toast.LENGTH_LONG).show();

            }
        });

        newVerticalLayout.addView(userName);

        currLayout.addView(newVerticalLayout);
    }
}
