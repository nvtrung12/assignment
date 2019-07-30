package parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.Tree;
import util.MultiReader;
import webservice.Constants;

/**
 * 
 * @since Mar 2018
 * 
 *        This is another way than {@link edu.assessment.parser.ParserV2
 *        version2} will consitence parser
 *
 */
public class ParserV4 implements IParser {

	final protected Logger logger = Logger.getLogger(this.getClass());

	/**
	 * For test only
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static void main(String[] args) throws Exception {
		String fileName = "tmp/1.pdf";
		fileName = "tmp/3.pdf";
		String phraseFile = "tmp/Phrase_File.docx";
		String stopwordFile = "tmp/stopword.txt";

		// output files
		String nounFile = "tmp/nouns.xlsx";
		String pairNounFile = "tmp/connections.xlsx";
		String conceptFile = "tmp/concepts.xlsx";

		IParser obj = new ParserV4(stopwordFile, phraseFile);
		// obj.processFile(fileName, conceptFile, nounFile, pairNounFile, null);
		// test2(args);
	}

	protected Set<String> stopWords = new HashSet<>();
	protected List<String> phrases = new LinkedList<>();

	final Set<String> label_to_get = new HashSet<>(Arrays.asList("NP"));
	final Set<String> nn_labels = new HashSet<>(Arrays.asList("NN", "NNS", "NNP"));
	final Set<String> v_labels = new HashSet<>(Arrays.asList("MD", "VB", "V", "VBN", "VBZ"));
	final Set<String> np_keep_labels = new HashSet<>(Arrays.asList("NN", "NNS", "NNP", "JJ"));

	public ParserV4() {

	}

	public ParserV4(String stopwordFile) {
		this.loadStopWord(stopwordFile);
	}

	public ParserV4(String stopwordFile, String phraseFile) {
		this.loadStopWord(stopwordFile);
		this.loadPhrase(phraseFile);
	}

	protected void loadStopWord(String stopwordFile) {
		List<String> stopWordList = new LinkedList<>();
		try {
			String[] stopWordsTmp = MultiReader.parseMulti(stopwordFile).trim().split("\\n");
			stopWordList = Arrays.asList(stopWordsTmp);
			stopWordList = stopWordList.stream().map(e -> e.trim()).collect(Collectors.toList());
		} catch (Exception e) {
			stopWordList = new LinkedList<>();
		}

		this.stopWords = new HashSet<>(stopWordList);
	}

	protected void loadPhrase(String phraseFile) {
		try {
			String str_Phrases = MultiReader.parseMulti(phraseFile);
			String[] phrasesTmp = str_Phrases.trim().toLowerCase().split("\\n");

			for (int i = 0; i < phrasesTmp.length; ++i)
				phrasesTmp[i] = phrasesTmp[i].trim();

			this.phrases = Arrays.asList(phrasesTmp);
			System.out.println(this.phrases);
		} catch (Exception e) {
		}

	}

	protected List<ExtendLabeledScoredTreeNode> visit(ExtendLabeledScoredTreeNode tree) {
		logger.debug("tree: " + tree.toString());
		List<ExtendLabeledScoredTreeNode> ret = new LinkedList<>();

		List<Tree> leaves = tree.getLeaves();
		List<ExtendLabeledScoredTreeNode> eleaves = leaves.stream().map(e -> (ExtendLabeledScoredTreeNode) e)
				.collect(Collectors.toList());

		String digitRE = "([+-,\\.]?\\d+)+";
		List<ExtendLabeledScoredTreeNode> interestedLeaves = eleaves.stream().filter(
				o -> !this.stopWords.contains(o.value().toLowerCase()) && !(o.value().toLowerCase().matches(digitRE)))
				.collect(Collectors.toList());

		// for N
		List<ExtendLabeledScoredTreeNode> nns = interestedLeaves.stream()
				.filter(e -> this.nn_labels.contains(e.parent().label().toString())).collect(Collectors.toList());

		logger.debug("nns: " + nns.toString() + "\ninterestedLeaves: " + interestedLeaves.toString());

		nns = nns.stream().map(e -> {
			if ("NP".equals("" + e.parent().parent().label())) {
				ExtendLabeledScoredTreeNode toRet = (ExtendLabeledScoredTreeNode) e.parent().parent();
				String s = treePresent(toRet);
				logger.debug("ss: " + s);

				// check if plural words, more than one word
				boolean isInPhraseListPlural = this.phrases.stream()
						.anyMatch(e1 -> e1.split("\\s+").length > 1 && s.toLowerCase().contains(e1));

				logger.debug(
						"t isInPhraseListPlural s e: " + isInPhraseListPlural + " -- " + s + " -- " + treePresent(e));
				// System.out.println("check " + s);
				if (isInPhraseListPlural) {

					// get all cases and sort by length, longest first
					List<String> xtmp = this.phrases.stream().filter(e1 -> s.toLowerCase().contains(e1))
							.collect(Collectors.toList());
					Collections.sort(xtmp, new Comparator<String>() {
						@Override
						public int compare(String o1, String o2) {
							String[] ar1 = o1.split("\\s+");
							String[] ar2 = o2.split("\\s+");
							return ar1.length < ar2.length ? 1 : ar1.length > ar2.length ? -1 : 0;
						}
					});

					logger.debug("xtmp: " + xtmp);

					// if xtmp do not contains e -> return e only
					// else, find any sub-part that include e (longest)

					// TODO filter only contains word, else return e
					boolean isIn = xtmp.stream().anyMatch(e2 -> (" " + e2 + " ").contains(e.value().toLowerCase()));
					if (!isIn)
						return e;

					String longest_phrase = xtmp.get(0); // only care longest phrase
					String[] longest_phrase_arr = longest_phrase.split("\\s+");
					Set<String> longest_phrase_set = new HashSet<>(Arrays.asList(longest_phrase_arr));

					// TODO

					List<Tree> inOnly = toRet.getChildrenAsList().stream()
							.filter(e1 -> longest_phrase_set.contains(e1.children()[0].value().toLowerCase()))
							.collect(Collectors.toList());
					toRet.setChildren(inOnly);
					if (true)
						return toRet;

					List<Tree> nnonly = toRet.getChildrenAsList().stream()
							.filter(e1 -> this.nn_labels.contains(e1.label().toString().toLowerCase()))
							.collect(Collectors.toList());
					String nnonlystr = String.join(" ", nnonly.stream().map(e2 -> {
						String str = String.join(" ", e2.getChildrenAsList().stream()
								.map(e3 -> e3.value().toLowerCase()).collect(Collectors.toList()));
						return str;
					}).collect(Collectors.toList()));

					logger.debug("NN only: " + nnonly);

					boolean isInPhraseListNNOnly = this.phrases.stream()
							.anyMatch(e1 -> nnonlystr.toLowerCase().contains(e1));
					if (isInPhraseListNNOnly) {
						// keep on NN..
						List<Tree> filterChild = toRet.getChildrenAsList().stream()
								.filter(e1 -> this.nn_labels.contains(e1.label().toString()))
								.collect(Collectors.toList());
						toRet.setChildren(filterChild);

					} else {
						// keep NN and JJ
						List<Tree> filterChild = toRet.getChildrenAsList().stream()
								.filter(e1 -> this.np_keep_labels.contains(e1.label().toString()))
								.collect(Collectors.toList());
						toRet.setChildren(filterChild);
					}
					return toRet;
				} else {
					// keep single word
					return (ExtendLabeledScoredTreeNode) e.parent();
				}
			}
			return (ExtendLabeledScoredTreeNode) e.parent();
		}).collect(Collectors.toList());

		ret.addAll(nns);

		for (ExtendLabeledScoredTreeNode o : ret) {
			for (Tree l : o.getLeaves())
				((ExtendLabeledScoredTreeNode) l).setMark(true);
		}

		// add all other
		List<ExtendLabeledScoredTreeNode> vs1 = interestedLeaves.stream()
				.filter(e -> !e.isMark() && !".".equals(e.toString())).collect(Collectors.toList());
		ret.addAll(vs1);

		return ret;
	}

	protected static String getSentence(String sentId, Map<String, List<TaggedWord>> sentenceMap) {
		List<TaggedWord> a = sentenceMap.get(sentId);
		List<String> tokens = a.stream().map(e -> e.value()).collect(Collectors.toList());

		return String.join(" ", tokens);
	}

	protected String treePresent(ExtendLabeledScoredTreeNode etree) {
		List<String> arr = etree.getLeaves().stream().map(e -> e.value()).collect(Collectors.toList());
		List<String> nonStopwordArr = arr.stream().filter(e -> !this.stopWords.contains(e))
				.collect(Collectors.toList());
		return String.join(" ", nonStopwordArr);
	}

	@Override
	public Map<String, Object> processFile(String fileName, String sDocID) throws Exception {
		Map<String, Object> fret = new HashMap<>();
		Map<String, Object> metaInfo = new HashMap<>();

		String[] tmpArr = fileName.split("[/\\\\]+");
		String docID = tmpArr[tmpArr.length - 1];
		if (null != sDocID)
			docID = sDocID;

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
		final Set<String> phraseWords = phrasesMap.keySet();

		System.out.println(String.join(",", phrases));

		String text = MultiReader.parseMulti(fileName);
		if (null == text)
			throw new Exception("Please enter the main file");

		String[] content_by_pages = text.split(MultiReader.newPage);
		metaInfo.put("numberOfPage", content_by_pages.length);

		System.out.println("start load models");

		System.out.println("starting run");
		long millis = System.currentTimeMillis();

		List<ExtendIndexedWord> concepts = new LinkedList<>();
		Map<String, List<TaggedWord>> sentenceMap = new HashMap<>();
		// collect all occursion of phrases

		int wordCount = 0;
		int sentenceCount = 0;
		Set<String> uniqueWord = new HashSet<>();

		// extract noun phrase => concepts => store to concepts
		for (int page_number = 0; page_number < content_by_pages.length; ++page_number) {

			System.out.println("page " + page_number);
			String page_content = content_by_pages[page_number];
			page_content = page_content.toLowerCase();

			// list of list word (each for one sentence)
			List<List<TaggedWord>> words = Utils.getTag(page_content);

			int sentence_count = 0;

			for (List<TaggedWord> wordOneSentence : words) {
				++sentence_count;
				wordCount += wordOneSentence.size();
				uniqueWord.addAll(wordOneSentence.stream().map(o -> o.value()).collect(Collectors.toList()));

				// check if sentence contain any word in phrase interested, if no then go to
				// next sentence
				boolean isContains = wordOneSentence.stream()
						.anyMatch(o -> phraseWords.contains(o.value().toLowerCase()));
				if (!isContains)
					continue;

				Tree tree = Utils.parseTreeCFG(wordOneSentence);
				ExtendLabeledScoredTreeNode etree = Utils.recreateTree(tree);
				List<ExtendLabeledScoredTreeNode> ret = this.visit(etree);

				System.out.println("ret");
				System.out.println(ret);

				String sentId = String.format("%s.%d.%d", docID, page_number + 1, sentence_count);
				sentenceMap.put(sentId, wordOneSentence);

				System.out.println(tree);

				// store each tree with docID.page_number.sentence_count.phrase_position

				for (int i = 0; i < ret.size(); ++i) {
					ExtendLabeledScoredTreeNode t = ret.get(i);

					ExtendIndexedWord iw = new ExtendIndexedWord();

					Set<String> setWord = new HashSet<>(
							t.getLeaves().stream().map(e -> e.value()).collect(Collectors.toList()));
					// System.out.println("setWord");
					// System.out.println(setWord);

					iw.setPhrase(this.treePresent(t));

					List<TaggedWord> lstTaggedWord = wordOneSentence.stream().filter(o -> setWord.contains(o.value()))
							.collect(Collectors.toList());

					// position of word in original sentence
					int pos = 1 + wordOneSentence.indexOf(lstTaggedWord.get(0));

					// pos is phrase_position (first word occur)
					iw.setDocID(sentId);
					iw.setPhraseIndex(String.format("%s.%d", iw.docID(), pos));

					iw.setPseudoPosition(pos);
					String pval = t.parent().label().value();
					String srole = pval.equals("S") || pval.equals("NP") ? "nsubj" : pval.equals("VP") ? "nobj" : "NA";
					iw.setSrole(srole);

					concepts.add(iw);
					// sentence contains many phrase
				}
			}

			sentenceCount += sentence_count;
		}

		metaInfo.put("numberOfWord", wordCount);
		metaInfo.put("numberOfSentence", sentenceCount);
		metaInfo.put("uniqueWord", uniqueWord.size());

		// remove duplicated concepts
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

		// save concepts to file
		List<Object[]> lstConcept = new LinkedList<>();
		lstConcept.add(Constants.CONCEPT_HEADER);

		// for (ExtendIndexedWord o : concepts) {
		for (String ph : mmap_concepts.keySet()) {
			ExtendIndexedWord o = mmap_concepts.get(ph);
			List<TaggedWord> a = sentenceMap.get(o.docID());
			List<String> tokens = a.stream().map(e -> e.value()).collect(Collectors.toList());

			Object[] row_data = { o.docID(), o.getPhraseIndex(), o.getPhrase(), o.getSrole(), mCountConceptInt.get(ph),
					String.join(" ", tokens) };

			lstConcept.add(row_data);
		}
		fret.put("concepts", lstConcept);
		// ----- end of write concept file ----

		// make connections
		System.out.println("make connection file");

		List<Object[]> lstConnection = new LinkedList<>();
		lstConnection.add(Constants.CONNECT_HEADER);

		Map<String, Integer> degreeOfSentenceNodes = new HashMap<>();
		Map<String, Integer> degreeOfConceptNodes = new HashMap<>();

		for (ExtendIndexedWord enoun1 : concepts) {

			Object[] row_data = { enoun1.getPhraseIndex(), enoun1.getPhrase(), enoun1.docID() };

			Integer old1 = degreeOfSentenceNodes.get(enoun1.docID());
			degreeOfSentenceNodes.put(enoun1.docID(), 1 + (null != old1 ? old1 : 0));

			Integer old = degreeOfConceptNodes.get(enoun1.getPhrase());
			degreeOfConceptNodes.put(enoun1.getPhrase(), 1 + (null != old ? old : 0));

			lstConnection.add(row_data);

		}
		fret.put("connection", lstConnection);

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
		fret.put("metaInfo", metaInfo);
		return fret;
	}
}
