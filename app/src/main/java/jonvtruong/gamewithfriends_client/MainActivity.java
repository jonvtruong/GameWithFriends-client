package jonvtruong.gamewithfriends_client;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    static String HOST = "123.123.1.1";
    static int PORT = 8880;
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
    private class JoinGame extends AsyncTask<Void, Void, Boolean> { // doInBackground's arguments, inprogress argument, return type of doInBackground + argument of onPost
        MainActivity act;

        JoinGame(MainActivity a){
            act = a;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) { // return type
            try {
                Log.d("console","trying to connect " + HOST + ":" + PORT);
                Socket socket = new Socket(HOST, PORT);
                GameVariables vars = ((GameVariables) getApplicationContext());

                vars.setSocket(socket);

                Log.d("console", "connected");

                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                String message = Protocol.receive(inputStream); // reading player number and account
                Log.d("console", "message received " + message);
                String[] parse = Protocol.parseCommand(message);
                Protocol.executeCommand(parse, vars); // process the message update client variables with number and account

                Protocol.send(outputStream, "n " + name); // sending name
                vars.setName(name);

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d("console","UnknownHostException: " + e.toString());
                return false;
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d("console", "Exception: " + e.toString());
                return false;
            }

            return true; // sends this variable into the argument of onPostExecute
        }

        @Override
        protected void onPostExecute(Boolean result){ //creates a new activity to run the game
            if(result) {
                Intent intent = new Intent(act, GameActivity.class);
                startActivity(intent);
                Log.d("console", "create new activity");
                Log.d("console", "--------------------");
            }
            else{
                Toast toast = Toast.makeText(MainActivity.this, "Server not available", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
