package org.example.chatapplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
  public static final String URL = "jdbc:mysql://localhost:3306/chatapp";
  public static final String USERNAME = "root";
  public static final String PASSWORD = "password";

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USERNAME, PASSWORD);
  }
}
