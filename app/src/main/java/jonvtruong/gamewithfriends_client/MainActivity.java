package jonvtruong.gamewithfriends_client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    static String HOST = "192.168.1.108";
    static int PORT = 8888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Called when the user taps the Submit button
     */
    public void joinGame(View view) {
        Log.d("console","joining...");
        EditText editText = (EditText) findViewById(R.id.editText);
        String name = editText.getText().toString().replaceAll("\\s",""); // gets the name entered from editText and removes and spaces
        JoinGame joinGame = new JoinGame(HOST,PORT,name);
        joinGame.execute();
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
}
