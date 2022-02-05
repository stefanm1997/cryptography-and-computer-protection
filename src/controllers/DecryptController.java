package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class DecryptController {

	@FXML
	private TextField location1;

	@FXML
	private TextArea ta;

	@FXML
	private Button exe;

	private String path1;
	private static String pom1;
	private static String test = null;
	// private String userName = LoginController.userName;

	@FXML
	void chooseLocationOfEncryptCode() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("kript"));
		fileChooser.setTitle("Open Resource File");
		File f = fileChooser.showOpenDialog(null);
		if (f != null) {
			pom1 = f.getName().toString();
			path1 = f.getPath();
			location1.setText(path1);
		}
	}

	private boolean isValid() {
		if ("".equals(location1.getText()))
			return false;
		else
			return true;
	}

	@FXML

	void decryptSourceCode() {

		try {
			if (isValid()) {
				Cipher cipher1 = Cipher.getInstance("RSA");
				FileInputStream f1 = new FileInputStream(path1);
				test = EncryptController.compare();
				// String[] split = path2.split("\\\\");
				PrivateKey pk = EncryptController.createPrivateKeyFromPemFile("keys" + File.separator + test);
				cipher1.init(Cipher.DECRYPT_MODE, pk);

				// dekriptovanje user name posiljaoca
				byte[] pom1 = new byte[256];
				f1.readNBytes(pom1, 0, 256);
				byte[] plainText1 = cipher1.doFinal(pom1);
				FileOutputStream f2 = new FileOutputStream("pom" + File.separator + "user.txt");
				f2.write(plainText1);
				f2.close();

				// dekriptovanje simetricnog kljuca kojim je kriptovan source code
				byte[] pom2 = new byte[256];
				f1.readNBytes(pom2, 0, 256);
				byte[] plainText2 = cipher1.doFinal(pom2);
				FileOutputStream f3 = new FileOutputStream("pom" + File.separator + "simKey.txt");
				f3.write(plainText2);
				f3.close();

				// dekriptovanje simetricnog algoritma kojim je je kriptovan source code
				byte[] pom3 = new byte[256];
				f1.readNBytes(pom3, 0, 256);
				byte[] plainText3 = cipher1.doFinal(pom3);
				FileOutputStream f4 = new FileOutputStream("pom" + File.separator + "simAlg.txt");
				f4.write(plainText3);
				f4.close();

				// dekriptovanje hash f-je
				byte[] pom4 = new byte[256];
				f1.readNBytes(pom4, 0, 256);
				byte[] plainText4 = cipher1.doFinal(pom4);
				FileOutputStream f5 = new FileOutputStream("pom" + File.separator + "hash.txt");
				f5.write(plainText4);
				f5.close();

				// dekriptovanje digitalniog potpisa koda
				Signature signature = null;
				if ("SHA-256".equals(new String(plainText4))) {
					signature = Signature.getInstance("SHA256WithRSA");
				} else {
					signature = Signature.getInstance("SHA512WithRSA");
				}

				byte[] pom5 = new byte[256];
				f1.readNBytes(pom5, 0, 256);

				Cipher cipher2 = Cipher.getInstance(new String(plainText3));
				SecretKey originalKey = new SecretKeySpec(plainText2, 0, plainText2.length, new String(plainText3));
				cipher2.init(Cipher.DECRYPT_MODE, originalKey);
				byte[] pom6 = f1.readAllBytes();
				f1.close();

				byte[] plainText6 = cipher2.doFinal(pom6);
				FileOutputStream rez = new FileOutputStream("dekript" + File.separator + DecryptController.pom1);
				rez.write(plainText6);
				rez.close();

				FileInputStream fin = new FileInputStream(
						"certificates" + File.separator + new String(plainText1) + ".crt");
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				X509Certificate certificate = (X509Certificate) cf.generateCertificate(fin);
				PublicKey pub = certificate.getPublicKey();
				signature.initVerify(pub);
				signature.update(plainText6);
				boolean verified = true;
				verified = signature.verify(pom5);
				System.getProperties();
				Arrays.stream(new File("pom").listFiles()).forEach(File::delete);
				if (verified) {
					new Alert(AlertType.INFORMATION, "Verifikovano").showAndWait();
				} else
					new Alert(AlertType.ERROR, "Nije verifikovano").showAndWait();
				exe.setDisable(false);
			} else
				new Alert(AlertType.ERROR, "Nije izabran kod za dekripciju ili kljuc").showAndWait();
		} catch (BadPaddingException e) {
			exe.setDisable(true);
			new Alert(AlertType.ERROR, "Dekripcija nije moguca").showAndWait();
		} catch (FileNotFoundException e) {
			new Alert(AlertType.ERROR, "Ne postoji fajl").showAndWait();

		} catch (Exception e) {
			// e.printStackTrace();
			new Alert(AlertType.ERROR, "Greska prilikom dekripcije").showAndWait();
		}

	}

	@FXML
	void compileAndRunDecryptCode() {

		try {
			if (isValid()) {
				new Thread() {
					public void run() {
						try {
							Runtime rt = Runtime.getRuntime();
							// rt.exec("cmd /c start cmd.exe", null, new File("dekript"));
							rt.exec("javac " + pom1, null, new File("dekript"));
							sleep(2000);
							ta.setText(new String(rt.exec("java " + pom1.split("\\.")[0], null, new File("dekript"))
									.getInputStream().readAllBytes()));
						} catch (Exception e) {
							new Alert(AlertType.ERROR, "Greska prilikom pokretanja! ").showAndWait();
						}
					}
				}.start();
			} else
				new Alert(AlertType.ERROR, "Nije izabran kod za dekripciju").showAndWait();
		} catch (Exception e) {
			new Alert(AlertType.ERROR, "Greska prilikom pokretanja programa").showAndWait();
		}
	}

}
