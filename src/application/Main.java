package application;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
//import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader
					.load(new File("src" + File.separator + "views" + File.separator + "Login.fxml").toURI().toURL());
			Scene scene = new Scene(root, 613, 362);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setTitle("Prijava na aplikaciju");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		launch(args);
		// String rez = sha256("korisnik4");
		// System.out.println(rez);
	}

	public static String sha256(String base) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(base.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
