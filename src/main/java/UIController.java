import com.google.common.base.Preconditions;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import org.apache.commons.lang.ArrayUtils;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ResourceBundle;

/**
 * Created by David on 24-6-2016.
 */
public class UIController implements Initializable {
    @FXML
    private PasswordField password;

    @FXML
    private TextArea message;

    @FXML
    private Button btnEncrypt;

    @FXML
    private Button btnDecrypt;

    private File file;
    
    public void initialize(URL location, ResourceBundle resources) {
        btnEncrypt.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                encrypt(event);
            }
        });
        btnDecrypt.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                decrypt(event);
            }
        });
    }

    private void encrypt(MouseEvent event) {
        Preconditions.checkArgument(!password.getText().isEmpty());
        Preconditions.checkArgument(!message.getText().isEmpty());

        BASE64Encoder encoder = new BASE64Encoder();
        String encrypted = encoder.encode(message.getText().getBytes());

        // salt
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[128];
        secureRandom.nextBytes(salt);

        byte[] saltedPassword = ArrayUtils.addAll(salt, password.getText().getBytes());


    }

    private void decrypt(MouseEvent event) {
    }
}
