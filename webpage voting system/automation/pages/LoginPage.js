const { By, until } = require('selenium-webdriver');

class LoginPage {
    constructor(driver) {
        this.driver = driver;
        this.emailField = By.id('email');
        this.passwordField = By.id('password');
        this.loginButton = By.id('loginBtn');
        this.loader = By.id('loader');
    }

    async login(email, password) {
        await this.driver.findElement(this.emailField).sendKeys(email);
        await this.driver.findElement(this.passwordField).sendKeys(password);
        await this.driver.findElement(this.loginButton).click();
    }

    async isLoaderVisible() {
        try {
            const loader = await this.driver.findElement(this.loader);
            return await loader.isDisplayed();
        } catch (e) {
            return false;
        }
    }
}

module.exports = LoginPage;
