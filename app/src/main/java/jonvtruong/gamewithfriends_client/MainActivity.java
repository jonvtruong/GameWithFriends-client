package jonvtruong.gamewithfriends_client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
        Client client = new Client(HOST,PORT);
        client.execute();
    }
}
