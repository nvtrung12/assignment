package edu.assessment;

import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObject;

import parser.Utils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.ling.CoreAnnotations.StemAnnotation;

/**
 * https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/parser/nndep/demo/DependencyParserDemo.java
 * Demonstrates how to first use the tagger, then use the NN dependency parser.
 * Note that the parser will not work on untagged text.
 *
 * @author Jon Gauthier
 */
public class Test {

	static String modelPath = DependencyParser.DEFAULT_MODEL;
	static String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";

	static MaxentTagger tagger = new MaxentTagger(taggerPath);
	static DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

	public static void main(String[] args) {
		// test1(args);
		// test2();
		 test3();
		// test4();
//		test5();
	}

	private static void test5() {
		// Create a document. No computation is done yet.
		Document doc = new Document("add your text here! It can contain multiple sentences.");
		for (Sentence sent : doc.sentences()) { // Will iterate over two sentences
			// We're only asking for words -- no need to load any models yet
			System.out.println("The second word of the sentence '" + sent + "' is " + sent.word(1));
			// When we ask for the lemma, it will load and run the part of speech tagger
			System.out.println("The third lemma of the sentence '" + sent + "' is " + sent.lemmas());
			// When we ask for the parse, it will load and run the parser
			System.out.println("The parse of the sentence '" + sent + "' is " + sent.parse());
			// ...
		}
	}

	private static void test4() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);
		String text = "we have to many babies. the implementations are very important. implementation have";
		Annotation document = pipeline.process(text);

		for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				String lemma = token.get(LemmaAnnotation.class);
				System.out.println("lemmatized version :" + lemma + "-" + word);
			}
		}

	}

	private static void test3() {
		// Stemmer s = new Stemmer();
		JsonObject value = Json.createObjectBuilder().add("firstName", "John").add("lastName", "Smith").add("age", 25)
				.add("address",
						Json.createObjectBuilder().add("streetAddress", "21 2nd Street").add("city", "New York")
								.add("state", "NY").add("postalCode", "10021"))
				.add("phoneNumber",
						Json.createArrayBuilder()
								.add(Json.createObjectBuilder().add("type", "home").add("number", "212 555-1234"))
								.add(Json.createObjectBuilder().add("type", "fax").add("number", "646 555-4567")))
				.build();
		System.out.println(value);
	}

	private static void test2() {
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

		lp.setOptionFlags(new String[] { "-maxLength", "80", "-retainTmpSubcategories" });

		String sent = "My name is Rahul";
		List<HasWord> w = Utils.getWords(sent).get(0);
		Tree parse = (Tree) lp.apply(w);
		double score = parse.score();
		System.out.println(parse);
		System.out.println(score);
	}

	private static void test1(String[] args) {
		String text = "I can almost always tell when movies use fake dinosaurs.";
		for (int argIndex = 0; argIndex < args.length;) {
			switch (args[argIndex]) {
			case "-tagger":
				taggerPath = args[argIndex + 1];
				argIndex += 2;
				break;
			case "-model":
				modelPath = args[argIndex + 1];
				argIndex += 2;
				break;
			default:
				throw new RuntimeException("Unknown argument " + args[argIndex]);
			}
		}
		// String text;
		// if (args.length > 0) {
		// text = IOUtils.slurpFileNoExceptions(args[0], "utf-8");
		// } else {
		// text = "I can almost always tell when movies use fake dinosaurs.";
		// }

		for (int i = 0; i < 1000; ++i) {
			process(text);
			// process2(text);
		}
	}

	private static void process(String text) {

		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
		for (List<HasWord> sentence : tokenizer) {
			List<TaggedWord> tagged = tagger.tagSentence(sentence);
			System.out.println(tagged);
			GrammaticalStructure gs = parser.predict(tagged);
			System.out.println(gs.root());

			// Print typed dependencies
			// log.info(gs);
			System.out.println(gs);
		}
	}

	private static void process2(String text) {

		Annotation ann = new Annotation(text);

		Properties props = PropertiesUtils.asProperties("annotators", "tokenize,ssplit,pos,depparse", "depparse.model",
				DependencyParser.DEFAULT_MODEL);

		AnnotationPipeline pipeline = new StanfordCoreNLP(props);

		pipeline.annotate(ann);

		for (CoreMap sent : ann.get(CoreAnnotations.SentencesAnnotation.class)) {
			SemanticGraph sg = sent.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
			// log.info(IOUtils.eolChar +
			// sg.toString(SemanticGraph.OutputFormat.LIST));
			System.out.println(sg.toString(SemanticGraph.OutputFormat.LIST));
		}

	}

}
