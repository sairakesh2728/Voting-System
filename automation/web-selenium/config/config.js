require('dotenv').config();

module.exports = {
    baseUrl: process.env.BASE_URL || 'https://sairakesh2728.github.io/Voting-System/',
    browser: 'chrome',
    headless: true,
    timeout: 30000,
    reportPath: './reports'
};
