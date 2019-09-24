package webservice.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import graph.ContentJSONGraphV2;
import graph.IGraph;
import util.XlsxUtil;
import webservice.Constants;

/**
 * 
 * 
 * api<br/>
 * from a file (uploaded or in downloads, uploads folder) make and return a
 * graph (json) to show in browser
 * 
 * input: fileName (related path), type (default or name of class) of graph
 *
 */

@WebServlet(urlPatterns = { "/api/graph", "/api/v1.1/graph" })
@MultipartConfig
public class GraphApi extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final Logger logger = Logger.getLogger(this.getClass());
	private float threshold = 0.8f;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		buildGraph(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		buildGraph(request, resp);
	}

	private void buildGraph(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("application/json");
		String message = "";

		String appPath = request.getServletContext().getRealPath("/");
		String downloadFolder = String.format("%s%s", appPath, Constants.DOWNLOAD_FOLDER);

		// get from query
		String graphType = request.getParameter("graphType");
		logger.debug("graphType: " + graphType);

		// get threshold from query if have
		try {
			this.threshold = Float.parseFloat(request.getParameter("threshold"));
		} catch (Exception e) {
			this.threshold = 0.8f;
		}

		// convert to full path
		//downloadFolder = String.format("%s%s%s", downloadFolder, File.separator, fileName);

		try {
			//TODO
			String fileName = request.getParameter("fileName");
			String sheetName = Constants.ConnectionSheetName;
			logger.debug("fileName: " + fileName);
			boolean isCall = false;
			String [] arrayHeader = Constants.CONNECT_HEADER;
			if (fileName.contains(";CALLink")) {
				isCall = true;
				String [] lstString = fileName.split(";");
				fileName = lstString[0];
				sheetName = lstString[1];
				arrayHeader = Constants.HEADER_CALLINK;
			}
			final String[] arrayData = arrayHeader;
			List<List<String>> connections = XlsxUtil.readFolderXlsx(downloadFolder, sheetName);
			List<List<String>> concepts = XlsxUtil.readFolderXlsx(downloadFolder, Constants.ConceptSheetName);

			// remove header
			connections.remove(0);
			concepts.remove(0);

			final Function<String, Integer> fConceptPos = headerName -> Arrays.asList(Constants.CONCEPT_HEADER).indexOf(headerName);
			final Function<String, Integer> fConnectionPos = headerName -> Arrays.asList(arrayData)
					.indexOf(headerName);

			// keep only number of concept nodes if have in params
			String keepNodesStr = request.getParameter("keepNodes");
			logger.debug("keepNodesStr: " + keepNodesStr);
			if (null == keepNodesStr || "default".equals(keepNodesStr) || "".equals(keepNodesStr)) {
				// do nothing
			} else {

				int xpos = fConceptPos.apply(Constants.NODE_NAME_HEADER_NAME);
				int nodeIdpos = fConceptPos.apply(Constants.NODE_ID_HEADER_NAME);

				int ypos = fConnectionPos.apply(Constants.COLL_SOURCE_OJBECT);
				int linkidPos = isCall ? fConnectionPos.apply(Constants.SOURCE_OBJECT_INDEX) : fConnectionPos.apply(Constants.COLL_LINK_ID);
				int sourobjPos = fConnectionPos.apply(Constants.COLL_SOURCE_OJBECT);

				int keepNodes = Integer.parseInt(keepNodesStr);
				message = keepNodes > concepts.size() ? "" : "For the large graph, we keep only 200 nodes!";

				// new way to filter
				//get if NodeName = null -> get (0) , k thì get()
				List<String> nodes = concepts.stream()
						.map(o -> o.get(xpos).equals("NULL") ? o.get(nodeIdpos) : o.get(xpos))
						.collect(Collectors.toList());
				List<List<String>> edges = connections.stream()
						.map(o -> Arrays.asList(o.get(linkidPos), o.get(sourobjPos))).collect(Collectors.toList());
				List<String> keepNodesLst = graph.Utils.graphCutByDegree(nodes, edges, keepNodes);
				Set nSet = new HashSet<>(keepNodesLst);

				concepts = concepts.stream().filter(o -> nSet.contains(o.get(xpos)) || nSet.contains(o.get(nodeIdpos)))
						.collect(Collectors.toList());
				connections = connections.stream()
						.filter(o -> nSet.contains(o.get(linkidPos)) || nSet.contains(o.get(sourobjPos)))
						.collect(Collectors.toList());

				// keep only keepNodes in concepts
				List<List<String>> sentenceNodes = concepts.stream().filter(o -> o.get(xpos).equals("NULL"))
						.collect(Collectors.toList());

				// only do if keepNodes greater than current number concept node else do nothing
				if (false && keepNodes <= concepts.size() - sentenceNodes.size()) {

					// now concepts only keep concepts not sentence nodes
					concepts = concepts.stream().filter(o -> !o.get(xpos).equals("NULL")).collect(Collectors.toList());
					while (concepts.size() > keepNodes)
						concepts.remove(keepNodes);

					Set<String> nodeSet = concepts.stream().map(o -> o.get(xpos)).collect(Collectors.toSet());

					// filter to keep connection include above concepts
					connections = connections.stream().filter(o -> nodeSet.contains(o.get(ypos)))
							.collect(Collectors.toList());

					Set<String> sentCollect = new HashSet<>(
							connections.stream().map(o -> o.get(linkidPos)).collect(Collectors.toList()));

					// TODO filter sentenceNodes that not have connection
					sentenceNodes = sentenceNodes.stream().filter(o -> sentCollect.contains(o.get(nodeIdpos)))
							.collect(Collectors.toList());

					// careful check duplicate here sentence node
					concepts.addAll(sentenceNodes);
				}

				logger.debug("connect");
				logger.debug(connections);
			}

			IGraph graph = null;

			if (null == graphType || "default".equals(graphType) || "".equals(graphType)) {
				graph = new ContentJSONGraphV2(isCall,concepts, connections);
			} else if ("mergeSentence".equals(graphType)) {
				graph = new ContentJSONGraphV2(concepts, connections, threshold);
			} else { // default
				graph = new ContentJSONGraphV2(isCall ,concepts, connections);
			}

			String graphjs = graph.getJsonVisjsFormat();

			String jsonReturn = Json.createObjectBuilder().add("status", 0).add("message", message)
					.add("fileName", fileName).add("grData", graphjs).build().toString();

			logger.debug("MESSAGE " + jsonReturn);
			response.getWriter().println(jsonReturn);
		} catch (Exception e) {
			e.printStackTrace();
			String jsonReturn = Json.createObjectBuilder().add("status", 1).add("message", e.getMessage()).build()
					.toString();
			response.getWriter().println(jsonReturn);
		}
	}
}
