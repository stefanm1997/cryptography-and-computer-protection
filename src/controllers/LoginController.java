package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
//import javax.security.cert.X509Certificate;
import java.util.Enumeration;

import application.Main;
import application.NewFormDecrypt;
import application.NewFormEncrypt;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.bouncycastle.asn1.x500.X500Name;
//import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

//import com.sun.security.auth.X500Principal;

public class LoginController {

	@FXML
	private RadioButton enk;

	@FXML
	private RadioButton dek;

	@FXML
	private TextField lab;

	@FXML
	private TextField name;

	@FXML
	private PasswordField pass;

	@FXML
	private Label label;

	ToggleGroup group = new ToggleGroup();
	private static String pom1;

	@FXML
	private void initialize() {
		enk.setToggleGroup(group);
		dek.setToggleGroup(group);
	}

	static String user;
	static String password;
	private String[] pom;
	private String path;
	public static String userName = null;

	@FXML
	void chooseSertificateWithButton() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("certificates"));
		fileChooser.setTitle("Choose sertificate");
		File f = fileChooser.showOpenDialog(null);
		if (f != null) {
			pom1 = f.getName().toString();
			path = f.getPath();
			lab.setText(path);
		}
	}

	private boolean isValid() {
		if ("".equals(name.getText()) || "".equals(pass.getText()) || "".equals(lab.getText()))
			return false;
		else
			return true;
	}

	// f-ja za dobijanje CN iz sertifikata
	public static String[] getCN(String path) {
		try {
			String certificateURL = path;
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate testCertificate = (X509Certificate) cf
					.generateCertificate(new FileInputStream(certificateURL));
			String test = testCertificate.getSubjectDN().getName();
			String[] split1 = test.split("\\,");
			String[] split2 = split1[1].split("=");
			return split2;
		} catch (Exception e) {
			new Alert(AlertType.ERROR, "Greska!!!").showAndWait();
		}
		return null;
	}

	// f-ja za provjeru ispravnosti korisnickog imena i lozinke
	private boolean isLogAndPassCorrect() throws Exception {
		String record = null;
		FileReader in = null;
		user = name.getText();
		password = Main.sha256(pass.getText());
		in = new FileReader("login" + File.separator + "login.txt");
		BufferedReader br = new BufferedReader(in);
		while ((record = br.readLine()) != null) {
			String[] split = record.split("#");
			if (user.equals(split[0]) && password.equals(split[1])) {
				return true;
			}
		}
		return false;
	}

	@FXML
	void openNewForm() {

		try {

			if (isValid()) {
				// provjera da li je sertifikat potpisan od strane CA tijela i da li je dobro
				// vrijeme trajanja sertifikata
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				FileInputStream input = new FileInputStream(path);
				X509Certificate c = (X509Certificate) cf.generateCertificate(input);
				c.checkValidity();
				c.verify(cf.generateCertificate(new FileInputStream("certificates" + File.separator + "rootca.csr"))
						.getPublicKey());

				// provjera da li se sertifikat nalazi u CRL listi
				X509CRLEntry revokedCertificate = null;
				X509CRL crl = null;
				crl = (X509CRL) cf.generateCRL(new FileInputStream("list" + File.separator + "lista.pem"));
				revokedCertificate = crl.getRevokedCertificate(c.getSerialNumber());

				if (isLogAndPassCorrect()) {
					pom = getCN(path);
					if (revokedCertificate == null) {
						if (user.equals(pom[1])) {
							if (enk.isSelected()) {
								userName = user;
								NewFormEncrypt form1 = new NewFormEncrypt();
								form1.openNewFormEncrypt("Encrypt.fxml", 560, 360);
							}
							if (dek.isSelected()) {
								userName = user;
								NewFormDecrypt form2 = new NewFormDecrypt();
								form2.openNewFormDecrypt("Decrypt.fxml", 598, 343);
							}
							((Stage) name.getScene().getWindow()).close();
						} else
							new Alert(AlertType.ERROR, "Nije izabaran odgovarajuci sertifikat!").showAndWait();
					} else
						new Alert(AlertType.ERROR, "Sertifikat je povucen(nalazi se u crl listi)").showAndWait();
				} else
					new Alert(AlertType.ERROR, "Pogresno korisnicko ime ili lozinka").showAndWait();

			} else
				new Alert(AlertType.ERROR, "Nisu popunjena sva polja!").showAndWait();
		} catch (CertificateExpiredException e) {
			new Alert(AlertType.ERROR, "Sertifikat je istekao!!!").showAndWait();
		} catch (CertificateNotYetValidException e) {
			new Alert(AlertType.ERROR, "Sertifikat jos nije validan!!!").showAndWait();
		} catch (SignatureException e) {
			new Alert(AlertType.ERROR, "Sertifikat nije potpisan od strane root ca!!!").showAndWait();
		} catch (Exception e) {
			new Alert(AlertType.ERROR, "Greska prilikom logovanja").showAndWait();

		}
	}
}
