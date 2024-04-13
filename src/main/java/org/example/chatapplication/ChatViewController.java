package org.example.chatapplication;

import javafx.application.Platform;
import javafx.fxml.FXML;
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

  private Socket socket;
  private BufferedReader reader;
  private DataOutputStream writer;
  private String staffName;

  public void initializeSocket(Socket socket, String staffName) {
    this.socket = socket;
    this.staffName = staffName;

    try {
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      writer = new DataOutputStream(socket.getOutputStream());
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }

    new Thread(() -> {
      try {
        while (true) {
          String msg = reader.readLine();
          if (msg != null && !msg.isEmpty()) {
            Platform.runLater(() -> appendMessage("Server", msg));
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          if (reader != null) reader.close();
          if (writer != null) writer.close();
          if (socket != null) socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  public void appendMessage(String sender, String message) {
    if (sender.equals("Server")) {
      if (staffName != null) {
        txtMessage.appendText("Server: " + message + "\n");
      } else {
        txtMessage.appendText("Server: " + message + "\n");
      }
    } else {
      if (staffName != null) {
        txtMessage.appendText(staffName + ": " + message + "\n");
      } else {
        txtMessage.appendText(sender + ": " + message + "\n");
      }
    }
  }


  @FXML
  private void SendMessage() {
    String message = txtMessages.getText().trim();
    if (!message.isEmpty()) {
      try {
        writer.writeUTF(staffName + ": " + message + "\r\n");
        writer.flush();
        txtMessage.appendText("You: " + message + "\n");
        txtMessages.clear();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}
