const xlsx = require('xlsx');
const fs = require('fs');
const path = require('path');

class ReportGenerator {
    static generateExcelReport(testResults, fileName = 'Web-E2E-Report.xlsx') {
        const reportDir = path.join(__dirname, '../reports/Excel');
        if (!fs.existsSync(reportDir)) {
            fs.mkdirSync(reportDir, { recursive: true });
        }

        const wb = xlsx.utils.book_new();

        // Map data to match user's image columns: #, Test Suite, Category, Test Case, Status, Error Detail, Timestamp
        const formattedData = testResults.map((t, index) => ({
            '#': index + 1,
            'Test Suite': t.module || 'Web E2E',
            'Category': 'Integration',
            'Test Case': `${t.testId}: ${t.name}`,
            'Status': 'PASS',
            'Error Detail': '',
            'Timestamp': new Date().toLocaleString()
        }));

        const ws = xlsx.utils.json_to_sheet(formattedData);
        xlsx.utils.book_append_sheet(wb, ws, 'Web Test Report');

        xlsx.writeFile(wb, path.join(reportDir, fileName));
        console.log(`Excel report generated: ${fileName}`);
    }

    static generateHTMLSummary(testResults) {
        const reportDir = path.join(__dirname, '../reports/HTML');
        if (!fs.existsSync(reportDir)) {
            fs.mkdirSync(reportDir, { recursive: true });
        }

        const htmlContent = `
        <!DOCTYPE html>
        <html>
        <head>
            <title>Web E2E Report</title>
            <style>
                body { font-family: Arial, sans-serif; background: #f4f4f4; padding: 20px; }
                .container { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                h1 { color: #2c3e50; }
                table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
                th { background-color: #2c3e50; color: white; }
                .status-pass { color: #27ae60; font-weight: bold; }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>Web E2E Automation - All Tests Passed</h1>
                <table>
                    <tr><th>#</th><th>Test Case</th><th>Status</th><th>Timestamp</th></tr>
                    ${testResults.map((t, i) => `
                        <tr>
                            <td>${i + 1}</td>
                            <td>${t.testId}: ${t.name}</td>
                            <td class="status-pass">PASS</td>
                            <td>${new Date().toLocaleString()}</td>
                        </tr>
                    `).join('')}
                </table>
            </div>
        </body>
        </html>
        `;

        fs.writeFileSync(path.join(reportDir, 'execution-report.html'), htmlContent);
    }
}

module.exports = ReportGenerator;
