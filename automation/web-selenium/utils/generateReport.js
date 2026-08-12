const xlsx = require('xlsx');
const fs = require('fs');
const path = require('path');

class ReportGenerator {
    static generateExcelReport(testResults, fileName = 'Automation_Test_Report.xlsx') {
        const reportDir = path.join(__dirname, '../reports/Excel');
        if (!fs.existsSync(reportDir)) {
            fs.mkdirSync(reportDir, { recursive: true });
        }

        const wb = xlsx.utils.book_new();

        // Sheet 1: Executed Test Cases
        const wsExecuted = xlsx.utils.json_to_sheet(testResults);
        xlsx.utils.book_append_sheet(wb, wsExecuted, 'Executed Test Cases');

        // Sheet 2: Passed Tests
        const passedTests = testResults.filter(t => t.status === 'PASS');
        const wsPassed = xlsx.utils.json_to_sheet(passedTests);
        xlsx.utils.book_append_sheet(wb, wsPassed, 'Passed Tests');

        // Sheet 3: Failed Tests
        const failedTests = testResults.filter(t => t.status === 'FAIL');
        const wsFailed = xlsx.utils.json_to_sheet(failedTests);
        xlsx.utils.book_append_sheet(wb, wsFailed, 'Failed Tests');

        // Summary Sheet
        const summary = [
            { Metric: 'Total Tests', Value: testResults.length },
            { Metric: 'Passed', Value: passedTests.length },
            { Metric: 'Failed', Value: failedTests.length },
            { Metric: 'Pass Rate', Value: `${((passedTests.length / testResults.length) * 100).toFixed(2)}%` }
        ];
        const wsSummary = xlsx.utils.json_to_sheet(summary);
        xlsx.utils.book_append_sheet(wb, wsSummary, 'Summary');

        xlsx.writeFile(wb, path.join(reportDir, fileName));
        console.log(`Excel report generated: ${fileName}`);
    }

    static generateHTMLSummary(testResults) {
        const reportDir = path.join(__dirname, '../reports/HTML');
        if (!fs.existsSync(reportDir)) {
            fs.mkdirSync(reportDir, { recursive: true });
        }

        const passedCount = testResults.filter(t => t.status === 'PASS').length;
        const failedCount = testResults.filter(t => t.status === 'FAIL').length;

        const htmlContent = `
        <!DOCTYPE html>
        <html>
        <head>
            <title>Execution Report</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                .summary { display: flex; gap: 20px; margin-bottom: 30px; }
                .card { padding: 20px; border-radius: 8px; color: white; flex: 1; text-align: center; }
                .total { background: #3498db; }
                .passed { background: #2ecc71; }
                .failed { background: #e74c3c; }
                table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                th { background-color: #f2f2f2; }
                .status-pass { color: green; font-weight: bold; }
                .status-fail { color: red; font-weight: bold; }
            </style>
        </head>
        <body>
            <h1>Voting System E2E Test Report</h1>
            <div class="summary">
                <div class="card total"><h3>Total</h3><p>${testResults.length}</p></div>
                <div class="card passed"><h3>Passed</h3><p>${passedCount}</p></div>
                <div class="card failed"><h3>Failed</h3><p>${failedCount}</p></div>
            </div>
            <table>
                <tr>
                    <th>Test ID</th>
                    <th>Module</th>
                    <th>Test Name</th>
                    <th>Status</th>
                    <th>Execution Time</th>
                </tr>
                ${testResults.map(t => `
                    <tr>
                        <td>${t.testId}</td>
                        <td>${t.module}</td>
                        <td>${t.name}</td>
                        <td class="status-${t.status.toLowerCase()}">${t.status}</td>
                        <td>${t.duration}ms</td>
                    </tr>
                `).join('')}
            </table>
        </body>
        </html>
        `;

        fs.writeFileSync(path.join(reportDir, 'execution-report.html'), htmlContent);
        console.log('HTML report generated: execution-report.html');
    }
}

module.exports = ReportGenerator;
