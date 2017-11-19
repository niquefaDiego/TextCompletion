package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WriterApplication extends Application
{
	
	@Override
    public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Predictor");
		AutocompleteTextArea textArea = new AutocompleteTextArea();
		
        VBox vbox = new VBox(textArea);
		Scene scene = new Scene( vbox, 600, 500 );
		vbox.prefHeightProperty().bind(scene.heightProperty());
		vbox.prefWidthProperty().bind(scene.widthProperty());
		textArea.prefWidthProperty().bind(vbox.prefWidthProperty());
		textArea.prefHeightProperty().bind(vbox.prefHeightProperty());

		
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
