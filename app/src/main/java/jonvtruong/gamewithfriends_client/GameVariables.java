package jonvtruong.gamewithfriends_client;

import android.app.Application;

import java.net.Socket;

/**
 * Created by jonvt on 8/14/2017.
 */

public class GameVariables extends Application {
    private Socket socket;
    private String name;
    private int account;
    private int playerNum;
    private String[] nameList = {"asdsad", "asjkhjhkjsad"};

    public void setName(String n){
        name = n;
    }

    public String getName(){
        return name;
    }

    public void setSocket(Socket s){
        socket = s;
    }

    public Socket getSocket(){
        return socket;
    }

    public void setAccount(int a){
        account = a;
    }

    public int getAccount(){
        return account;
    }

    public void setPlayerNum(int num){
        playerNum = num;
    }

    public int getPlayerNum(){
        return playerNum;
    }

    public void setNameList(String[] list){
        nameList = list;
    }

    public String[] getNameList(){
        return nameList;
    }
}
