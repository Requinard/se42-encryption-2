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
import sun.security.pkcs11.wrapper.CK_PKCS5_PBKD2_PARAMS;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
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
        btnEncrypt.setOnMouseClicked(event -> {
            try {
                encrypt(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        btnDecrypt.setOnMouseClicked(event -> {
            try {
                decrypt(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Cipher initCipher(byte[] salt) {
        // wrap key data in Key/IV specs to pass to cipher
        SecretKeySpec key = new SecretKeySpec(password.getText().getBytes(), "DES");
        IvParameterSpec ivSpec = new IvParameterSpec(salt);
// create the cipher with the algorithm you choose
// see javadoc for Cipher class for more info, e.g.
        try {
            return Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[8];
        secureRandom.nextBytes(salt);

        return salt;
    }

    private SecretKey getSecretKey(String password, byte[] salt) throws NoSuchAlgorithmException {
        /* Derive the key, given password and salt. */
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = null;
        try {
            tmp = factory.generateSecret(spec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private void encrypt(MouseEvent event) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Preconditions.checkArgument(!password.getText().isEmpty());
        Preconditions.checkArgument(!message.getText().isEmpty());

        // De mess staat voor semester 4 in zijn geheel
        String mess = message.getText();
        byte[] salt = generateSalt();

        Cipher cipher = initCipher(salt);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password.getText(), salt));

        byte[] encrypted = new byte[cipher.getOutputSize(mess.length())];
        int enc_len = cipher.update(mess.getBytes(), 0, mess.length(), encrypted, 0);
        enc_len += cipher.doFinal(encrypted, enc_len);

        writeEncrypted(salt, encrypted, enc_len, cipher.getIV());
    }

    private void writeEncrypted(byte[] salt, byte[] encrypted, int enc_len, byte[] iv) {
        File f = new File("text.enc");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(salt.length);
            fos.write(enc_len);
            fos.write(iv.length);
            fos.write(salt);
            fos.write(iv);
            fos.write(encrypted);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void decrypt(MouseEvent event) throws ShortBufferException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, InvalidKeyException {
        File f = new File("text.enc");

        FileInputStream fis = new FileInputStream(f);

        int saltLength = Math.abs(fis.read());
        int enc_len = Math.abs(fis.read());
        int iv_len = Math.abs(fis.read());

        byte[] salt = new byte[saltLength];
        byte[] iv = new byte[iv_len];
        byte[] mess = new byte[(int) (f.length() - 1 - saltLength - iv_len)];

        fis.read(salt, 0, saltLength);
        fis.read(iv, 0, iv_len);
        fis.read(mess);

        Cipher cipher = initCipher(salt);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password.getText(), salt), new IvParameterSpec(iv));

        byte[] decrypted = new byte[cipher.getOutputSize(enc_len)];
        cipher.update(mess, 0, enc_len, decrypted, 0);

        message.setText(new String(decrypted));


    }
}
