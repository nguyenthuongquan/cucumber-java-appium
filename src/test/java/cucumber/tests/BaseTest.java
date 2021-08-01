package cucumber.tests;

import io.appium.java_client.android.AndroidDriver;
import org.aeonbits.owner.ConfigFactory;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import utilities.DesiredCapabilitiesUtil;
import utilities.ReadConfigFile;
import utilities.ThreadLocalDriver;

import java.io.IOException;
import java.net.URL;

public class BaseTest {
    private final DesiredCapabilitiesUtil desiredCapabilitiesUtil = new DesiredCapabilitiesUtil();
    static ReadConfigFile readConfigFile = ConfigFactory.create(ReadConfigFile.class);

    @BeforeMethod
    @Parameters({"udid", "platformVersion"})
    public void setup(@Optional String udid, @Optional String platformVersion) throws IOException {
        DesiredCapabilities caps = desiredCapabilitiesUtil.getDesiredCapabilities(udid, platformVersion);
        ThreadLocalDriver.setTLDriver(new AndroidDriver<>(new URL(readConfigFile.appiumServerURL()), caps));
    }

    @AfterMethod()
    public synchronized void teardown() {
        ThreadLocalDriver.getTLDriver().quit();
    }
}
