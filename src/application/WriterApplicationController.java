package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WriterApplicationController
{
	@FXML AutocompleteTextArea autocompleteTextArea;
	
	@FXML Label previousWord;
	@FXML Label previousWordType;
	@FXML Label currentWord;
	@FXML Label currentWordExpectedType;
	
	@FXML
	public void initialize() {
		autocompleteTextArea.setText("Texto inicial :)" );
	}
}
