package application;

import java.io.IOException;
import java.util.List;

import core.DBParser;
import core.Tags;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Path;
import javafx.stage.Window;


public class AutocompleteTextArea extends TextArea implements AutocompleteCallback
{
	public static final int MAX_ENTRIES = 8;

	private Data data = new Data();
	private ListPopup dropdownList = null;
	
	// [) range of the word being typed, only used if dropdownList != null
	private int wordBeg = -1, wordEnd = -1;
	
	private WriterApplicationController applicationController;
	
	private void hideDropdown() {
		if ( dropdownList != null ) {
			dropdownList.hide();
			dropdownList = null;
		}
	}
	
	public void setWriterApplicationController ( WriterApplicationController controller ) {
		applicationController = controller;
	}
	
	public Data getData() {
		return data;
	}
	
	// --------------- START OF HACK ---------------------
	// How to know the caret position:
	// https://community.oracle.com/thread/2534556
	
	private Path findCaret(Parent parent) {
		// Warning: this is an ENORMOUS HACK
		for (Node n : parent.getChildrenUnmodifiable()) {
			if (n instanceof Path) {
				return (Path) n;
			} else if (n instanceof Parent) {
				Path p = findCaret((Parent) n);
				if (p != null) {
					return p;
				}
			}
		}
		return null;
	}

	private Point2D findScreenLocation(Node node) {
		if ( node == null ) return new Point2D(0,0);
		double x = 0;
		double y = 0;
		for (Node n = node; n != null; n=n.getParent()) {
			Bounds parentBounds = n.getBoundsInParent();
			x += parentBounds.getMinX();
			y += parentBounds.getMinY();
		}
		Scene scene = node.getScene();
		x += scene.getX();
		y += scene.getY();
		Window window = getScene().getWindow();
		x += window.getX();
		y += window.getY();
		Point2D screenLoc = new Point2D(x, y);
		return screenLoc;
	}
	// ---------------- END OF HACK --------------------
	
	/**
	 * Creates ands shows the Popup if it's not showing (dropdownList is null).
	 * Updates the list in Popup if it's showing (dropdownList is not null).
	 * @param list
	 * @param wordBeg
	 * @param wordEnd
	 */
	private synchronized void showInPopup(List<String> list) {
		if ( dropdownList == null ) {
			try {
				Path caret = findCaret(this);
		 		Point2D position = findScreenLocation(caret).add(0,10);
				dropdownList = new ListPopup(list, position, this);
			} catch ( IOException e ) {
				e.printStackTrace();
				System.err.println("Couldn't initialize dropdown list :c");
			}			
		}
		else {
			dropdownList.getController().populateList(list);
		}
		dropdownList.show(getScene().getWindow());
	}
	
	@Override
	public void autocomplete( String value ) {
		String currentText = getText();
		char[] val = value.toCharArray();
		String currentWord = data.currentWord.getValue();
		for ( int i = 0; i < val.length && i < currentWord.length(); ++i )
			if ( Character.isUpperCase(currentWord.charAt(i)) )
				val[i] = Character.toUpperCase(val[i]);
		String newText = currentText.substring(0,wordBeg)
				+ new String(val)
				+ currentText.substring(wordEnd);
		setText(newText);
		positionCaret(wordBeg + value.length());
		hideDropdown();
	}
	
	private void wordBeingTypedChanged ( String currentWord, String previousWord ) {
		
		System.out.println( "prevWord = " + previousWord + ", current word = " + currentWord );
		
		if ( currentWord == null ) {
			data.resetValues();
			hideDropdown();
			return;
		}
		
		DBParser parser = DBParser.getInstance();
		List<String> searchResult = parser.getPredictions(previousWord, currentWord, MAX_ENTRIES);
		
		if ( applicationController != null ) {
			String previousWordType = parser.getType(previousWord);
			String expectedWordType = Tags.getName(
					parser.getExpectedType(previousWordType, currentWord));
			data.setValues(
					previousWord, Tags.getName(previousWordType),
					currentWord, expectedWordType );
		}
		
		if (searchResult.size() > 0) {
			showInPopup(searchResult);
		} else {
			hideDropdown();
		}
	}
	
