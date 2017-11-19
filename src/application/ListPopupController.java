package application;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class ListPopupController {
	
	@FXML
	ListView<String> lvCompletion;
	
	private AutocompleteCallback callback;
	
	@FXML public void handleMouseClick() {
	    callback.autocomplete(getSelection());
	}
	
	public void selectCurrent() {
		callback.autocomplete(getSelection());
	}
	
	public void setAutocompleteCallback( AutocompleteCallback callback ) {
		this.callback = callback;
	}
	
	void selectNext() {
		lvCompletion.getSelectionModel().selectNext();
	}
	
	void selectPrev() {
		lvCompletion.getSelectionModel().selectPrevious();
	}
	
	public String getSelection() {
		String value = lvCompletion.getSelectionModel().getSelectedItem();
		if ( value == null )
			return lvCompletion.getItems().get(0);
		return value;
	}
	
	void populateList ( List<String> list ) {
		lvCompletion.getItems().addAll( list );
	}
	
	@FXML
	public void initialize() {
		lvCompletion.getSelectionModel().selectFirst();
	}
}
