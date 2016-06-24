import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by David on 24-6-2016.
 */
public class Main extends Application {
    public void start(Stage primaryStage) throws Exception {
        Parent fxml = new FXMLLoader().load(this.getClass().getResourceAsStream("main.fxml"));
        primaryStage.setTitle("SE42 Encryption");
        primaryStage.setScene(new Scene(fxml, 800, 800));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
