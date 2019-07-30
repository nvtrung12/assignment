package edu.assessment.parser;

import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.GrammaticalStructure;
import parser.DGParserHelper;
import parser.Utils;

/**
 * 
 * @author hung
 * @since Feb 2018
 * 
 *        Test for DGParserHelper
 */
public class TestDGParserHelper {

	public static void main(String[] args) {
		test1(args);

	}

	private static void test1(String[] args) {
		String text = "I can almost always tell when movies use fake dinosaurs.";

		// String text;
		// if (args.length > 0) {
		// text = IOUtils.slurpFileNoExceptions(args[0], "utf-8");
		// } else {
		// text = "I can almost always tell when movies use fake dinosaurs.";
		// }

		System.out.println("start load models");
		DGParserHelper dgparserhelper = DGParserHelper.getInstance();

		System.out.println("starting run");
		long millis = System.currentTimeMillis();

		for (int i = 0; i < 1; ++i) {

			List<List<TaggedWord>> tagged = Utils.getTag(text);
			if (tagged.size() > 0)
				System.out.println(tagged.get(0));

			List<GrammaticalStructure> out = dgparserhelper.parse(text);
			for (GrammaticalStructure gs : out) {
				System.out.println(gs);
				System.out.println(gs.allTypedDependencies());

			}
		}
		long ti = System.currentTimeMillis() - millis;
		System.out.println("Run time for 1000: " + ti);
	}
}
