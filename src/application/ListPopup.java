package application;

import java.io.IOException;
import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;

public class ListPopup extends Popup
{
	private static final int LV_ROW_HEIGHT = 24;
	
	private ListPopupController controller;
	
	public ListPopupController getController() {
		return controller;
	}
	
	public ListPopup(
			List<String> list,
			Point2D position,
			AutocompleteCallback callback)
			throws IOException
	{
		
		FXMLLoader fxmlLoader = new FXMLLoader(ListPopup.class.getResource("ListPopup.fxml"));
		Parent content = fxmlLoader.load();

		controller = (ListPopupController)fxmlLoader.getController();
		controller.populateList(list);
		controller.setAutocompleteCallback(callback);

		final ObservableList<String> items = controller.lvCompletion.getItems();
		
		controller.lvCompletion.setPrefHeight(items.size()*LV_ROW_HEIGHT + 2);
		items.addListener( new ListChangeListener<String>() {
				@Override
				public void onChanged(Change<? extends String> c) {
					controller.lvCompletion.setPrefHeight(items.size()*LV_ROW_HEIGHT + 2);
				}
			});
		
		controller.lvCompletion.setOnKeyPressed( evt -> {
				if ( evt.getCode() == KeyCode.TAB ) {
					if ( evt.isShiftDown() ) controller.selectPrev();
					else controller.selectNext();
				}
				else if ( evt.getCode() == KeyCode.ENTER )
					controller.selectCurrent();
			});
			
		setX( position.getX() );
		setY( position.getY() );
		setAutoHide(true);		
		getContent().add( content );
	}
}
