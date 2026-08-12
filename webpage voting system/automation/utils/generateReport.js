const XLSX = require('xlsx');
const fs = require('fs');
const path = require('path');

function generateExcelReport(results) {
    const wb = XLSX.utils.book_new();

    // Sheet 1: All Executed Test Cases
    const wsAll = XLSX.utils.json_to_sheet(results);
    XLSX.utils.book_append_sheet(wb, wsAll, "Executed Test Cases");

    // Sheet 2: Passed
    const passed = results.filter(r => r.status === 'PASS');
    const wsPassed = XLSX.utils.json_to_sheet(passed);
    XLSX.utils.book_append_sheet(wb, wsPassed, "Passed Tests");

    // Sheet 3: Failed
    const failed = results.filter(r => r.status === 'FAIL');
    const wsFailed = XLSX.utils.json_to_sheet(failed);
    XLSX.utils.book_append_sheet(wb, wsFailed, "Failed Tests");

    const reportPath = path.join(__dirname, '../reports/Automation_Test_Report.xlsx');
    XLSX.writeFile(wb, reportPath);
    console.log(`Report generated at: ${reportPath}`);
}

// Example usage / Export
module.exports = { generateExcelReport };
