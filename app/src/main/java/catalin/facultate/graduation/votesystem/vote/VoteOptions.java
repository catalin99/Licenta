package catalin.facultate.graduation.votesystem.vote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import catalin.facultate.graduation.R;

public class VoteOptions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_options);
        TextView id, name;
        id = findViewById(R.id.textViewid);
        name = findViewById(R.id.textViewname);
        Intent currIntent = getIntent();
        id.setText(currIntent.getStringExtra("ID"));
        name.setText(currIntent.getStringExtra("Name"));
    }
}
