package jonvtruong.gamewithfriends_client;

import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/** Asynchronous task to connect to game server, set up player, and start game **/

public class JoinGame extends AsyncTask<Void, Void, Void> {

    String address;
    int port;

    String name;
    Socket socket = null;

    JoinGame(String addr, int p, String n){
        address = addr;
        port = p;
        name = n;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            Log.d("console","trying to connect " + address + ":" + port);
            socket = new Socket(address, port);
            Protocol.setSocket(socket);

            Log.d("console", "connected");

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            String message = Protocol.receive(inputStream); // reading player number and account
            Log.d("console", message);
            Protocol.gameProtocol(message); // update client variables with number and account

            Protocol.send(outputStream, "n " + name); // sending name

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("console","UnknownHostException: " + e.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("console","Exception: " + e.toString());
        }
        finally{
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d("console","Exception: " + e.toString());
                }
            }
        }

        return null;
    }

/*    @Override
    protected void onPostExecute(Void socket) {
        //textResponse.setText(response);

    }*/

}