package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 
 * @author thanhhungqb
 *
 */
public class XlsxUtil {
	/***
	 * Write a row data to sheet (opened) (new row)
	 * 
	 * @param sheet    xlsx openned sheet
	 * @param rowNum
	 * @param row_data
	 * @return
	 */
	public static final int MAX_ROW_SHEET = 30;
	
	public static String nextFile(List<String> fileNames, String downloadFolder, String fileNameBase) {
		if (fileNames == null || fileNames.size() == 0) {
			String conceptsFileName = String.format("%s.xlsx", fileNameBase + "_1");
			String conceptSfilePath = String.format("%s%s%s", downloadFolder, File.separator, conceptsFileName);
			return conceptSfilePath;
		}
		String conceptsFileName = String.format("%s.xlsx", fileNameBase + "_" + (fileNames.size() + 1));
		String conceptSfilePath = String.format("%s%s%s", downloadFolder, File.separator, conceptsFileName);
		return conceptSfilePath;
	}
	
	public static String getLastFile(List<String> fileNames) {
		return fileNames.get(fileNames.size() - 1);
	}
	
	public static boolean writeRowXlsx(XSSFSheet sheet, int rowNum, List<Object> row_data) {
		Row row = sheet.createRow(rowNum);
		int colNum = 0;
		for (Object field : row_data) {
			Cell cell = row.createCell(colNum++);
			if (field instanceof String) {
				cell.setCellValue((String) field);
			} else if (field instanceof Integer) {
				cell.setCellValue((Integer) field);
			} else if (field instanceof Float || field instanceof Double) {
				cell.setCellValue((double) field);
			}
		}

		return true;
	}

	/**
	 * 
	 * @param filePath
	 * @param sheetName
	 * @param row_datas
	 * @return
	 * @throws IOException
	 */
	
	public static boolean writeXlsx(String filePath, String sheetName, List<List<Object>> row_datas)
			throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet(sheetName);
		int rowNum = 0;

		for (List<Object> row_data : row_datas) {
			Row row = sheet.createRow(rowNum++);
			int colNum = 0;
			for (Object field : row_data) {
				Cell cell = row.createCell(colNum++);
				if (field instanceof String) {
					cell.setCellValue((String) field);
				} else if (field instanceof Integer) {
					cell.setCellValue((Integer) field);
				} else if (field instanceof Float || field instanceof Double) {
					cell.setCellValue((double) field);
				}
			}
		}

		FileOutputStream outputStream = new FileOutputStream(filePath);
		workbook.write(outputStream);
		workbook.close();

