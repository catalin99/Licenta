package catalin.facultate.graduation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import catalin.facultate.graduation.auth.login.Login_Main;
import catalin.facultate.graduation.auth.register.Register_Main;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent startIntent = new Intent(this, Login_Main.class);
        startActivity(startIntent);
    }
}
