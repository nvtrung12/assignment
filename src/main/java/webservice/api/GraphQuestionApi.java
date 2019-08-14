package webservice.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import graph.IGraph;
import graph.QuestionGraph;
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

@WebServlet(urlPatterns = { "/api/graphQuestion", "/api/v1.1/graphQuestion" })
@MultipartConfig
public class GraphQuestionApi extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final Logger logger = Logger.getLogger(this.getClass());

	private List<List<String>> connections = null;
	private List<List<String>> concepts = null;

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

		String appPath = request.getServletContext().getRealPath("/");
		// get from query
		String fileName = request.getParameter("fileName");
		logger.debug("fileName: " + fileName);
		String graphType = request.getParameter("graphType");
		logger.debug("graphType: " + graphType);
		String question = request.getParameter("question");
		logger.debug("question: " + question);

		// convert to full path
		fileName = String.format("%s%s", appPath, fileName);

		try {

			List<List<String>> connections = XlsxUtil.readFolderXlsx(fileName, Constants.ConnectionSheetName);
			List<List<String>> concepts = XlsxUtil.readFolderXlsx(fileName, Constants.ConceptSheetName);

			// remove header
			connections.remove(0);
			concepts.remove(0);

			this.concepts = concepts;
			this.connections = connections;

			// map from id to sentence content
			Map<String, String> id2sent = new HashMap();

			// concepts.stream().filter(o -> true).collect(Collectors.toList());
			int nodeNamePos = Utils.fConceptPos.apply(Constants.NODE_NAME_HEADER_NAME);
			int displayNamePos = Utils.fConceptPos.apply(Constants.DISPLAY_NAME_HEADER_NAME);
			final int nodeIdPos = Utils.fConceptPos.apply(Constants.NODE_ID_HEADER_NAME);

			final int sourceObjectPos = Utils.fConnectionPos.apply(Constants.COLL_SOURCE_OJBECT);
			final int linkIdPos = Utils.fConnectionPos.apply(Constants.COLL_LINK_ID);

			id2sent = concepts.stream().filter(o -> "NULL".equals(o.get(nodeNamePos)))
					.collect(Collectors.toMap(o -> o.get(nodeIdPos), o -> o.get(displayNamePos)));
			Map<String, List<String>> concept2sents = connections.stream()
					.collect(Collectors.toMap(o -> o.get(sourceObjectPos), o -> {
						List<String> ret = new LinkedList<>();
						ret.add(o.get(linkIdPos));
						return ret;
					}, (o1, o2) -> {
						o1.addAll(o2);
						return o1;
					}));

			filter_by_question(question);

			concepts = this.concepts;
			connections = this.connections;

			IGraph graph = null;

			graph = new QuestionGraph(concepts, connections, id2sent, concept2sents);

			// String graphjs = graph.getJsonVisjsFormat();

			String jsonReturn = Json.createObjectBuilder().add("status", 0).add("fileName", fileName)
					.add("grData", graph.getJsonVisjsObj()).build().toString();

			response.getWriter().println(jsonReturn);
		} catch (Exception e) {
			e.printStackTrace();
			String jsonReturn = Json.createObjectBuilder().add("status", 1).add("message", e.getMessage()).build()
					.toString();
			response.getWriter().println(jsonReturn);
		}
	}

	/**
	 * This version one or many link match question
	 * 
	 * @param question
	 * @throws Exception
	 */
	private void filter_by_question(String question) throws Exception {
		String[] questionWords = question.split("\\s+");
		logger.debug("words: " + questionWords);

		int xpos = Utils.fConnectionPos.apply(Constants.COLL_SOURCE_OJBECT);
		int linkIdPos = Utils.fConnectionPos.apply(Constants.COLL_LINK_ID);
		int nodeIdPos = Utils.fConceptPos.apply(Constants.NODE_ID_HEADER_NAME);
		int soidxPos = Utils.fConnectionPos.apply(Constants.COLL_SOURCE_OBJECT_INDEX);

		// keep linkID, and get only one, useful when extend to partial match question
		Set<String> keepLinkIdSet = this.connections.stream().filter(o -> o.get(xpos).equals(question))
				.map(o -> o.get(linkIdPos)).collect(Collectors.toSet());

		if (keepLinkIdSet.size() == 0) {
			logger.error("Question input not found");
			throw new Exception("Question input not found");
		}

		// only keep connection
		this.connections = this.connections.stream().filter(o -> keepLinkIdSet.contains(o.get(linkIdPos)))
				.collect(Collectors.toList());

		logger.debug("keep connections: " + this.connections);

		// get keep concepts and related sentence
		Set<String> conceptSet = this.connections.stream().map(o -> o.get(soidxPos)).collect(Collectors.toSet());
		Set<String> sentenceSet = conceptSet.stream().map(o -> graph.Utils.extractSentenceIndex(o))
				.collect(Collectors.toSet());
		conceptSet.addAll(sentenceSet);

		this.concepts = this.concepts.stream().filter(o -> conceptSet.contains(o.get(nodeIdPos)))
				.collect(Collectors.toList());
		logger.debug("keep conncepts: " + this.concepts);
	}
}
