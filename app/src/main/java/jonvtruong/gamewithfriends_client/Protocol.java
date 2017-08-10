package jonvtruong.gamewithfriends_client;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

class Protocol {
    private static Socket socket = null;
    private static int playerNum  = 0;
    private static int account;
    private static final int BUFFER_SIZE = 64;

    /** messages will be in the format: a 200 = total account value 200 **/
    static void gameProtocol(String m){
        String[] parse = m.split(" ");
        String command = parse[0];
        Log.d("console", "command: " + command);

        if(command.equals("n")) { //if creating new player, update player number and account starting balance
            playerNum = Integer.parseInt(parse[1]);
            account = Integer.parseInt(parse[2]);

            Log.d("console", "Player number: " + playerNum + " account balance: " + account);
        }

        else if(command.equals("a")) {
            account = Integer.parseInt(parse[1]);
            Log.d("console","account updated: " + account);
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
        byte[] bytes = null;

        try {
            bytes = message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
            Log.d("console","Unsupported encoding Exception: " + e.toString());
        }

        try {
            conn.write(bytes);
        } catch(IOException e){
            e.printStackTrace();
            Log.d("console","IOException: " + e.toString());
        }

    }

    static void setSocket(Socket s){
        socket = s;
    }

    static Socket getSocket(){
        return socket;
    }

}
