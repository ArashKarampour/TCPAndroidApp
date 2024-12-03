package com.example.myapplicationserver;

import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class MainActivityServer extends AppCompatActivity {

    private TextView tvServerIP, tvServerPort,tvStatus,tvClientMsg;
    private String serverIP;
    private int serverPort = 3000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_server);

        tvServerIP = findViewById(R.id.tvServerIP);
        tvServerPort = findViewById(R.id.tvServerPort);
        tvStatus = findViewById(R.id.tvStatus);
        tvClientMsg = findViewById(R.id.tvClientMsg);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();

                // Convert the integer IP address to a human-readable format
                serverIP = String.format(
                        "%d.%d.%d.%d",
                        (ipAddress & 0xff),
                        (ipAddress >> 8 & 0xff),
                        (ipAddress >> 16 & 0xff),
                        (ipAddress >> 24 & 0xff)
                );
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                // If we want to get the mobile data IPv4 address, we may need to use a third-party service.
                // This is because Android doesn't provide a direct API for obtaining the mobile data IP address.
                //Alternatively we can see our server IP address from settings:
                serverIP = "IP -> Settings -> About Phone -> Status -> IP Address";
            }else{
                serverIP = "10.0.2.15"; //setting IP for android emulator
            }
        }else {
            serverIP = "You are disconnected from Internet.\nConnect and restart the App";
        }

        tvServerIP.setText(serverIP);
        tvServerPort.setText(String.valueOf(serverPort));

    }

    private ServerThread serverThread;
    public void onClickStartServer(View view){
        serverThread = new ServerThread();
        serverThread.startServer();
    }

    public void onClickStopServer(View view){
        serverThread.stopServer();
    }

    class ServerThread extends Thread implements Runnable{

        private String inputline;
        private boolean serverRunning;
        private ServerSocket serverSocket;
        private int count=0;
        public void startServer(){
            serverRunning = true;
            start();
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(serverPort);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("Waiting for Clients...");
                    }
                });

                while (serverRunning){
                    Socket socket = serverSocket.accept();
                    count++;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvStatus.setText("Connectet to:"+socket.getInetAddress()+":"+socket.getLocalPort());
                                }
                            });

                            try {
                                PrintWriter outputServer = new PrintWriter(socket.getOutputStream(),true);
                                outputServer.println("Welcome to Server"+count);

                                //for reading from client
                                BufferedReader inputServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                while((inputline = inputServer.readLine()) != null){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tvClientMsg.setText(inputline);
                                        }
                                    });

                                    if(".".equals(inputline)){
                                        outputServer.println("Bye");
                                        break;
                                    }

                                }
                                // end of reading from a client

                                socket.close();
                                interrupt();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }).start();

                    //1
                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Connectet to:"+socket.getInetAddress()+":"+socket.getLocalPort());
                        }
                    });*/

                    //2
                    /*PrintWriter outputServer = new PrintWriter(socket.getOutputStream(),true);
                    outputServer.println("Welcome to Server"+count);*/
                    //outputServer.flush();

                    /*BufferedReader inputServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    inputline = inputServer.readLine();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvClientMsg.setText(inputline.toString());
                        }
                    });*/

                    /*new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BufferedReader inputServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                inputline = inputServer.readLine();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvClientMsg.setText(inputline.toString());
                                    }
                                });
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).start();*/

                   //3
                   /*
                    //for reading from client
                   BufferedReader inputServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while((inputline = inputServer.readLine()) != null){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvClientMsg.setText(inputline);
                            }
                        });

                        if(".".equals(inputline)){
                            outputServer.println("Bye");
                            break;
                        }

                    }
                    // end of reading from a client

                    socket.close();*/

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void stopServer(){
            serverRunning=false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(serverSocket != null){
                        try {
                            serverSocket.close();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvStatus.setText("Server has been stopped");
                                }
                            });

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }).start();
        }

    }//serverThread

}