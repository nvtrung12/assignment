package webservice;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;

import graph.ContentJSONGraph;
import graph.IGraph;
import graph.Utils;
import util.XlsxUtil;

/**
 * @deprecated not use any more, use api /api/filter instead <br/>
 * 			remove next version
 */
@WebServlet(urlPatterns = { "/concept_filter", "/filter" })
@MultipartConfig
public class ConceptFilter extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		String appPath = this.getServletContext().getRealPath("/");
		String uploadFolder = String.format("%s%s%s", appPath, File.separator, Constants.UPLOAD_FOLDER);
		String downloadFolder = String.format("%s%s%s", appPath, File.separator, Constants.DOWNLOAD_FOLDER);
		String ccName = String.format("filter_concepts.%s.xlsx", uuid);
		String cpath = String.format("%s%s%s", downloadFolder, File.separator, ccName);

		String st = "null";
		String fileToProcess = null;
		int numhops = 1;
		try {
			List<Pair<String, String>> files = null;

			fileToProcess = request.getParameter("uploaded_file");
			if (fileToProcess == null || fileToProcess.equals("")) {
				try {
					files = webservice.api.Utils.storeFiles(request, resp);
					if (files.size() == 0)
						throw new Exception();
				} catch (Exception e) {
					throw new Exception("Please upload file to filter");
				}

				fileToProcess = files.get(0).getValue();
			}

			String fileToProcessPath = String.format("%s%s%s", uploadFolder, File.separator, fileToProcess);

			String filterConcept = request.getParameter("filter_concept");

			if (null == filterConcept)
				filterConcept = "";
			// throw new Exception("Please enter a filter concept");

			try {
				numhops = Integer.parseInt(request.getParameter("numhops"));
			} catch (Exception e) {
				numhops = 1;
			}
			numhops++;

			// disable default load full graph if no info
			if ("".equals(filterConcept)) {
				// request.getRequestDispatcher("filter.jsp").forward(request, resp);
				// return;
			}

			Map<String, Integer> info = new HashMap<>();
			info.put(filterConcept, numhops);
			Map<String, Object> ret = filter_graph(fileToProcessPath, info);
			st = (String) ret.get(STR_GRAPH_CT);

			// write
			List<List<Object>> concepts = (List<List<Object>>) ret.get(Constants.ConceptSheetName);
			List<List<Object>> connection = (List<List<Object>>) ret.get(Constants.ConnectionSheetName);
			concepts.add(0, Arrays.asList(Constants.CONCEPT_HEADER));
			connection.add(0, Arrays.asList(Constants.CONNECT_HEADER));

			XlsxUtil.writeXlsx(cpath, Constants.ConceptSheetName, concepts);
			XlsxUtil.writeRowsXlsxAppend(cpath, Constants.ConnectionSheetName, connection);
			XlsxUtil.writeRowsXlsxAppend(cpath, Constants.META_SHEET_NAME, new LinkedList<>());

			request.setAttribute("default_id_values", Json.createObjectBuilder().add("numhops", numhops - 1)
					.add("filter_concept", filterConcept).add("uploaded_file", fileToProcess).build().toString());
			request.setAttribute("message", String.format("Process filter for %s done!", fileToProcess));
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("message", e.getMessage());
		}

		request.setAttribute("gr_data", st);

		request.setAttribute("conceptFile", ccName);

		request.getRequestDispatcher("filter.jsp").forward(request, resp);
	}

	/**
	 * @deprecated separated to GraphApiHelper.filterGraph for use in more context,
	 *             please replace it
	 * @param fileToProcess
	 * @param filterInfo
	 *            map filterConcept => hops
	 * @return Map:
	 * @throws Exception
	 */
	@Deprecated
	protected Map<String, Object> filter_graph(String fileToProcess, Map<String, Integer> filterInfo) throws Exception {
		// String filterConcept = null;
		// int hops = 0;

		Map<String, Object> ret = new HashMap<>();

		Set<String> nodes = new HashSet<>();

		// load from old file
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

		int connectCLPost = Arrays.asList(Constants.CONNECT_HEADER).indexOf(Constants.COLL_SOURCE_OBJECT_INDEX); // 0
		// concept filter, store id of concept (#1)
		Set<String> conceptsFilterSet = new HashSet<>(
				connectionFiltered.stream().map(o -> o.get(connectCLPost)).collect(Collectors.toList()));

		//
		int conceptObjectIndexPos = Arrays.asList(Constants.CONCEPT_HEADER).indexOf(Constants.OBJECT_INDEX_HEADER_NAME); // 1

		// get #2
		List<List<String>> concepts_filter = concepts.stream()
				.filter(o -> conceptsFilterSet.contains(o.get(conceptObjectIndexPos))).collect(Collectors.toList());

		List<Object[]> subGraph = new LinkedList<>();
		for (List<String> lst : connectionFiltered)
			subGraph.add(lst.toArray());

		Map<String, String> sentMap = new HashMap<>();
		for (List<String> lst : concepts) {
			sentMap.put(Utils.extractSentenceIndex(lst.get(fConceptPos.apply(Constants.NODE_ID_HEADER_NAME))),
					lst.get(fConceptPos.apply(Constants.DISPLAY_NAME_HEADER_NAME)));
		}

		Map<String, String> subSentenceMap = new HashMap<>();
		for (String k : nodes)
			if (null != sentMap.get(k))
				subSentenceMap.put(k, sentMap.get(k));

		IGraph graph = new ContentJSONGraph(subGraph, sentMap, nodes, concepts);

		String st = graph.getJsonVisjsFormat();

		ret.put(STR_GRAPH_CT, st);
		ret.put(Constants.ConnectionSheetName, connectionFiltered);
		ret.put(Constants.ConceptSheetName, concepts_filter);

		return ret;
	}

	protected final Function<String, Integer> fConceptPos = hname -> Arrays.asList(Constants.CONCEPT_HEADER)
			.indexOf(hname);

	protected final String STR_GRAPH_CT = "graphCT";
}
