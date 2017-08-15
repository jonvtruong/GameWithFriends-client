package jonvtruong.gamewithfriends_client;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        TextView name = (TextView) findViewById(R.id.nameText);
        GameVariables vars = ((GameVariables) getApplicationContext());
        name.setText(vars.getName());

        TextView account = (TextView) findViewById(R.id.amountText);
        account.setText(Integer.toString(vars.getAccount()));

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);


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
}
