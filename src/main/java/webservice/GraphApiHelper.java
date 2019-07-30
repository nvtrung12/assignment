package webservice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import util.XlsxUtil;

/**
 * 
 * Implement some wire use function for graph with apis
 *
 */
public class GraphApiHelper {

	/**
	 * 
	 * @param fileToProcess
	 *            xlsx file with correct format
	 * @param filterInfo
	 *            map filterConcept => hops
	 * @return Map:
	 * @throws Exception
	 */
	public static Map<String, Object> filterGraph(String fileToProcess, Map<String, Integer> filterInfo)
			throws Exception {

		Map<String, Object> ret = new HashMap<>();

		Set<String> nodes = new HashSet<>();

		// load from file
		final List<List<String>> concepts = XlsxUtil.readXlsx(fileToProcess, Constants.ConceptSheetName);
		final List<List<String>> connections = XlsxUtil.readXlsx(fileToProcess, Constants.ConnectionSheetName);

		// static position of column, must dynamic to support update columns later
		int connectionConceptPos = Arrays.asList(Constants.CONNECT_HEADER).indexOf(Constants.COLL_SOURCE_OJBECT);
		int connectionSentIdPos = Arrays.asList(Constants.CONNECT_HEADER).indexOf(Constants.COLL_LINK_ID);

		List<List<String>> connectionFiltered = null;

		for (String filterConcept : filterInfo.keySet()) {
			// String filterConcept = null;
			int hops = filterInfo.get(filterConcept);
			System.out.println("do for: " + filterConcept + " -- " + hops);

			// filter
			// confirm match or substring hops 1, 1:concepts, 2: sent id
			connectionFiltered = connections.stream()
					.filter(o -> o.size() > 2 && o.get(connectionConceptPos).contains(filterConcept))
					.collect(Collectors.toList());
			// store all concept match, mostly only 1 element
			nodes.addAll(
					connectionFiltered.stream().map(o -> o.get(connectionConceptPos)).collect(Collectors.toList()));

			// expand by hops
			for (int nfilter = 2; nfilter <= hops; ++nfilter) {
				// sent id to expand
				List<String> expandSent = connections.stream().filter(o -> nodes.contains(o.get(connectionConceptPos)))
						.map(o -> o.get(connectionSentIdPos)).collect(Collectors.toList());
				// concept to expand
				List<String> expandConcept = connections.stream()
						.filter(o -> nodes.contains(o.get(connectionSentIdPos))).map(o -> o.get(connectionConceptPos))
						.collect(Collectors.toList());

				nodes.addAll(expandSent);
				nodes.addAll(expandConcept);
			}
		}

		// get connection filtered
		connectionFiltered = connections.stream()
				.filter(o -> nodes.contains(o.get(connectionConceptPos)) && nodes.contains(o.get(connectionSentIdPos)))
				.collect(Collectors.toList());

		// get concept filtered
		final int conceptNodeNamePos = fConceptPos.apply(Constants.NODE_NAME_HEADER_NAME);
		final int conceptObjextPos = fConceptPos.apply(Constants.OBJECT_INDEX_HEADER_NAME);

		List<List<String>> conceptsFilter = concepts.stream()
				.filter(o -> nodes.contains(o.get(conceptNodeNamePos)) || nodes.contains(o.get(conceptObjextPos)))
				.collect(Collectors.toList());

		ret.put(Constants.ConnectionSheetName, connectionFiltered);
		ret.put(Constants.ConceptSheetName, conceptsFilter);

		return ret;
	}

	public static final Function<String, Integer> fConceptPos = hname -> Arrays.asList(Constants.CONCEPT_HEADER)
			.indexOf(hname);
}
