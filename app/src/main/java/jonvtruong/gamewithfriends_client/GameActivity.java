package jonvtruong.gamewithfriends_client;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class GameActivity extends AppCompatActivity {
    private Handler handler;
    private Spinner spinner;
    private TextView account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

       // Intent intent = getIntent();
        TextView name = (TextView) findViewById(R.id.nameText);
        GameVariables vars = ((GameVariables) getApplicationContext());
        name.setText(vars.getName());

        account = (TextView) findViewById(R.id.amountText);
        account.setText(Integer.toString(vars.getAccount()));

        // create spinner to select player names
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vars.getNameList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        handler = new Handler();

        // create background thread to listen and process server messages
        Thread listen = new Thread(new listenThread(vars, this));
        listen.start();
    }

    /**
     * Called when the user taps the Pay button
     */
    public void pay(View view) {
        Log.d("console","paying");
        EditText editText = (EditText) findViewById(R.id.paymentText);
        try {
            int payment = Integer.parseInt(editText.getText().toString()); // gets the name entered from editText and removes any spaces
            SendPayment s = new SendPayment();
            s.execute(payment);
        } catch(NumberFormatException e){
            Log.d("console", "number too big");
        }
    }

    /** Asynchronous task to connect to game server, set up player, and start game **/
    private class SendPayment extends AsyncTask<Integer, Void, Void> {
        SendPayment(){
        }

        @Override
        protected Void doInBackground(Integer... amount) {
            try {
                Log.d("console","sending payment");
                GameVariables vars = ((GameVariables) getApplicationContext());

                Socket socket = vars.getSocket();
                OutputStream outputStream = socket.getOutputStream();

                Protocol.send(outputStream, "t " + vars.getPlayerNum() + " " + 0 + " " + amount[0]); // sending transfer message: "t from to amount"

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
        }
    }

    private class listenThread implements Runnable{
        private GameVariables vars;
        private Socket socket;
        private GameActivity act;

        listenThread(GameVariables v, GameActivity g){
            vars = v;
            socket = vars.getSocket();
            act = g;
        }

        @Override
        public void run() {
            // sets thread to run in the background to not compete with UI thread
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            while(true){
                try {
                    InputStream inputStream = socket.getInputStream();
                    Log.d("console","listening...");
                    String message = Protocol.receive(inputStream); // listening for message from server

                    if(message == null){ //if disconnected
                        Log.d("console", "disconnected");
                        break;
                    }

                    Log.d("console", message);
                    final String command = Protocol.gameProtocol(message, vars); // process the message
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateUI(command);
                        }
                    });

                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d("console","UnknownHostException: " + e.toString());
                    break;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d("console", "Exception: " + e.toString());
                    break;
                }
            }
        }

        private void updateUI(String command){
            switch(command){
                case "p":
                    updateSpinner();
                case "t":
                    updateAccountText();
            }
        }

        private void updateSpinner(){
            ArrayAdapter<String> adapter = new ArrayAdapter<>(act, android.R.layout.simple_spinner_item, vars.getNameList());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        private void updateAccountText(){
            account.setText(Integer.toString(vars.getAccount()));
        }
    }
}
