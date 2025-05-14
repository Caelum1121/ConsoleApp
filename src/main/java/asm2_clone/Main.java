package asm2_clone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.SQLException;
import asm2_clone.db.DB_Connection;

public class Main extends Application {

    public static void main(String[] args) {
        try (Connection conaadmn = DB_Connection.getConnection()) {
            System.out.println("Connected to Supabase!");
            asm2_clone.db.LendingRecordDAO.autoFixMisclassifiedOverdue();
            asm2_clone.db.LendingRecordDAO.autoMarkOverdue();
            asm2_clone.db.LendingRecordDAO.updateReturnDates();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            
            primaryStage.setTitle("UNI LEND - Login");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
