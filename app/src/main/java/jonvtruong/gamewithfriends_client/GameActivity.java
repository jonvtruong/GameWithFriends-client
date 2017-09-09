package jonvtruong.gamewithfriends_client;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private Handler handler;
    private Spinner spinner;
    private TextView accountText;
    private GameVariables vars;
    private int selectedPlayer;
    private String nameSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // create background thread to listen and process server messages
        handler = new Handler();
        vars = (GameVariables) getApplicationContext();
        Thread listen = new Thread(new listenThread(this));
        listen.start();

       // Intent intent = getIntent();
        spinner = (Spinner) findViewById(R.id.spinner);

        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() { // called whenever player name is selected
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // An item was selected. You can retrieve the selected item using parent.getItemAtPosition(pos)
                nameSelected = (String) parent.getItemAtPosition(pos);
                Log.d("console","name selected: " + nameSelected);
                HashMap dict = vars.getNameList();
                selectedPlayer = (int) dict.get(nameSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
           // Another interface callback
            }
        });

        TextView name = (TextView) findViewById(R.id.nameText);

        name.setText(vars.getName());

        accountText = (TextView) findViewById(R.id.amountText);
        accountText.setText(Integer.toString(vars.getAccount()));
        Log.d("console","setup accountText: " + vars.getAccount());

        //updateSpinner();
    }

    /**
     * Called when the user taps the Pay button
     */
    public void pay(View view) {
        EditText editText = (EditText) findViewById(R.id.paymentText);
        try {
            int payment = Integer.parseInt(editText.getText().toString()); // gets the name entered from editText and removes any spaces
            if(payment <= vars.getAccount()) {
                //confirmation dialog
                paymentDialog(payment);
            }

            else{
                Toast toast = Toast.makeText(this, "Not enough money", Toast.LENGTH_SHORT);
                toast.show();
            }

        } catch(NumberFormatException e){
            Log.d("console", "number too big");
        }
    }

    private void updateSpinner(){
        // create spinner to select player names
        List<String> names = new ArrayList<>(vars.getNameList().keySet());
        names.remove(vars.getName());
        Log.d("console", "removed name");
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        Log.d("console", "updated spinner");
    }

    private void paymentDialog(int p){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final int pay = p;
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Is this correct?")
                .setTitle("Paying " + nameSelected + " $" + p);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            // User clicks yes
            Log.d("console", "confirmed, paying");
            SendPayment s = new SendPayment();
            s.execute(selectedPlayer, pay);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // user clicks no

            }
        });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /** Asynchronous task to send payment **/
    private class SendPayment extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... arg) {
            try {
                Log.d("console","sending payment");

                Socket socket = vars.getSocket();
                OutputStream outputStream = socket.getOutputStream();

                Protocol.send(outputStream, "t " + vars.getPlayerNum() + " " + arg[0] + " " + arg[1]); // sending transfer message: "t from to amount"

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
        protected void onPostExecute(Void result){ //displays toast message after payment is sent to server, runs on UI thread
            Toast toast = Toast.makeText(GameActivity.this, "Payment sent", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class listenThread implements Runnable{
        private Socket socket;

        listenThread(GameActivity g){
            socket = vars.getSocket();
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
                    handler.post(new runCommand(message));

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
    }

    /** executes command in UI thread **/
    private class runCommand implements Runnable{
        String[] parse;
        String command;
        String message;

        runCommand(String m){
            message = m;
            parse = Protocol.parseCommand(m);
            command = parse[0];
        }

        @Override
        public void run() {
            //get confirmation dialog for transactions (commands that are not new player added)
            //boolean needDialog = !(command.equals("p") || command.equals("n"));
            if(command.equals("t")) {
                //show confirmation dialog
                receiveDialog();
            }

            else{
                //execute command, update variables
                Protocol.executeCommand(parse, vars);
                //update UI with new variable
                updateUI();
            }

        }

        private void receiveDialog(){

            AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);

            int fromNum = Integer.parseInt(parse[1]);

            HashMap<String,Integer> list = vars.getNameList();
            String fromName ="";

            for(String key: list.keySet()) {
                if(list.get(key).equals(fromNum)) {
                    fromName = key;
                }
            }


            builder.setMessage("Do you accept?")
                    .setTitle(fromName + " wants to pay you $" + parse[3]);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicks yes
                    message = message.replaceFirst("t","y");
                    Log.d("console", "confirmed transaction: " + message);
                    SendOK s = new SendOK();
                    s.execute(message);
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // user clicks no

                }
            });
            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void updateUI(){
            switch(command){
                case "p": // processes command: p (list of player names)
                    updateSpinner();
                    break;

                case "a": // receives command: a new_account_value
                    updateAccountText();
                    Log.d("console","displaying new account value");
                    break;
            }
        }

        private void updateAccountText(){
            accountText.setText(Integer.toString(vars.getAccount()));
        }

        /** Asynchronous task to send transaction confirmation **/
        private class SendOK extends AsyncTask<String, Void, Void> {
            @Override
            protected Void doInBackground(String... arg) {
                try {
                    Log.d("console","sending payment");

                    Socket socket = vars.getSocket();
                    OutputStream outputStream = socket.getOutputStream();

                    Protocol.send(outputStream, arg[0]); // sending confirmed transfer message: "y from to amount"

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
            protected void onPostExecute(Void result){ //displays toast message after payment is confirmed, runs on UI thread
                Toast toast = Toast.makeText(GameActivity.this, "Payment received", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
