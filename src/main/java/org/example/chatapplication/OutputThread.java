package org.example.chatapplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import javafx.scene.control.TextArea;

public class OutputThread extends Thread{
  Socket socket;
  TextArea txt;
  BufferedReader bf;
  String sender;
  String receiver;

  public OutputThread (Socket socket, TextArea txt, String sender, String receiver){
    super();
    this.socket=socket;
    this.txt=txt;
    this.sender=sender;
    this.receiver=receiver;
    try {
      bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }catch (Exception e){
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    while (true){
      try {
        if(socket != null){
          String msg = "";
          if((msg = bf.readLine()) != null && msg.length() > 0 ){
            txt.appendText("\n" + receiver + ":" + msg);
          }
        }
        sleep(1000);
      }catch (Exception e){
        e.printStackTrace();
      }
    }
  }
}
