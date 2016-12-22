/*
 * FactorioUpdater - The best factorio mod manager
 * Copyright 2016 The FactorioUpdater Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.vilsol.factorioupdater.managers;

import javafx.scene.control.Alert;
import me.vilsol.factorioupdater.Resource;
import me.vilsol.factorioupdater.ui.UI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ProtocolManager implements Runnable {

    @Override
    public void run() {
        try{
            ServerSocket serverSocket = new ServerSocket(Resource.SOCKET_PORT);
            System.out.println("Listening for sockets on " + Resource.SOCKET_PORT);

            while(true){
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    try{
                        DataInputStream dIn = new DataInputStream(socket.getInputStream());
                        String message = dIn.readUTF();

                        System.out.println("Received Message: " + message);

                        if(!message.startsWith("fu://")){
                            return;
                        }

                        String padded = message.substring(5);
                        String command = padded.substring(0, padded.indexOf("/"));
                        String rest = padded.substring(padded.indexOf("/") + 1);

                        switch(command){
                            case "pack":
                                System.out.println("Would import pack: " + rest);
                                UI.showAlert(Alert.AlertType.INFORMATION, null, "Would import pack:\n" + rest);
                                break;
                            case "join":
                                System.out.println("Would join server: " + rest);
                                UI.showAlert(Alert.AlertType.INFORMATION, null, "Would join server:\n" + rest);
                                break;
                        }

                        dIn.close();
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void send(String s){
        System.out.println("Sending " + s);
        try{
            DataOutputStream dOut = new DataOutputStream(new Socket("127.0.0.1", Resource.SOCKET_PORT).getOutputStream());
            dOut.writeUTF(s);
            dOut.flush();
            dOut.close();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

}
