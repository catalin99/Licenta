package catalin.facultate.graduation.auth.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import catalin.facultate.graduation.R;
import catalin.facultate.graduation.auth.register.Register_Main;

public class Login_Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login__main);
    }

    public void GoToRegister(View view)
    {
        Intent registerIntent = new Intent(this, Register_Main.class);
        startActivity(registerIntent);
    }

    public void GoToFaceRecognition(View view)
    {

    }
}
