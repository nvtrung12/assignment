package graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A collection of parent of concept (parent of concept may be sentence or
 * question or more) provide some functions to get parents by concept
 *
 * Based on define, parent maybe present by id or by content or Object with full
 * info
 */ 
public class ConceptParentCollection {

	protected Map<String, Object> mapConceptParents = new HashMap<>();
	
	public ConceptParentCollection() {

	}

	/**
	 * Get all parent of current concept,
	 * 
	 * @param concept
	 * @return TODO currently empty
	 */
	public List<Object> getParents(String concept) {
		return new LinkedList<Object>();
	}
}
