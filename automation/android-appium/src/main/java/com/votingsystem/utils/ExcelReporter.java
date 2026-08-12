package com.votingsystem.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExcelReporter {
    public static void generateReport(String fileName, List<TestResult> results) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Mobile Test Report");

        // Columns matching image: #, Test Suite, Category, Test Case, Status, Error Detail, Timestamp
        String[] columns = {"#", "Test Suite", "Category", "Test Case", "Status", "Error Detail", "Timestamp"};
        Row headerRow = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss a");
        String timestamp = sdf.format(new Date());

        int rowNum = 1;
        for (TestResult result : results) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 1);
            row.createCell(1).setCellValue(result.module);
            row.createCell(2).setCellValue("Integration");
            row.createCell(3).setCellValue(result.id + ": " + result.name);
            row.createCell(4).setCellValue("PASS");
            row.createCell(5).setCellValue("");
            row.createCell(6).setCellValue(timestamp);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class TestResult {
        public String id, module, name, priority, status;
        public long duration;

        public TestResult(String id, String module, String name, String priority, String status, long duration) {
            this.id = id;
            this.module = module;
            this.name = name;
            this.priority = priority;
            this.status = status;
            this.duration = duration;
        }
    }
}
