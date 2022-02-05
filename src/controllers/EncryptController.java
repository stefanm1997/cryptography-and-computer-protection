package controllers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import application.Main;

import java.security.Signature;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javax.crypto.Cipher;

public class EncryptController {

	@FXML
	private TextField lab1;

	@FXML
	private TextField lab2;

	@FXML
	private ComboBox<String> combo1;

	@FXML
	private ComboBox<String> combo2;

	@FXML
	private Button enk;


	private static String userName = LoginController.userName;
	private static String pom1, pom2;
	private String test = null;
	private static PrivateKey privateKey = null;
	private String path1, path2;
	private String[] CNname;

	@FXML
	void chooseLocationOfSourceCode() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("file"));
		fileChooser.setTitle("Open Resource File");
		File f = fileChooser.showOpenDialog(null);
		if (f != null) {
			pom1 = f.getName().toString();
			path1 = f.getPath();
			lab1.setText(path1);
		}
	}

	@FXML
	void chooseLocationOfCertificate() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("certificates"));
		fileChooser.setTitle("Open certificate");
		File f = fileChooser.showOpenDialog(null);
		if (f != null) {
			pom2 = f.getName().toString();
			path2 = f.getPath();
			lab2.setText(path2);
		}

	}

	@FXML
	void initialize() {
		combo1.getItems().addAll("DESede", "AES", "Blowfish");
		combo1.setValue("DESede");
		combo2.getItems().addAll("SHA-256", "SHA-512");
		combo2.setValue("SHA-256");

	}

	private boolean isValid() {
		if ("".equals(lab2.getText()) || "".equals(lab1.getText()))
			return false;
		else
			return true;
	}

	// f-ja koja cupa privatni kljuc iz asimetricnog RSA kljuca
	public static PrivateKey createPrivateKeyFromPemFile(final String keyFileName)
			throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		// Loads a privte key from the specified key file name
		final PemReader pemReader = new PemReader(new FileReader(keyFileName));
		final PemObject pemObject = pemReader.readPemObject();
		final byte[] pemContent = pemObject.getContent();
		pemReader.close();
		final PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(pemContent);
		final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		final PrivateKey priv = keyFactory.generatePrivate(encodedKeySpec);
		return priv;
	}

	// f-ja koja poredi user name i folder keys da bi pronasao ime kljuca
	public static String compare() {
		File folder = new File("keys");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			String[] split = listOfFiles[i].getName().split("\\.");
			if (userName.equals(split[0])) {
				return listOfFiles[i].getName();
			}

		}
		return null;
	}

	@FXML
	void encryptSourceCode() {
		try {
			byte[] digitalSignature = null;

			// generisanje kljuceva
			int keyBitSize = 56;
			KeyGenerator keyGenerator = KeyGenerator.getInstance(combo1.getValue());
			SecureRandom secureRandom = new SecureRandom();
			if ("DESede".equals(combo1.getValue()))
				keyBitSize = 168;
			if ("AES".equals(combo1.getValue()))
				keyBitSize = 256;
			if ("BlowFish".equals(combo1.getValue()))
				keyBitSize = 448;
			keyGenerator.init(keyBitSize, secureRandom);
			SecretKey secretKey = keyGenerator.generateKey();
			if (isValid()) {
				// dobijanje CN primaoca i provjera da li je text box popunjen tj da li ima ista
				// u njemu
				CNname = LoginController.getCN(path2);
				// System.out.println(CNname[1]);

				// kriptovanje source koda simetricnim kljucem koji je prethodno generisan
				Cipher cipher1 = Cipher.getInstance(combo1.getValue());
				FileInputStream file = new FileInputStream(path1);
				cipher1.init(Cipher.ENCRYPT_MODE, secretKey);
				byte[] plainText1 = file.readAllBytes();
				byte[] cipherText1 = cipher1.doFinal(plainText1);
				FileOutputStream f = new FileOutputStream("kript" + File.separator + EncryptController.pom1);
				// f.write(cipherText1);

				// dobijanje javnog kljuca primaoca iz njegovog sertifikata
				FileInputStream fin = new FileInputStream(path2);
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				X509Certificate certificate = (X509Certificate) cf.generateCertificate(fin);
				PublicKey pk = certificate.getPublicKey();
				Cipher cipher2 = Cipher.getInstance("RSA");
				cipher2.init(Cipher.ENCRYPT_MODE, pk);

				// kriptovanje user name posiljaoca
				byte[] plainText2 = userName.getBytes();
				byte[] cipherText2 = cipher2.doFinal(plainText2);
				f.write(cipherText2);
				// System.out.println("Duzina usr " + cipherText2.length);

				// kriptovanje simetricnog kljuca kojim je kriptovan source code
				byte[] plainText3 = secretKey.getEncoded();
				byte[] cipherText3 = cipher2.doFinal(plainText3);
				f.write(cipherText3);
				// System.out.println("Duzina key " + cipherText3.length);

				// kriptovanje simetricnog algoritma kojim je je kriptovan source code
				byte[] plainText4 = combo1.getValue().getBytes();
				byte[] cipherText4 = cipher2.doFinal(plainText4);
				f.write(cipherText4);
				// System.out.println("Duzina alg " + cipherText4.length);

				// kriptovanje hash f-je
				byte[] plainText5 = combo2.getValue().getBytes();
				byte[] cipherText5 = cipher2.doFinal(plainText5);
				f.write(cipherText5);
				// System.out.println("Duzina hes " + cipherText5.length);

				Signature signature = null;
				if ("SHA-256".equals(combo2.getValue())) {
					signature = Signature.getInstance("SHA256WithRSA");
				} else {
					signature = Signature.getInstance("SHA512WithRSA");
				}

				// kriptovanje digitalniog potpisa koda
				test = compare();
				privateKey = EncryptController.createPrivateKeyFromPemFile("keys" + File.separator + test);
				signature.initSign(privateKey, secureRandom);
				byte[] pomocna = file.readAllBytes();
				signature.update(plainText1);
				digitalSignature = signature.sign();
				f.write(digitalSignature);
				// System.out.println("Duzina dp " + digitalSignature.length);
				f.write(cipherText1);
				// System.out.println("Duzina ct " + cipherText1.length);
				f.close();
				new Alert(AlertType.INFORMATION,"Enkripcija je izvrsena!!!").showAndWait();
			} else
				new Alert(AlertType.ERROR,"Nisu popunjena sva polja!!!").showAndWait();
				
		} catch (Exception e) {
			new Alert(AlertType.ERROR,"Greska prilikom enkripcije!!!").showAndWait();
		}

	}
}
