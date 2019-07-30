package parser;

import java.io.Serializable;

import edu.stanford.nlp.ling.IndexedWord;

/**
 * 
 * @author thanhhungqb
 *
 */
public class ExtendIndexedWord extends IndexedWord implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String phrase;
	private String phraseIndex;
	private String srole;
	private String nodeType;

	/**
	 * copy constructor
	 * 
	 * @param word
	 */
	public ExtendIndexedWord(IndexedWord word) {
		super(word);
	}

	public ExtendIndexedWord() {

	}

	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	public String getPhraseIndex() {
		return phraseIndex;
	}

	public void setPhraseIndex(String phraseIndex) {
		this.phraseIndex = phraseIndex;
	}

	public String getSrole() {
		return srole;
	}

	public void setSrole(String srole) {
		this.srole = srole;
	}
	
	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	

	@Override
	public int hashCode() {
		return this.phraseIndex.hashCode() * 101 + this.phrase.hashCode();
	}
}
