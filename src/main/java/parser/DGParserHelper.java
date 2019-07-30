package parser;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;

/**
 * 
 * @author hung
 * @since Feb 2018
 *
 *        English Dependency parser helper, load models only one. <br/>
 *        singleton design pattern
 */
public class DGParserHelper {
	static String modelPath = DependencyParser.DEFAULT_MODEL;
	static String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";

	private static DGParserHelper instance = null;

	MaxentTagger tagger;
	DependencyParser parser;

	/**
	 * singleton get instance
	 * 
	 * @return DGParserHelper an instance
	 */
	public static DGParserHelper getInstance() {
		if (null == instance) {
			instance = new DGParserHelper();
		}

		return instance;
	}

	/**
	 * constructor, load models only one time
	 */
	protected DGParserHelper() {
		tagger = new MaxentTagger(taggerPath);
		parser = DependencyParser.loadFromModelFile(modelPath);
	}

	/**
	 * parse many sentences
	 * 
	 * @param text
	 * @return list of dependency tree, one for each sentence from input text
	 */
	public List<GrammaticalStructure> parse(String text) {

		List<GrammaticalStructure> ret = new LinkedList<GrammaticalStructure>();

		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
		for (List<HasWord> sentence : tokenizer) {
			List<TaggedWord> tagged = tagger.tagSentence(sentence);
			GrammaticalStructure gs = parser.predict(tagged);

			ret.add(gs);
		}
		return ret;
	}
}
