package graph;

import javax.json.JsonObject;

public interface IGraph {
	/**
	 * 
	 * @return json format for visjs simple graph: {nodes:[], edges:[]}
	 */
	public String getJsonVisjsFormat();

	public JsonObject getJsonVisjsObj();
}
