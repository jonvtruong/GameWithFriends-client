package jonvtruong.gamewithfriends_client;

import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends AsyncTask<Void, Void, Void> {

    String address;
    int port;
    String response = "";

    Client(String addr, int p){
        address = addr;
        port = p;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        Socket socket = null;
        Log.d("console","running");
        try {
            Log.d("console","trying to connect " + address + ":" + port);
            socket = new Socket(address, port);
            Log.d("console","connected");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(64);
            byte[] buffer = new byte[64];

            int bytesRead;
            InputStream inputStream = socket.getInputStream();

    /*
     * notice:
     * inputStream.read() will block if no data return
     */
            while ((bytesRead = inputStream.read(buffer)) != -1){
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                Log.d("console","bytearray " + byteArrayOutputStream);
                response = byteArrayOutputStream.toString("UTF-8");
                Log.d("console","received " + response);
                Log.d("console",""+bytesRead);
            }

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "Exception: " + e.toString();
        }
        finally{
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        Log.d("console",response);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        //textResponse.setText(response);
        super.onPostExecute(result);
    }

}