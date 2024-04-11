package org.example.chatapplication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ManagerController extends Application{
  private AnchorPane chatAnchorPane;
  @FXML
  private TextField txtServerPort;
  @FXML
  private TabPane tabPane;
  private ServerSocket serverSocket;

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("ManagerChatter.fxml"));
    loader.setController(this);
    Parent root = loader.load();
    chatAnchorPane = (AnchorPane) loader.getNamespace().get("chatAnchorPane");

    primaryStage.setTitle("Manager Chat");
    primaryStage.setScene(new Scene(root, 600, 600));
    primaryStage.show();

    new Thread(this::startServerSocket).start();
  }

  private void startServerSocket() {
    try {
      serverSocket = new ServerSocket(Integer.parseInt(txtServerPort.getText()));
      while (true) {
        Socket clientSocket = serverSocket.accept();
        handleClientConnection(clientSocket);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleClientConnection(Socket clientSocket) {
    Platform.runLater(() -> {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatView.fxml"));
        Parent root = loader.load();
        ChatViewController controller = loader.getController();
        controller.initializeSocket(clientSocket);

        // Gửi tin nhắn "manager is running" tới client
        DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());
        writer.writeUTF("manager is running");
        writer.flush();

        // Hiển thị tên staff trên tab
        DataInputStream reader = new DataInputStream(clientSocket.getInputStream());
        String staffName = reader.readUTF();
        Tab tab = new Tab(staffName);
        tab.setContent(root);
        tabPane.getTabs().add(tab);

        new Thread(() -> {
          try {
            while (true) {
              String msg = reader.readUTF();
              if (msg != null && !msg.isEmpty()) {
                // Hiển thị tin nhắn từ client lên giao diện
                controller.appendMessage(staffName, msg);
              }
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }).start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void setTxtServerPort(TextField txtServerPort) {
    this.txtServerPort = txtServerPort;
  }

  public void setTabPane(TabPane tabPane) {
    this.tabPane = tabPane;
  }

  public static void main(String[] args) {
    launch(args);
  }
}
