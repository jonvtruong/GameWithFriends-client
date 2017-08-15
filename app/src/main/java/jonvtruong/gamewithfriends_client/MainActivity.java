package jonvtruong.gamewithfriends_client;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    static String HOST = "192.168.1.108";
    static int PORT = 8888;
    static String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Called when the user taps the Submit button
     */
    public void join(View view) {
        Log.d("console","joining...");
        EditText editText = (EditText) findViewById(R.id.editText);
        name = editText.getText().toString().replaceAll("\\s",""); // gets the name entered from editText and removes any spaces
        JoinGame j = new JoinGame(this);
        j.execute();
    }


    /** Asynchronous task to connect to game server, set up player, and start game **/
    private class JoinGame extends AsyncTask<Void, Void, Void> {
        MainActivity act;

        JoinGame(MainActivity a){
            act = a;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                Log.d("console","trying to connect " + HOST + ":" + PORT);
                Socket socket = new Socket(HOST, PORT);
                GameVariables vars = ((GameVariables) getApplicationContext());

                vars.setSocket(socket);

                Log.d("console", "connected");

                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                String message = Protocol.receive(inputStream); // reading player number and account
                Log.d("console", message);
                Protocol.gameProtocol(message, vars); // process the message update client variables with number and account

                Protocol.send(outputStream, "n " + name); // sending name
                vars.setName(name);

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d("console","UnknownHostException: " + e.toString());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d("console", "Exception: " + e.toString());
            }

            return null; // sends this variable into the argument of onPostExecute
        }

        @Override
        protected void onPostExecute(Void result){ //creates a new activity to run the game
            Intent intent = new Intent(act, GameActivity.class);
            startActivity(intent);
            Log.d("console","create new activity");
        }
    }
}
