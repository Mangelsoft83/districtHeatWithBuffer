package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelToDoubleArray {

    public static double[][] readExcelFileToDoubleArray(String filePath) {
        try (FileInputStream file = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0); // Read the first sheet
            List<double[]> rows = new ArrayList<>();

            for (Row row : sheet) {
                List<Double> values = new ArrayList<>();
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case NUMERIC:
                            values.add(cell.getNumericCellValue());
                            break;
                        default:
                            throw new IllegalArgumentException("Only numeric cells are supported");
                    }
                }
                double[] rowArray = values.stream().mapToDouble(Double::doubleValue).toArray();
                rows.add(rowArray);
            }

            return rows.toArray(new double[0][]);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // or handle the error accordingly
        }
    }

    public static void main(String[] args) {
        String filePath = "path/to/your/excel/file.xlsx";
        double[][] data = readExcelFileToDoubleArray(filePath);
        if (data != null) {
            for (double[] row : data) {
                for (double cell : row) {
                    System.out.print(cell + " ");
                }
                System.out.println();
            }
        }
    }
}

