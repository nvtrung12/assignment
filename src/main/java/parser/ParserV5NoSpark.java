package parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.Tree;
import util.MultiReader;
import util.XlsxUtil;
import webservice.Constants;

public class ParserV5NoSpark extends ParserV4 implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HttpSession session = null;
	private Map<String, String> equivalenMap = new HashedMap<>();
	private  URL dictFile = this.getClass().getClassLoader().getResource("/defaults/words_alpha.txt");//("defaults/words_alpha.txt");
	SpellChecker spellCheck = null;
	public ParserV5NoSpark() {

	}

	public ParserV5NoSpark(String stopwordFile) {
		this.loadStopWord(stopwordFile);
	}

	public ParserV5NoSpark(String stopwordFile, String phraseFile) {
		this.loadStopWord(stopwordFile); // load list of Stopword
		this.loadPhrase(phraseFile);
	}

	public ParserV5NoSpark(String stopwordFile, String phraseFile, HttpSession session) {
		this.loadStopWord(stopwordFile); // load list of Stopword
		this.loadPhrase(phraseFile);
		this.session = session;
		
	}

	/**
	 * A xlsx file
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public void setEquivalenFile(String fileName) throws Exception {
		FileInputStream inputStream = new FileInputStream(new File(fileName));
		this.setEquivalenFile(inputStream);
	}

	/**
	 * A xlsx file
	 * 
	 * @param inputStream
	 * @throws Exception
	 */
	public void setEquivalenFile(InputStream inputStream) throws Exception {
		List<List<String>> tmp = XlsxUtil.readXlsx(inputStream, "sheet1");
		for (List<String> row : tmp) {
			if (row.size() >= 2) {
				this.equivalenMap.put(row.get(1).toLowerCase(), row.get(0).toLowerCase());
			}
		}
		System.out.println("test...." + this.equivalenMap);
	}

	/*
	 * 
	 * return
	 */
	public Map<String, Object> processFile(String fileName, String sDocID) throws Exception {
		Map<String, Object> fret = new HashMap<>();
		Map<String, Object> metaInfo = new HashMap<>();

		String[] tmpArr = fileName.split("[/\\\\]+");
		String docID = null != sDocID ? sDocID : tmpArr[tmpArr.length - 1];

		// use map to quick access one word in phrases, use when search in node info
		HashedMap<String, List<Integer>> phrasesMap = new HashedMap<String, List<Integer>>();
		for (int i = 0; i < phrases.size(); ++i) {
			phrases.set(i, phrases.get(i).trim());
			String[] sarr = phrases.get(i).split("\\s+");
			for (String word : sarr) {
				List<Integer> tmp = phrasesMap.get(word);
				if (null == tmp)
					tmp = new LinkedList<Integer>();

				tmp.add(i);
				phrasesMap.put(word, tmp);
			}
		}
		final Set<String> phraseWords = new HashSet<>(phrasesMap.keySet());

		System.out.println(String.join(",", phrases));

		// read content of pdf file
		String text = MultiReader.parseMulti(fileName);
		if (null == text)
			throw new Exception("Please enter the main file");

		// get Content by page
		String[] contentByPages = text.split(MultiReader.newPage);
		metaInfo.put("numberOfPage", contentByPages.length);

		System.out.println("start load models");

		System.out.println("starting run");
		long millis = System.currentTimeMillis();

		Map<String, String> sentenceMap = new HashMap<>();
		// collect all occursion of phrases

		List<Map<String, Object>> lstSentence = new LinkedList<>();

		// sentence split for easy process later
		System.out.println("start ...");

		// parallel begin
		// get sentences from pages
		List<List<String>> out2 = Arrays.asList(contentByPages).stream().parallel().map(o -> {
			Reader reader = new StringReader((String) o);
			DocumentPreprocessor dp = new DocumentPreprocessor(reader);
			List<String> sentenceList = new ArrayList<String>();

			for (List sentence : dp) {
				// SentenceUtils not Sentence
				String sentenceString = SentenceUtils.listToString(sentence);
				sentenceList.add(sentenceString);
			}
			return sentenceList;
		}).collect(Collectors.toList());
		// parallel end

		int sentenceCount = 0;
		int pageNumber = 0;
		//
		for (List<String> onePageCt : out2) { // get each page
			pageNumber++;

			int sentenceNumber = 1;
			for (String oneSent : onePageCt) { // get each sentence of page
				// for one sentence
				String sentID = String.format("%s:x.x.x:%d.%d", docID, pageNumber, sentenceNumber);
				sentenceMap.put(String.format("%s.0", sentID), oneSent);

				Map<String, Object> one = new HashMap<>();
				one.put(STR_WORD, oneSent);
				one.put(STR_SENT_ID, sentID);
				// one.put(STR_PHRASES, phraseWords);

				lstSentence.add(one);
				++sentenceNumber;
				sentenceCount += sentenceNumber;
			}
		}
		
		System.out.println("Done get pages, spark begin");

		// use to load number of sentence processed
		final AtomicInteger loader = new AtomicInteger();
		if (null != this.session)
			if(lstSentence != null)
			this.session.setAttribute("totalSentence", lstSentence.size());

		// run parallel, this will be longest process, so track by progress
		List<Map<String, Object>> out = lstSentence.stream().parallel().map(object -> {

			Map<String, Object> input = (Map<String, Object>) object;
			String wordOneSentence = (String) input.get(STR_WORD); // get content of sentence
			String sentId = (String) input.get(STR_SENT_ID); // get id of sentence

			// final Set<String> phraseWords1 = (Set<String>) input.get(STR_PHRASES);

			List<ExtendIndexedWord> concepts = new LinkedList<>();
			Set<String> uniqueWord = new HashSet<>();
			int wordCount = 0;

			List<String> words1 = Arrays.asList(wordOneSentence.split("\\s+"));

			wordCount += words1.size();
			uniqueWord.addAll(words1);

			// check if sentence contains any word in phrase interested, if no then go to
			// next sentence
			boolean isContains = words1.stream().anyMatch(o -> phraseWords.contains(o.toLowerCase()));
			isContains = true;
			if (isContains) {
				// continue;

				//// parse a sentence into a consistent tree, which has subject, verb,
				//// object,...
				Tree tree = Utils.parseTreeCFG(wordOneSentence);
				//
				ExtendLabeledScoredTreeNode etree = Utils.recreateTree(tree);
				List<ExtendLabeledScoredTreeNode> ret = this.visit(etree);

				// store each tree with docID.page_number.sentence_count.phrase_position
				for (int i = 0; i < ret.size(); ++i) {
					try {
						ExtendLabeledScoredTreeNode t = ret.get(i);

						ExtendIndexedWord iw = new ExtendIndexedWord();

						Set<String> setWord = new HashSet<>(
								t.getLeaves().stream().map(e -> e.value()).collect(Collectors.toList()));
						String treeStr = this.treePresent(t).toLowerCase();

						// use equivalen word if have in dict, map to full phrase if have
						treeStr = this.equivalenMap.get(treeStr) == null ? treeStr : this.equivalenMap.get(treeStr);

						iw.setPhrase(treeStr);

						List<String> lstTaggedWord = words1.stream().filter(o -> setWord.contains(o))
								.collect(Collectors.toList());

						// position of word in original sentence
						int pos = 1 + words1.indexOf(lstTaggedWord.get(0));

						// pos is phrase_position (first word occur)
						iw.setDocID(String.format("%s.0", sentId));
						iw.setPhraseIndex(String.format("%s.%d", sentId, pos));

						iw.setPseudoPosition(pos);
						String pval = t.parent().label().value();
						String srole = pval.equals("S") || pval.equals("NP") ? "nsubj"
								: pval.equals("VP") ? "nobj" : "NA";
						iw.setSrole(srole);

						concepts.add(iw);
						// sentence contains many phrase
					} catch (Exception e) {
						System.out.println(e);
						System.out.println(ret.get(i));
					}
				}
			}

			Map<String, Object> ret1 = new HashMap<>();
			ret1.put(STR_CONCEPT, concepts);
			ret1.put(STR_WORD_COUNT, wordCount);
			ret1.put(STR_UNIQUE_WORD, uniqueWord);

			return ret1;
		}).peek(stat -> {
			int tmp = loader.incrementAndGet();
			if (null != this.session)
				this.session.setAttribute("numProcessed", tmp);

		}).collect(Collectors.toList());

		System.out.println("Done spark");
		System.out.println(out);

		// collect info
		Integer wordCount = out.stream().map(o -> (Integer) o.get(STR_WORD_COUNT)).reduce((a, b) -> a + b).get();

		Set<String> uniqueWord = out.stream().map(o -> (Set<String>) o.get(STR_UNIQUE_WORD)).reduce((a, b) -> {
			a.addAll(b);
			return a;
		}).get();

		// collect info
		metaInfo.put("numberOfWord", wordCount);
		metaInfo.put("numberOfSentence", sentenceCount);
		metaInfo.put("uniqueWord", uniqueWord.size());

		// remove duplicated concepts
		List<ExtendIndexedWord> concepts = out.stream().map(o -> (List<ExtendIndexedWord>) o.get(STR_CONCEPT))
				.reduce((a, b) -> {
					a.addAll(b);
					return a;
				}).get();

		// finish all parallel task

		Set<ExtendIndexedWord> nset = new HashSet<ExtendIndexedWord>(concepts);
		concepts = new LinkedList<>();
		concepts.addAll(nset);

		metaInfo.put("uniqueDomainConcept", concepts.size());
		metaInfo.put("numberOfSentenceNodes",
				new HashSet<>(concepts.stream().map(o -> o.docID()).collect(Collectors.toList())).size());

		// count frequency
		Map<String, ExtendIndexedWord> mmap_concepts = new HashedMap<>();
		Map<String, Integer> mCountConceptInt = new HashMap<>();
		for (ExtendIndexedWord enoun : concepts) {
			String key = enoun.getPhrase().toLowerCase();
			ExtendIndexedWord tmp = mmap_concepts.get(key);
			if (tmp == null) {
				mmap_concepts.put(key, enoun);
				mCountConceptInt.put(key, 1);
			} else {
				mCountConceptInt.put(key, 1 + mCountConceptInt.get(key));
			}
		}

		// ----- save concepts to file -----
		List<Object[]> lstConcept = new LinkedList<>();
		lstConcept.add(Constants.CONCEPT_HEADER);
		
		System.out.println("create dictionary");
		File dict = new File(dictFile.toURI());
		if(!dict.exists())
		{
			System.out.println("dont have dict");
		}
		else {
			SpellDictionary dictionary = new SpellDictionaryHashMap(dict);
			spellCheck = new SpellChecker(dictionary);
		}
		// for (ExtendIndexedWord o : concepts) {
		for (String ph : mmap_concepts.keySet()) {
			ExtendIndexedWord o = mmap_concepts.get(ph);
			String a = sentenceMap.get(o.docID());
			
			String str= o.getPhrase();	
			for(int i =0; i< str.length(); i++)
			{
				Character w = str.charAt(i);
				if(!Character.isLetter(w) && w != ' ')
					{
						str= "";	
						break;
					}
			}
			
			if (StringUtils.isEmpty(str) || str.equals("NULL") || StringUtils.isNumeric(str))
				continue;
			
			//check exist in pharse list
			boolean isPharse = false;
			for (String word : phraseWords) {
				if(word.equals(str)) {
					isPharse = true;
					break;
				}
			}
			//uppercase string to compare with dictionary wordalpha
			str = str.substring(0, 1).toUpperCase() + str.substring(1);
			
			// checkspelling = 1 <-->invalid word
			StringWordTokenizer x = new StringWordTokenizer(str);
				if (spellCheck.checkSpelling(x) == 1 && isPharse == false)
					{
						str="";
						continue;
					}
			//lowercase string	
				str = str.substring(0, 1).toLowerCase() + str.substring(1);
			
			String nodeType = o.getPhrase() == "NULL" ? "Collection" : "Concept";
			Object[] row_data = { o.getPhraseIndex(), o.getPhraseIndex(), str, "", "", nodeType, o.getSrole(),
					String.join(" ", a), "", mCountConceptInt.get(ph), "box", "blue", "green" };

			lstConcept.add(row_data);
		}

		// list of sentence nodes
		List<Object[]> lstSentenceNode = new LinkedList<>();
		for (String sentID : sentenceMap.keySet()) {
			Object[] rowData = { sentID, sentID, "NULL", "", "", "Collection", "Collection.Sentence",
					sentenceMap.get(sentID), "", 1 };
			lstSentenceNode.add(rowData);
		}

		lstConcept.addAll(lstSentenceNode);

		fret.put(Constants.ConceptSheetName, lstConcept);
		// ----- end of write concept file ----

		// ------ make connections
		System.out.println("make connection file");

		List<Object[]> lstConnection = new LinkedList<>();
		lstConnection.add(Constants.COLLECTION_HEADER);

		Map<String, Integer> degreeOfSentenceNodes = new HashMap<>();
		Map<String, Integer> degreeOfConceptNodes = new HashMap<>();
		Map<String, String> mapPhraseIndex = new HashedMap<>();

		for (ExtendIndexedWord enoun1 : concepts) {
			String key = enoun1.getPhrase();
			String value = enoun1.getPhraseIndex();
			if(!mapPhraseIndex.containsKey(key)) {
				mapPhraseIndex.put(key, value);
			}else {
				value = mapPhraseIndex.get(key);
			}
			Object[] row_data = { enoun1.docID(), value, key, "", enoun1.docID(),
					"NULL", "", "NULL" };

			Integer old1 = degreeOfSentenceNodes.get(enoun1.docID());
			degreeOfSentenceNodes.put(enoun1.docID(), 1 + (null != old1 ? old1 : 0));

			Integer old = degreeOfConceptNodes.get(enoun1.getPhrase());
			degreeOfConceptNodes.put(enoun1.getPhrase(), 1 + (null != old ? old : 0));

			lstConnection.add(row_data);

		}
		fret.put(Constants.ConnectionSheetName, lstConnection);
		// --------end of write connection

		metaInfo.put("numberOfSentencesToConceptLinks", lstConnection.size() - 1);
		metaInfo.put("sentenceDegreeDistribution", degreeOfSentenceNodes);
		metaInfo.put("conceptDegreeDistribution", degreeOfConceptNodes);

		OptionalDouble val = degreeOfSentenceNodes.keySet().stream().mapToDouble(o -> degreeOfSentenceNodes.get(o))
				.average();
		metaInfo.put("sentenceDegreeDistributionAverage", val.getAsDouble());

		val = degreeOfConceptNodes.keySet().stream().mapToDouble(o -> degreeOfConceptNodes.get(o)).average();
		metaInfo.put("conceptDegreeDistributionAverage", val.getAsDouble());

		long ti = System.currentTimeMillis() - millis;
		System.out.println("Run time: " + ti);
		fret.put("runtime", ti);

		fret.put("sentencesMap", sentenceMap);
		fret.put(Constants.META_SHEET_NAME, metaInfo);
		return fret;
	}

	protected final String STR_WORD_COUNT = "wordCount";
	protected final String STR_CONCEPT = "concept";
	protected final String STR_UNIQUE_WORD = "unique_word";
	protected final String STR_SENTENCE_MAP = "sentence_mapping";

	final String STR_WORD = "words";
	final String STR_DOC_ID = "docID";
	final String STR_PAGE_NUMBER = "pageNumber";
	final String STR_SENTENCE_NUMBER = "sentenceNumber";
	final String STR_PHRASES = "phraseWords";
	final String STR_SENT_ID = "sentID";
}
