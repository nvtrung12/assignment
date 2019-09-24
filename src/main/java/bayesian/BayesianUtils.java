package bayesian;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.spark_project.guava.collect.Sets;

public class BayesianUtils {
	/**
	 * Load simple network format from Excel file <br/>
	 * Row 1, 2: k,l,m,n,... and real values <br/>
	 * A4-X.. matrix for direct values 0/1
	 * 
	 * @param is
	 * @return map: prob<string, value> for A1:X2 and matrix<>
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws EncryptedDocumentException
	 */
	public static Map<String, Object> loadSimpleNetworkFormat(Workbook workbook, String sheetName,
			String sheetParam, String sheetAllNodes)
			throws EncryptedDocumentException, InvalidFormatException, IOException {
		Map<String, Object> ret = new HashMap<String, Object>();
		Map<String, String> element = new HashMap<String, String>();
		//Workbook workbook = WorkbookFactory.create(inputStream);

		Sheet sheet = workbook.getSheet(sheetName);
		Sheet sheetP = workbook.getSheet(sheetParam);
		//sheetCollection
		Sheet sheetAll = workbook.getSheet(sheetAllNodes);
		
		DataFormatter dataFormatter = new DataFormatter();

		Map<String, Double> prob = new HashMap<>();
		Row r1 = sheetP.getRow(0);
		Row r2 = sheetP.getRow(1);
		for (int i = r1.getFirstCellNum(); i < r1.getLastCellNum(); ++i) {
			prob.put(dataFormatter.formatCellValue(r1.getCell(i)),
					Double.parseDouble(dataFormatter.formatCellValue(r2.getCell(i))));
		}

		// store graph with adj format
		// vi -> list of adj
		//Map<String, Set<String>> graphAdj = new HashMap<>();
		// load graph here
		// row 3 (4th) is vertexs
		//Row r3 = sheet.getRow(1);
		//final Function<Integer, String> fVertex = idx -> dataFormatter.formatCellValue(r3.getCell(idx));
		MultiValuedMap<String, String> graphAdj = new ArrayListValuedHashMap<>();
		MultiValuedMap<String, String> graphToMe = new ArrayListValuedHashMap<>();
		for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); ++rowNum) {
			Row row = sheet.getRow(rowNum);
			String sourceId = dataFormatter.formatCellValue(row.getCell(1));
			String sourceName = dataFormatter.formatCellValue(row.getCell(2));
			String sinkId = dataFormatter.formatCellValue(row.getCell(4));
			String sinkName = dataFormatter.formatCellValue(row.getCell(5));
			if (!sourceName.equals("") && !sinkName.equals("")) {
				graphAdj.put(sourceName, sinkName);
				graphToMe.put(sinkName, sourceName);
				element.put(sinkId, sinkName);
				element.put(sourceId, sourceName);
			}
		}
		//workbook.close();
		Map<String, Collection<String>> x =  graphAdj.asMap();
		Map<String, Set<String>> grapMap =  new HashMap<>();
		x.forEach((k,v)->{
			Set<String> value = Sets.newHashSet(v);
			grapMap.put(k, value);
		});
		
		
		
		Map<String, Collection<String>> y =  graphToMe.asMap();
		Map<String, Set<String>> graphToMe2 =  new HashMap<>();
		y.forEach((k,v)->{
			Set<String> value = Sets.newHashSet(v);
			graphToMe2.put(k, value);
		});
		//get data from sheetAllNodes
		Map<String, String> allNode = new HashMap<>(); 
		for (int rowNum = 1; rowNum <= sheetAll.getLastRowNum(); ++rowNum) {
			Row row = sheetAll.getRow(rowNum);
			String objectIndex = dataFormatter.formatCellValue(row.getCell(1));
			String type = dataFormatter.formatCellValue(row.getCell(5));
			if(!objectIndex.equals("") && !type.equals("") && !type.equals("NULL") && !objectIndex.equals("NULL")) {
				allNode.put(objectIndex, type);
			}
		}
		ret.put("prob", prob);//{k=0.5, l=0.25, m=0.7, n=0.2}
		ret.put("graph", grapMap);//{Q1=[C1, C2], C3=[C5], Q2=[C2], C4=[C5], C1=[C3, C4], C2=[C4]}
		ret.put("graphToMe", graphToMe2);//{C3=[C1], C4=[C1, C2], C5=[C3, C4], C1=[Q1], C2=[Q1, Q2]}
		ret.put("allNode", allNode);
		ret.put("collections", element);
		ret.put("workbook", workbook);
		return ret;
	}
	
}
