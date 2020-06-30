/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deskspace;

import insidefx.undecorator.UndecoratorScene;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.sql.*;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.WindowEvent;

/**
 *
 * @author mrbri
 */

public class DeskSpace extends Application {
    
    public static Connection cn;
    public static String jarPath;
    
    @Override
    public void start(Stage stage) throws Exception {
        connect();
        Region root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        
        final UndecoratorScene undecoratorScene = new UndecoratorScene(stage, root);
        undecoratorScene.addStylesheet("deskspace/stylesheet.css");
        undecoratorScene.setFadeInTransition();

        //Fade out transition on window closing request
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                we.consume();   // Do not hide yet
                undecoratorScene.setFadeOutTransition();
                System.exit(0); // Well this screws it up.
            }
        });
        
        stage.getIcons().add(new Image("https://i.pinimg.com/736x/43/ba/15/43ba"
                + "150dbeb6ca83c1ba57bc9bd9b285--perler-bead-designs-hama-beads"
                + "-design.jpg")); //LOL Hardcoded xD
        stage.setTitle("DeskSpace");
        stage.setScene(undecoratorScene);
        stage.show();
        
        final Class<?> referenceClass = DeskSpace.class;
        final URL url =
            referenceClass.getProtectionDomain().getCodeSource().getLocation();
        jarPath = String.valueOf(new File(url.toURI()).getParentFile());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
        
    }
    
    private static void connect(){
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url= String.format("jdbc:mysql://130.211.163.91:3306/deskspace");
            cn = DriverManager.getConnection(url, "root", "root");
            System.out.println("Connection Successful");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
}