		return true;
	}
	
	public static boolean writeXlsxMutil(List<String> fileNames, String downloadFolder, String fileNameBase, String sheetName, List<List<Object>> row_datas) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet(sheetName);
		int rowNum = 0;
		
		for (List<Object> row_data : row_datas) {
			Row row = sheet.createRow(rowNum++);
			int colNum = 0;
			for (Object field : row_data) {
				Cell cell = row.createCell(colNum++);
				if (field instanceof String) {
					cell.setCellValue((String) field);
				} else if (field instanceof Integer) {
					cell.setCellValue((Integer) field);
				} else if (field instanceof Float || field instanceof Double) {
					cell.setCellValue((double) field);
				}
			}
			if (rowNum >= MAX_ROW_SHEET) {
				FileOutputStream outputStream = new FileOutputStream(getLastFile(fileNames));
				workbook.write(outputStream);
				workbook.close();
				fileNames.add(nextFile(fileNames, downloadFolder, fileNameBase));
				workbook = new XSSFWorkbook();
				sheet = workbook.createSheet(sheetName);
				rowNum = 0;
			}
		}
		FileOutputStream outputStream = new FileOutputStream(getLastFile(fileNames));
		workbook.write(outputStream);
		workbook.close();
		return true;
	}

	/**
	 * 
	 * @param filePath
	 * @param sheetName
	 * @param row_datas
	 * @return
	 * @throws EncryptedDocumentException
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static boolean writeRowsXlsxAppend(String filePath, String sheetName, List<List<Object>> row_datas)
			throws EncryptedDocumentException, InvalidFormatException, IOException {

		InputStream inp = new FileInputStream(filePath);
		Workbook workbook = WorkbookFactory.create(inp);
		Sheet sheet = workbook.getSheet(sheetName);
		if (null == sheet) {
			sheet = workbook.createSheet(sheetName);
		}

		int rows = sheet.getLastRowNum();
		System.out.println(rows);

		int rowNum;
		if (rows <= 1)
			rowNum = rows;
		else
			rowNum = rows + 1;

		for (List<Object> row_data : row_datas) {
			Row row = sheet.createRow(rowNum++);
			int colNum = 0;
			for (Object field : row_data) {
				Cell cell = row.createCell(colNum++);
				if (field instanceof String) {
					cell.setCellValue((String) field);
				} else if (field instanceof Integer) {
					cell.setCellValue((Integer) field);
				} else if (field instanceof Float || field instanceof Double) {
					cell.setCellValue((double) field);
				} else {
					cell.setCellValue("");
				}
			}
		}
		inp.close();

		FileOutputStream fileOut = new FileOutputStream(filePath);
		workbook.write(fileOut);
		fileOut.close();
		workbook.close();

		return true;
	}
	
	public static boolean writeRowsXlsxAppendMuti(List<String> fileNames, String downloadFolder, String fileNameBase, String sheetName, List<List<Object>> row_datas)
			throws EncryptedDocumentException, InvalidFormatException, IOException {

		InputStream inp = new FileInputStream(getLastFile(fileNames));
		Workbook workbook = WorkbookFactory.create(inp);
		Sheet sheet = workbook.getSheet(sheetName);
		if (null == sheet) {
			sheet = workbook.createSheet(sheetName);
		}

		int rows = sheet.getLastRowNum();
		System.out.println(rows);

		int rowNum;
		if (rows <= 1)
			rowNum = rows;
		else
			rowNum = rows + 1;

		for (List<Object> row_data : row_datas) {
			Row row = sheet.createRow(rowNum++);
			int colNum = 0;
			for (Object field : row_data) {
				Cell cell = row.createCell(colNum++);
				if (field instanceof String) {
					cell.setCellValue((String) field);
				} else if (field instanceof Integer) {
					cell.setCellValue((Integer) field);
				} else if (field instanceof Float || field instanceof Double) {
					cell.setCellValue((double) field);
				} else {
					cell.setCellValue("");
				}
			}
			
			if (rowNum >= MAX_ROW_SHEET) {
				FileOutputStream outputStream = new FileOutputStream(getLastFile(fileNames));
				workbook.write(outputStream);
				workbook.close();
				fileNames.add(nextFile(fileNames, downloadFolder, fileNameBase));
				workbook = new XSSFWorkbook();
				sheet = workbook.createSheet(sheetName);
				rowNum = 0;
			}
		}
		inp.close();

		FileOutputStream fileOut = new FileOutputStream(getLastFile(fileNames));
		workbook.write(fileOut);
		fileOut.close();
		workbook.close();

		return true;
	}

	/**
	 * 
	 * @param filePath
	 * @param sheetName
	 * @return List<List<String>>
	 * @throws Exception
	 */
	public static List<List<String>> readXlsx(String filePath, String sheetName) throws Exception {
		Workbook workbook = WorkbookFactory.create(new File(filePath), null, true);

		Sheet sheet = workbook.getSheet(sheetName);

		DataFormatter dataFormatter = new DataFormatter();

		List<List<String>> ret = new LinkedList<>();

		for (Row row : sheet) {
			short minColIx = row.getFirstCellNum();
			short maxColIx = row.getLastCellNum();

			// initial list data with empty string
			List<String> rowList = new LinkedList<String>();
			for (short cellIndex = 0; cellIndex < maxColIx; ++cellIndex)
				rowList.add("");

			for (short colIx = minColIx; colIx < maxColIx; colIx++) {
				Cell cell = row.getCell(colIx);
				if (cell == null) {
					continue;
				} else {
					String cellValue = dataFormatter.formatCellValue(cell);
					rowList.set(colIx, cellValue);
				}
			}
			if (rowList.size() > 0)
				ret.add(rowList);
		}

		workbook.close();

		return ret;
	}
	
	public static List<List<String>> readFolderXlsx(String filePath, String sheetName) throws Exception {
		List<List<String>> ret = new LinkedList<>();
		for(File file: new File(filePath).listFiles()) {
			String fileName = file.getName();
			if(fileName.endsWith(".xlsx")) {
				Workbook workbook = WorkbookFactory.create(file, null, true);

				Sheet sheet = workbook.getSheet(sheetName);
				if (sheet != null) {

					DataFormatter dataFormatter = new DataFormatter();
					for (Row row : sheet) {
						short minColIx = row.getFirstCellNum();
						short maxColIx = row.getLastCellNum();

						// initial list data with empty string
						List<String> rowList = new LinkedList<String>();
						for (short cellIndex = 0; cellIndex < maxColIx; ++cellIndex)
							rowList.add("");

						for (short colIx = minColIx; colIx < maxColIx; colIx++) {
							Cell cell = row.getCell(colIx);
							if (cell == null) {
								continue;
							} else {
								String cellValue = dataFormatter.formatCellValue(cell);
								rowList.set(colIx, cellValue);
							}
						}
						if (rowList.size() > 0)
							ret.add(rowList);
					}

					workbook.close();
				}
			}
			
		}
		return ret;
	}

	public static List<List<String>> readXlsx(InputStream inputStream, String sheetName) throws Exception {
		Workbook workbook = WorkbookFactory.create(inputStream);

		Sheet sheet = workbook.getSheet(sheetName);

		DataFormatter dataFormatter = new DataFormatter();

		List<List<String>> ret = new LinkedList<>();

		for (Row row : sheet) {
			short minColIx = row.getFirstCellNum();
			short maxColIx = row.getLastCellNum();

			// initial list data with empty string
			List<String> rowList = new LinkedList<String>();
			for (short cellIndex = 0; cellIndex < maxColIx; ++cellIndex)
				rowList.add("");

			for (short colIx = minColIx; colIx < maxColIx; colIx++) {
				Cell cell = row.getCell(colIx);
				if (cell == null) {
					continue;
				} else {
					String cellValue = dataFormatter.formatCellValue(cell);
					rowList.set(colIx, cellValue);
				}
			}
			if (rowList.size() > 0)
				ret.add(rowList);
		}

		workbook.close();

		return ret;
	}

	/**
	 * Convert Object[] to List<Object> in Excel data
	 * 
	 * @param data
	 * @return
	 */
	public static List<List<Object>> convertRowType(List<Object[]> data) {
		List<List<Object>> out = data.stream().map(o -> Arrays.asList(o)).collect(Collectors.toList());
		return out;
	}
}
