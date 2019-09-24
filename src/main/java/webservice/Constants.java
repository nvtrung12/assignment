package webservice;

import java.util.Arrays;
import java.util.function.Function;

/**
 * 
 *
 */
public class Constants {
	// all concept and connect, sentencemap will be save to file
	public static String DataFilePath = "/var/log/assessment.xlsx";
	public static String ConnectionSheetName = "CollectionLink";
	public static String SentenceMapSheetName = "sentenceMap";
	public static String ConceptSheetName = "AllNodes";
	public static String META_SHEET_NAME = "metaInfo";
	public static String COLLECTION_LINK_SHEET_NAME = "CollectionLink";

	public static String PHRASE_LIST_FILE_DEFAULT = "defaults/Phrase_default.docx";
	public static String IGNORED_CONCEPTS_FILE_DEFAULT = "defaults/Ignored_concept_default.txt";
	public static String EQUIVALENCE_LIST_FILE_DEFAULT = "defaults/Equivalence_List.xlsx";

	public static final String UPLOAD_FOLDER = "uploads";
	public static final String DOWNLOAD_FOLDER = "downloads";

	// header for excel file
	public static final String NODE_ID_HEADER_NAME = "NodeID";
	public static final String DISPLAY_NAME_HEADER_NAME = "Display Name";
	public static final String OBJECT_INDEX_HEADER_NAME = "ObjectIndex";
	public static final String NODE_NAME_HEADER_NAME = "NodeName";

	// for visualization
	public static final String NODE_SHAPE = "Node Shape";
	public static final String NODE_FILL_COLOR = "Node Fill Color";
	public static final String NODE_OUTLINE_COLOR = "Node Outline Color";
	public static final String NODE_ICON = "Node Icon";

	public static final String[] CONCEPT_HEADER = { NODE_ID_HEADER_NAME, OBJECT_INDEX_HEADER_NAME,
			NODE_NAME_HEADER_NAME, "not used", "not used", "NodeType", "NodeSubType", DISPLAY_NAME_HEADER_NAME,
			"NodeWeight1", "NodeWeight2", NODE_SHAPE, NODE_FILL_COLOR, NODE_OUTLINE_COLOR, NODE_ICON };

	public static final String COLL_LINK_ID = "LinkID";
	public static final String COLL_SOURCE_OBJECT_INDEX = "SourceObjectIndex";
	public static final String COLL_SOURCE_OJBECT = "SourceObject";

	public static final String[] COLLECTION_HEADER = { COLL_LINK_ID, COLL_SOURCE_OBJECT_INDEX, COLL_SOURCE_OJBECT,
			"InType", "SinkObjectIndex", "InThisObject", "OutType", "CollectionLinkType", "LinkNametoDisplay",
			"LInkWeight1", "LinkWeigh2" };

	public static final String[] CONNECT_HEADER = COLLECTION_HEADER;

	public static final Function<String, Integer> fNodeHeaderPos = headerName -> Arrays.asList(CONCEPT_HEADER)
			.indexOf(headerName);
	
	public static final String SOURCE_OBJECT_INDEX = "SourceObjectIndex";
	
	public static final String[] HEADER_CALLINK = { "LinkID", SOURCE_OBJECT_INDEX, "SourceObject", "InType",
			"SinkObjectIndex", "InThisObject", "OutType", "CollectionLinkType", "LinkNametoDisplay", "LInkWeight1",
			"LinkWeigh2" };
	

}
