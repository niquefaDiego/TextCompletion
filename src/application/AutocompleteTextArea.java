package application;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;

public class AutocompleteTextArea extends TextArea implements AutocompleteCallback
{
	public static final int MAX_ENTRIES = 5;
	
	//TODO remove when Trie is implemented :)
	private final SortedSet<String> entries = new TreeSet<>();
	
	private ListPopup dropdownList = null;
	private int wordBeg, wordEnd; // [) range of the word being typed
	
	private void hideDropdown() {
		if ( dropdownList != null )
			dropdownList.hide();
	}
		
	private void showPopup(List<String> list, int wordBeg, int wordEnd) {
		hideDropdown();
		try {
			Point2D position = this.localToScreen(40, 40);
			dropdownList = new ListPopup(list, position, this);
			dropdownList.show(getScene().getWindow());
		} catch ( IOException e ) {
			e.printStackTrace();
			System.err.println("Couldn't initialize dropdown list :c");
		}
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
		System.out.println("word being typed = '" + word + "'" );
		
		if ( word == null ) {
			hideDropdown();
			return;
		}
		
		LinkedList<String> searchResult = new LinkedList<>();
		searchResult.addAll(entries.subSet(word, word + Character.MAX_VALUE));
		if (searchResult.size() > 0)
		{
			System.out.println("show dropdown");
			showPopup(searchResult, wordBeg, wordEnd);
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
