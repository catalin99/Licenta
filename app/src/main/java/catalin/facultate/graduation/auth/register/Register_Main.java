package catalin.facultate.graduation.auth.register;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import catalin.facultate.graduation.R;
import catalin.facultate.graduation.model.User;

public class Register_Main extends AppCompatActivity {

    static String REGISTER_USER = "catalin.facultate.graduation.auth.register.REGISTER_USER";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_register__main);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register__main);
    }

    public void GoToSelfieActivity(View view)
    {
        TextView email = findViewById(R.id.emailRegister);
        TextView cnp = findViewById(R.id.cnpRegister);
        TextView password = findViewById(R.id.passwordRegister);
        User user = new User(email.getText().toString(), cnp.getText().toString(), password.getText().toString());
        Intent startIntent = new Intent(this, Register_Selfie.class);
        startIntent.putExtra(REGISTER_USER, user);
        startActivity(startIntent);
    }
}
