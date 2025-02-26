/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dbmsminute;
import java.sql.*;


public class DBUtil {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/minute";
    private static final String USER = "minuter";
    private static final String PASSWORD = "minuter@2025";
    
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
