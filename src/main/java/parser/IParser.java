package parser;

import java.util.Map;

/**
 * 
 * @author thanhhungqb
 *
 */
public interface IParser {

	public Map<String, Object> processFile(String fileName, String sDocID) throws Exception;
}
