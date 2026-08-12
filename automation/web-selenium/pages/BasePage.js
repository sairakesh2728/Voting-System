const { until, By } = require('selenium-webdriver');

class BasePage {
    constructor(driver) {
        this.driver = driver;
        this.timeout = 10000;
    }

    async navigateTo(url) {
        await this.driver.get(url);
    }

    async waitForElement(locator) {
        return await this.driver.wait(until.elementLocated(locator), this.timeout);
    }

    async click(locator) {
        const element = await this.waitForElement(locator);
        await this.driver.wait(until.elementIsVisible(element), this.timeout);
        await element.click();
    }

    async sendKeys(locator, text) {
        const element = await this.waitForElement(locator);
        await element.clear();
        await element.sendKeys(text);
    }

    async getText(locator) {
        const element = await this.waitForElement(locator);
        return await element.getText();
    }

    async takeScreenshot(name) {
        const image = await this.driver.takeScreenshot();
        require('fs').writeFileSync(`./reports/screenshots/${name}.png`, image, 'base64');
    }
}

module.exports = BasePage;
