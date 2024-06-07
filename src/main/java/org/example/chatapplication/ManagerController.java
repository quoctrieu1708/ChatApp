package org.example.chatapplication;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ManagerController extends Application {
  @FXML
  private TextField txtServerPort;
  @FXML
  private TabPane tabPane;

  private ServerSocket serverSocket;
  private String staffName;

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("ManagerChatter.fxml"));
    loader.setController(this);
    Parent root = loader.load();

    primaryStage.setTitle("Manager Chat");
    primaryStage.setScene(new Scene(root, 600, 480));
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
        controller.initializeSocket(clientSocket, staffName);

        DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());

        DataInputStream reader = new DataInputStream(clientSocket.getInputStream());
        String staffName = reader.readUTF();
        this.staffName = staffName;
        Tab tab = new Tab(staffName);
        tab.setContent(root);
        tabPane.getTabs().add(tab);

        new Thread(() -> {
          try {
            while (true) {
              String msg = reader.readUTF();
              if (msg != null && !msg.isEmpty()) {
                Platform.runLater(() -> controller.txtMessage.appendText(msg + "\n"));
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
