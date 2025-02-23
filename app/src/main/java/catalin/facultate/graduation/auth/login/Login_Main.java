package catalin.facultate.graduation.auth.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import catalin.facultate.graduation.MainActivity;
import catalin.facultate.graduation.R;
import catalin.facultate.graduation.auth.register.Register_Main;

public class Login_Main extends AppCompatActivity {

    FirebaseAuth fAuth;
    static String AUTHEMAIL = "catalin.facultate.graduation.auth.login.email";
    static String AUTHPASS = "catalin.facultate.graduation.auth.login.password";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        if(fAuth.getCurrentUser()==null)
        {
            setContentView(R.layout.activity_login__main);
        }
        else
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void GoToRegister(View view)
    {
        Intent registerIntent = new Intent(this, Register_Main.class);
        startActivity(registerIntent);
    }

    public void GoToFaceRecognition(View view)
    {
        final TextView emailView, passwordView;
        emailView = findViewById(R.id.emailLogin);
        passwordView = findViewById(R.id.passwordLogin);
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        fAuth.signInWithEmailAndPassword(emailView.getText().toString(), passwordView.getText().toString()).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(Login_Main.this, "Autentificare completă", Toast.LENGTH_LONG).show();
                        Intent faceRecognitionIntent = new Intent(Login_Main.this, Login_FaceRecognition.class);
                        faceRecognitionIntent.putExtra(AUTHEMAIL, emailView.getText().toString());
                        faceRecognitionIntent.putExtra(AUTHPASS, passwordView.getText().toString());
                        startActivity(faceRecognitionIntent);
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Login_Main.this, "Autentificare eșuată: "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }
}
