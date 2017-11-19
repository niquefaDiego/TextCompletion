package application;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
	public static final int MAX_ENTRIES = 5;
	
	//TODO remove when Trie is implemented :)
	private final SortedSet<String> entries = new TreeSet<>();
	
	private ListPopup dropdownList = null;
	
	// [) range of the word being typed, only used if dropdownList != null
	private int wordBeg = -1, wordEnd = -1;
	
	private void hideDropdown() {
		if ( dropdownList != null ) {
			dropdownList.hide();
			dropdownList = null;
		}
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
		System.out.println( "autocompleted '" + value + "'");
		String currentText = getText(); 
		String newText = currentText.substring(0,wordBeg)
				+ value
				+ currentText.substring(wordEnd);
		setText(newText);
		positionCaret(wordBeg + value.length());
		hideDropdown();
	}
	
	private void wordBeingTypedChanged ( String word ) {
		
		if ( word == null ) {
			hideDropdown();
			return;
		}
		
		LinkedList<String> searchResult = new LinkedList<>();
		searchResult.addAll(entries.subSet(word, word + Character.MAX_VALUE));
		if (searchResult.size() > 0)
		{
			showInPopup(searchResult);
		}
		else
			hideDropdown();
	}
	
	public AutocompleteTextArea() {
	    super();
	    
	    //TODO remove this
	    String words[] = { "diego", "said", "velasquez", "dado", "duda", "dudu" };
	    for ( String word : words )
	    	entries.add(word);
	    
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

				//all words have less than 100 letters U_U
				final int MAX_ITERS = 100;
				while ( beg >= 0 && Character.isLetter(newText.charAt(beg)) && iters < MAX_ITERS)
					beg--;
				beg++;
				while ( end < newText.length() && Character.isLetter(newText.charAt(end)) && iters < MAX_ITERS )
					end++;
				
				if ( beg < end && iters < MAX_ITERS ) {
					//new word is being typed
					if ( wordBeg != beg ) {
						hideDropdown();
					}
					wordBeg = beg; wordEnd = end;
					wordBeingTypedChanged(newText.substring(wordBeg, wordEnd));	
				}
				else
					hideDropdown();
			}
			else
				hideDropdown();
		}
	}
}
