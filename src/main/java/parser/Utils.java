package parser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;

/**
 * 
 * @author hung
 *
 *         Implement some useful functions for parser
 */
public class Utils {
	static String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
	static String modelPath = DependencyParser.DEFAULT_MODEL;

	public static LexicalizedParser lp = null;

	/**
	 * Get list of list word (document preprocessing)
	 * 
	 * @param docContent
	 *            document includes many sentences
	 * @return list of list word for each sentence
	 */
	public static List<List<HasWord>> getWords(String docContent) {

		List<List<HasWord>> ret = new LinkedList<List<HasWord>>();

		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(docContent));
		for (List<HasWord> sentence : tokenizer) {
			ret.add(sentence);
		}
		return ret;
	}

	/**
	 * 
	 * @param document
	 *            a document includes one or many sentences
	 * @return a list of list include TaggedWord word/tag
	 */
	public static List<List<TaggedWord>> getTag(String document) {

		MaxentTagger tagger = new MaxentTagger(taggerPath);
		List<List<TaggedWord>> ret = new LinkedList<List<TaggedWord>>();

		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(document));
		for (List<HasWord> sentence : tokenizer) {
			List<TaggedWord> tagged = tagger.tagSentence(sentence);
			// System.out.println(tagged);
			ret.add(tagged);
		}
		return ret;
	}

	public static Tree parseTreeCFG(List<TaggedWord> words) {
		if (lp == null)
			lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

		lp.setOptionFlags(new String[] { "-maxLength", "80", "-retainTmpSubcategories" });

		Tree parse = (Tree) lp.apply(words);

		return parse;
	}

	/**
	 * parse use English CFG (PCFG)
	 * 
	 * @param sent
	 * @return
	 */
	public static Tree parseTreeCFG(String sent) {
		if (lp == null)
			lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

		lp.setOptionFlags(new String[] { "-maxLength", "80", "-retainTmpSubcategories" });

		List<HasWord> wlist = Utils.getWords(sent).get(0);
		Tree parse = (Tree) lp.apply(wlist);

		return parse;
	}

	/**
	 * 
	 * @param doc
	 *            may be contains many sentences
	 * @return list of tree by sentence
	 */
	public static List<Tree> parseDocTreeCFG(String doc) {
		if (lp == null)
			lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

		lp.setOptionFlags(new String[] { "-maxLength", "80", "-retainTmpSubcategories" });

		List<List<HasWord>> sentWords = Utils.getWords(doc);

		List<Tree> ret = new LinkedList<>();
		for (List<HasWord> wlist : sentWords) {
			Tree parse = (Tree) lp.apply(wlist);
			ret.add(parse);
		}

		return ret;
	}

	/***
	 * Write a row data to sheet (opened) (new row)
	 * 
	 * @param sheet
	 *            xlsx openned sheet
	 * @param rowNum
	 * @param row_data
	 * @return
	 */
	public static boolean writeRowXlsx(XSSFSheet sheet, int rowNum, Object[] row_data) {
		Row row = sheet.createRow(rowNum);
		int colNum = 0;
		for (Object field : row_data) {
			Cell cell = row.createCell(colNum++);
			if (field instanceof String) {
				cell.setCellValue((String) field);
			} else if (field instanceof Integer) {
				cell.setCellValue((Integer) field);
			} else if (field instanceof Float || field instanceof Double) {
				cell.setCellValue((double) field);
			}
		}

		return true;
	}

	/**
	 * 
	 * @param filePath
	 * @param sheetName
	 * @param row_datas
	 * @return
	 * @throws IOException
	 */
	public static boolean writeXlsx(String filePath, String sheetName, List<Object[]> row_datas) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet(sheetName);
		int rowNum = 0;

		for (Object[] row_data : row_datas) {
			Row row = sheet.createRow(rowNum++);
			int colNum = 0;
			for (Object field : row_data) {
				Cell cell = row.createCell(colNum++);
				if (field instanceof String) {
					cell.setCellValue((String) field);
				} else if (field instanceof Integer) {
					cell.setCellValue((Integer) field);
				} else if (field instanceof Float || field instanceof Double) {
					cell.setCellValue((double) field);
				}
			}
		}

		FileOutputStream outputStream = new FileOutputStream(filePath);
		workbook.write(outputStream);
		workbook.close();

		return true;
	}

	/**
	 * 
	 * @param tree
	 * @return
	 */
	public static ExtendLabeledScoredTreeNode recreateTree(Tree tree) {
		if (null == tree)
			return null;
		if (tree.isLeaf()) {
			return new ExtendLabeledScoredTreeNode(tree.label());
		}
		List<ExtendLabeledScoredTreeNode> childs = new LinkedList<>();
		for (Tree stree : tree.children()) {
			childs.add(recreateTree(stree));
		}
		return new ExtendLabeledScoredTreeNode(tree.label(), childs);

	}
}
