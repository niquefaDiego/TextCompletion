package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WriterApplication extends Application{
	
	@Override
    public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle(":) :) :)");
		AutocompleteTextArea textField = new AutocompleteTextArea();
        VBox vbox = new VBox(textField);
		Scene scene = new Scene( vbox, 300, 500 );
		
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
