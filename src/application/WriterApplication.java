package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WriterApplication extends Application
{
	@Override
    public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Predictor");
		
		FXMLLoader fxmlLoader = new FXMLLoader(ListPopup.class.getResource("WriterApplication.fxml"));
		Parent content = fxmlLoader.load();
		WriterApplicationController controller = (WriterApplicationController) fxmlLoader.getController();
		Scene scene = new Scene(content );
		
		controller.root.prefHeightProperty().bind( scene.heightProperty() );
		controller.root.prefWidthProperty().bind(scene.widthProperty());
		
		controller.autocompleteTextArea.prefHeightProperty().bind( controller.root.heightProperty() );
		
		/*AutocompleteTextArea textArea = new AutocompleteTextArea();
		
        VBox vbox = new VBox(textArea);
		Scene scene = new Scene( vbox, 600, 500 );
		vbox.prefHeightProperty().bind(scene.heightProperty());
		vbox.prefWidthProperty().bind(scene.widthProperty());
		textArea.prefWidthProperty().bind(vbox.prefWidthProperty());
		textArea.prefHeightProperty().bind(vbox.prefHeightProperty());

		
		primaryStage.setScene(scene);*/
		primaryStage.setScene( scene );
		primaryStage.show();
	}
}