	public AutocompleteTextArea() {
	    super();
	    textProperty().addListener(new TextChangeListener());  
	}
	
	private class TextChangeListener implements ChangeListener<String> {
		@Override
		public void changed(ObservableValue<? extends String> observableValue, String oldText, String newText) {
			int caretPos =getCaretPosition();

			if ( !newText.isEmpty() )
			{
				//try find word being typed
				int beg = Math.max(0, Math.min(caretPos, newText.length()-1));
				int end = beg;
				int iters = 0;

				final int MAX_ITERS = 100; //all words have less than 100 letters U_U
				while ( beg >= 0 && Character.isLetter(newText.charAt(beg))
						&& iters++ < MAX_ITERS) beg--;
				beg++;
				
				while ( end < newText.length()
						&& Character.isLetter(newText.charAt(end))
						&& iters++ < MAX_ITERS ) end++;
				
				if ( beg < end && iters < MAX_ITERS ) {
					//new word is being typed
					if ( wordBeg != beg ) {
						hideDropdown();
					}
					
					
					wordBeg = beg; wordEnd = end;
					
					//find previus word
					iters = 0;
					int j = wordBeg-1;
					while ( j >= 0 && Character.isWhitespace(newText.charAt(j))
							&& iters++ < MAX_ITERS ) j--;
					int i = j;
					if ( j >= 0 && Character.isLetter(newText.charAt(j)) ) {
						while ( i >= 0 && Character.isLetter(newText.charAt(i))
								&& iters++ < MAX_ITERS ) i--;
						i++;
					}
					else if ( j >= 0 && Character.isDigit(newText.charAt(j)) ) {
						while ( i >= 0 && Character.isDigit(newText.charAt(i))
								&& iters++ < MAX_ITERS ) i--;
						i++;
					}
					
					String previousWord = null;
					if ( j >= 0 && iters < MAX_ITERS ) {
						previousWord = newText.substring(i,j+1);
					}
					wordBeingTypedChanged(newText.substring(wordBeg, wordEnd), previousWord);	
				}
				else {
					hideDropdown();
					data.resetValues();
				}
			}
			else {
				data.resetValues();
				hideDropdown();
			}
		}
	}
	
	public static class Data {
		StringProperty previousWord = new SimpleStringProperty();
		StringProperty previousWordType = new SimpleStringProperty();
		StringProperty currentWord = new SimpleStringProperty();
		StringProperty currentWordExpectedType = new SimpleStringProperty();
		
		public StringProperty getPreviousWord() {
			return previousWord;
		}
		
		public StringProperty getPreviousWordType() {
			return previousWordType;
		}

		public StringProperty getCurrentWord() {
			return currentWord;
		}
		
		public StringProperty getCurrentWordExpectedType() {
			return currentWordExpectedType;
		}
		
		public void resetValues() {
			this.previousWord.setValue("");
			this.previousWordType.setValue("");
			this.currentWord.setValue("");
			this.currentWordExpectedType.setValue("");
		}
		
		public void setValues(
				String previousWord,
				String previousWordType,
				String currentWord,
				String currentWordExpectedType) {
			
			if ( previousWord == null )
				previousWord = previousWordType = "";
			else if ( previousWordType == null )
				previousWordType = "Desconocido";
			
			if ( currentWord == null )
				currentWord = currentWordExpectedType = null;
			else if ( currentWordExpectedType == null )
				currentWordExpectedType = "Desconocido";
			
			this.previousWord.setValue(previousWord);
			this.previousWordType.setValue(previousWordType);
			this.currentWord.setValue(currentWord);
			this.currentWordExpectedType.setValue(currentWordExpectedType);
		}
		
		public Data() {
			resetValues();
		}
	}
}
