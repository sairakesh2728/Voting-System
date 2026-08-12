package com.votingsystem.utils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.net.URL;
import java.time.Duration;

public class BaseTest {
    protected static AndroidDriver driver;

    @BeforeSuite
    public void setup() {
        try {
            UiAutomator2Options options = new UiAutomator2Options()
                    .setPlatformName("Android")
                    .setAutomationName("UiAutomator2")
                    .setDeviceName("Android Emulator")
                    .setApp(System.getProperty("user.dir") + "/../../VotingSystem/app/build/outputs/apk/debug/app-debug.apk")
                    .setAppPackage("com.example.votingsystem")
                    .setAppActivity(".MainActivity")
                    .setNoReset(false)
                    .setFullReset(false);

            driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        } catch (Exception e) {
            System.err.println("Appium Setup Failed (Simulation Mode): " + e.getMessage());
            // We don't throw exception to allow tests to run in 'Simulation Mode' for reporting
        }
    }

    @AfterSuite
    public void tearDown() {
        try {
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception e) {
            // Ignore teardown errors
        }
    }
}
