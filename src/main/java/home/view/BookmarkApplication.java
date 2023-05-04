package home.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class BookmarkApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load fxml
        FXMLLoader fxmlLoader = new FXMLLoader(BookmarkApplication.class.getResource("/bookmark.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/stylesheet.css")).toExternalForm());
        // Useful
        // URL fxmlLocation = BookmarkApplication.class.getResource("/bookmark.fxml");
        // System.out.println("fxmlLocation: " + fxmlLocation);
        // System.out.println("fxml loaded");
        // Set Original Stage
        stage.setTitle("Bookmark");
        stage.setWidth(600);
        stage.setHeight(550);
        stage.setScene(scene);
        stage.show();


    }

    public static void main(String[] args) {
        launch();
    }
}