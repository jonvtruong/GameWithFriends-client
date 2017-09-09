package jonvtruong.gamewithfriends_client;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;

class Protocol{
    private static final int BUFFER_SIZE = 128;

    /** messages will be in the format: a 200 = total account value 200 **/
    static String[] parseCommand(String m){
        String[] parse = m.split(" ");
        Log.d("console", "received command: " + parse[0]);

        return parse;
    }

    static void executeCommand(String[] p, GameVariables vars){
        String[] parse = p;
        String command = parse[0];

        switch(command) {
            case("n"): //if creating new player, update player number and account starting balance, only occurs in mainactivity
                int playerNum = Integer.parseInt(parse[1]);
                int account = Integer.parseInt(parse[2]);

                vars.setPlayerNum(playerNum);
                vars.setAccount(account);
                Log.d("console", "setting up Player number: " + playerNum + " account balance: " + account);
                break;

            case("a"): //update personal account
                vars.setAccount(Integer.parseInt(parse[1]));
                Log.d("console", "account updated: " + vars.getAccount());
                break;

            case("p"): //update player list
                HashMap<String,Integer> list = new HashMap<>();
                for (int i=1; i<parse.length; i++){
                    list.put(parse[i], i - 1);
                }

                vars.setNameList(list);

                Log.d("console", "player list updated: " + vars.getNameList());
                break;
        }
    }

    /**reads from the input stream and stores it as a byte array.
     the byte array is then cut to remove null values then converted to string **/

    static String receive(InputStream conn){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(BUFFER_SIZE);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        try{
            bytesRead = conn.read(buffer); //reads input stream and stores into byte array, stores size of the input into bytes read
            byteArrayOutputStream.write(buffer, 0, bytesRead);
            return byteArrayOutputStream.toString("UTF-8");
        } catch (IOException e){
            e.printStackTrace();
            Log.d("console", "IOException: " + e.toString());
            return null;
        }
    }

    static void send(OutputStream conn, String message){
        try {
            byte[] bytes = message.getBytes("UTF-8");
            conn.write(bytes);
            Log.d("console", "sending: " + message);
        }

        catch (UnsupportedEncodingException e){
            e.printStackTrace();
            Log.d("console","Unsupported encoding Exception: " + e.toString());
        }

        catch(IOException e){
            e.printStackTrace();
            Log.d("console","IOException: " + e.toString());
        }
    }
}
