//package edu.assessment;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.util.Iterator;
//import java.util.List;
//
//import edu.mit.jwi.Dictionary;
//import edu.mit.jwi.IDictionary;
//import edu.mit.jwi.item.IIndexWord;
//import edu.mit.jwi.item.ISynset;
//import edu.mit.jwi.item.ISynsetID;
//import edu.mit.jwi.item.IWord;
//import edu.mit.jwi.item.IWordID;
//import edu.mit.jwi.item.POS;
//import edu.mit.jwi.item.Pointer;
//
//public class T2 {
//
//	public static void main(String[] args) throws IOException {
//		testDictionary();
//	}
//
//	public static void testDictionary() throws IOException {
//
//		// construct the URL to the Wordnet dictionary directory
//		String wnhome = "/home/hung/Downloads/tmp"; // System.getenv(" WNHOME ");
//		String path = wnhome + File.separator + "dict";
//		URL url = new URL("file", null, path);
//
//		// construct the dictionary object and open it
//		IDictionary dict = new Dictionary(url);
//		dict.open();
//
//		getSynonyms(dict);
//		getHypernyms(dict);
//
//		// look up first sense of the word "dog "
//		IIndexWord idxWord = dict.getIndexWord("dog", POS.NOUN);
//		IWordID wordID = idxWord.getWordIDs().get(0);
//		IWord word = dict.getWord(wordID);
//		System.out.println("Id = " + wordID);
//		System.out.println("Lemma = " + word.getLemma());
//		System.out.println("Gloss = " + word.getSynset().getGloss());
//
//	}
//
//	public static void getSynonyms(IDictionary dict) {
//
//		// look up first sense of the word "dog "
//		IIndexWord idxWord = dict.getIndexWord("dog", POS.NOUN);
//		IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
//		IWord word = dict.getWord(wordID);
//		ISynset synset = word.getSynset();
//
//		// iterate over words associated with the synset
//		for (IWord w : synset.getWords())
//			System.out.println(w.getLemma());
//	}
//
//	public static void getHypernyms(IDictionary dict) {
//
//		// get the synset
//		IIndexWord idxWord = dict.getIndexWord("dog", POS.NOUN);
//		IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
//		IWord word = dict.getWord(wordID);
//		ISynset synset = word.getSynset();
//
//		// get the hypernyms
//		List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
//
//		// print out each h y p e r n y m s id and synonyms
//		List<IWord> words;
//		for (ISynsetID sid : hypernyms) {
//			words = dict.getSynset(sid).getWords();
//			System.out.print(sid + " {");
//			for (Iterator<IWord> i = words.iterator(); i.hasNext();) {
//				System.out.print(i.next().getLemma());
//				if (i.hasNext())
//					System.out.print(", ");
//			}
//			System.out.println("}");
//		}
//	}
//}
