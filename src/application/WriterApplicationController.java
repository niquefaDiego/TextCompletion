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
		autocompleteTextArea.setWriterApplicationController ( this );
		
		previousWord.textProperty().bind( autocompleteTextArea.getData().getPreviousWord());
		previousWordType.textProperty().bind( autocompleteTextArea.getData().getPreviousWordType());
		currentWord.textProperty().bind( autocompleteTextArea.getData().getCurrentWord());
		currentWordExpectedType.textProperty().bind( autocompleteTextArea.getData().getCurrentWordExpectedType());
	}
}
