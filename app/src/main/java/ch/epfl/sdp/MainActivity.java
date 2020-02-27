package ch.epfl.sdp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void greetMessage(View view) {
        EditText editText = findViewById(R.id.mainName);
        String message = editText.getText().toString();
        TextView textView = findViewById(R.id.greetingMessage);
        textView.setText("Hello " + message + "!");
    }

}