package androidapp.smartshopper.smartshopper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by Ben on 2016-10-22.
 */
public class SmartShopClient {
    private int port = 6969;
    //private String addr = "ec2-35-160-222-208.us-west-2.compute.amazonaws.com";
    private String addr = "192.168.0.19";
    private Socket connection;
    private BufferedWriter outputStream;
    private BufferedReader inputStream;

    public SmartShopClient() {
        try {
            connection = new Socket(addr, port);
            outputStream = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream()));
            inputStream = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
    }

    public String sendRequest(String request){
        try{
            outputStream.write(request);
            outputStream.flush();
            String response = inputStream.readLine();
            System.out.println(response);
            return response;
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }


}
