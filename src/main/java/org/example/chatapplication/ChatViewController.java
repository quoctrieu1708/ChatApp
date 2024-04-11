package org.example.chatapplication;

import java.time.format.DateTimeFormatterBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatViewController {
  @FXML
  public TextArea txtMessage;
  @FXML
  public TextArea txtMessages;
  public Button btnSend;

  private Socket socket;
  private BufferedReader reader;
  private DataOutputStream writer;
  private String staffName;

  public void initializeSocket(Socket socket) {
    this.socket = socket;

    try {
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      writer = new DataOutputStream(socket.getOutputStream());


    } catch (IOException e) {
      e.printStackTrace();
    }

    new Thread(() -> {
      try {
        while (true) {
          String msg = reader.readLine();
          if (msg != null && !msg.isEmpty()) {
            txtMessage.appendText(msg + "\n");
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
  }

  public void setStaffName(String staffName) {
    this.staffName = staffName;
    Tab staffNameLabel = null;
    staffNameLabel.setText("Chatting with: " + staffName);
  }

  public void appendMessage(String sender, String message) {
    txtMessages.appendText(sender + ": " + message + "\n");
  }


  @FXML
  private void SendMessage() {
    String message = txtMessage.getText().trim();
    if (!message.isEmpty()) {
      try {
        writer.writeUTF(message + "\r\n");
        writer.flush();
        txtMessages.appendText("You: " + message + "\n");
        txtMessage.clear();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}
