package application;

import java.io.File;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NewFormDecrypt {

	public void openNewFormDecrypt(String fxmlFile,int height,int width) {
		try {
			Stage primaryStage=new Stage(); 
			Parent root = FXMLLoader.load(new File("src"+File.separator+"views"+File.separator+ fxmlFile).toURI().toURL());
			Scene scene = new Scene(root,height,width);
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setTitle("Dekripcija");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
