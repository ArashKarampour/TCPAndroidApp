package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private TextView tvRecData;
    private EditText etServerIP, etServerPort,etClientPacket;
    private Button btnClientConnect;
    private String serverIP;
    private int serverPort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRecData = findViewById(R.id.tvRectxt);
        etServerIP = findViewById(R.id.serverIP);
        etServerPort = findViewById(R.id.serverPort);
        etClientPacket = findViewById(R.id.clientPacket);
        btnClientConnect = findViewById(R.id.btnConnect);
    }
    //onclick method

/*    public void onClickConnect(View view){
        serverIP = etServerIP.getText().toString();
        serverPort = Integer.valueOf(etServerPort.getText().toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(serverIP, serverPort);
                    // Reading from server
                    BufferedReader brInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String txtFromServer = brInput.readLine();

                    //PrintWriter outClient = new PrintWriter(socket.getOutputStream());
                    //outClient.write("something from client");
                    //outClient.flush();



                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvRecData.setText(txtFromServer);
                        }
                    });

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    */
    private ClientThread clientThread;
    private String msg;
    public void onClickConnect(View view){
        serverIP = etServerIP.getText().toString();
        serverPort = Integer.valueOf(etServerPort.getText().toString());
        clientThread = new ClientThread();
        clientThread.connect();
    }

    public void onClickSendMsg(View view){
        msg = etClientPacket.getText().toString();
        clientThread.sendMessage(msg);
    }

    public class ClientThread extends Thread implements Runnable{

        private Socket socket;
        private BufferedReader brInputClient;
        private PrintWriter outClient;
        private String txtFromServer;

        public void connect(){
            start();
        }

        @Override
        public void run() {
            try {
                socket = new Socket(serverIP, serverPort);
                // Reading from server
                brInputClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                txtFromServer = brInputClient.readLine();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvRecData.setText(txtFromServer);
                    }
                });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void sendMessage(String msg){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        outClient = new PrintWriter(socket.getOutputStream(),true);
                        outClient.println(msg);

                        brInputClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        txtFromServer = brInputClient.readLine();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvRecData.setText(txtFromServer);
                            }
                        });
                        //with write and flush it didn't work properly! but with println works fine!

                        /*outClient = new PrintWriter(socket.getOutputStream());
                        outClient.write(msg);
                        outClient.flush();*/
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }).start();
        }
    }
}