package bayesian;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

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
	public static Map<String, Object> loadSimpleNetworkFormat(InputStream inputStream, String sheetName)
			throws EncryptedDocumentException, InvalidFormatException, IOException {
		Map<String, Object> ret = new HashMap<String, Object>();

		Workbook workbook = WorkbookFactory.create(inputStream);

		Sheet sheet = workbook.getSheet(sheetName);

		DataFormatter dataFormatter = new DataFormatter();

		Map<String, Double> prob = new HashMap<>();
		Row r1 = sheet.getRow(0);
		Row r2 = sheet.getRow(1);
		for (int i = r1.getFirstCellNum(); i < r1.getLastCellNum(); ++i) {
			prob.put(dataFormatter.formatCellValue(r1.getCell(i)),
					Double.parseDouble(dataFormatter.formatCellValue(r2.getCell(i))));
		}

		// store graph with adj format
		// vi -> list of adj
		Map<String, Set<String>> graphAdj = new HashMap<>();
		// load graph here
		// row 3 (4th) is vertexs

		Row r3 = sheet.getRow(3);
		final Function<Integer, String> fVertex = idx -> dataFormatter.formatCellValue(r3.getCell(idx));

		for (int rowNum = 4; rowNum < sheet.getLastRowNum(); ++rowNum) {
			Row row = sheet.getRow(rowNum);
			String vertex = dataFormatter.formatCellValue(row.getCell(0));
			Set<String> nextVertext = new HashSet<>();

			for (int colNum = 1; colNum < row.getLastCellNum(); ++colNum) {
				try {
					String cellStr = dataFormatter.formatCellValue(row.getCell(colNum));
					int cellInt = Integer.parseInt(cellStr);
					if (1 == cellInt) {
						nextVertext.add(fVertex.apply(colNum));
					}
				} catch (Exception e) {
					// not correct adj, omit
				}
			}
			graphAdj.put(vertex, nextVertext);
		}
		workbook.close();

		// build reverse graphAdj, who connect to me
		Map<String, Set<String>> graphToMe = new HashMap<>();
		for (String fromVertext : graphAdj.keySet()) {
			for (String toVertext : graphAdj.get(fromVertext)) {
				if (graphToMe.get(toVertext) == null) {
					graphToMe.put(toVertext, new HashSet<>());
				}
				graphToMe.get(toVertext).add(fromVertext);
			}
		}

		ret.put("prob", prob);
		ret.put("graph", graphAdj);
		ret.put("graphToMe", graphToMe);
		return ret;
	}
}
