package com.votingsystem.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelReporter {
    public static void generateReport(String fileName, List<TestResult> results) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Execution Report");

        String[] columns = {"Test ID", "Module", "Test Name", "Priority", "Status", "Duration"};
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        int rowNum = 1;
        for (TestResult result : results) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(result.id);
            row.createCell(1).setCellValue(result.module);
            row.createCell(2).setCellValue(result.name);
            row.createCell(3).setCellValue(result.priority);
            row.createCell(4).setCellValue(result.status);
            row.createCell(5).setCellValue(result.duration);
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
