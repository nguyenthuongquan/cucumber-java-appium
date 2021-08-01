package cucumber.steps;

import io.cucumber.core.backend.TestCaseState;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestCase;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Assume;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import utilities.ReadConfigFile;
import utilities.ThreadLocalDriver;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AppHooks {

    static ReadConfigFile readConfigFile = ConfigFactory.create(ReadConfigFile.class);
    private int currentStepDefIndex = 0;

    @Before(value = "@skip", order = 0)
    public void skipScenario(Scenario scenario) {
        System.out.println("Skipped scenario: " + scenario.getName());
        Assume.assumeTrue(false);
    }

    @Before()
    public void setup() {

    }

    @AfterStep
    public void takeScreenShot(Scenario scenario) throws NoSuchFieldException, IllegalAccessException {
        if (readConfigFile.isScreenshotTakenForEveryStep())
            takeAndEmbedScreenshotToScenario(scenario);
        if (readConfigFile.isScreenshotTakenForVerificationStep()
                && getCurrentStep(scenario).startsWith("verify"))
            takeAndEmbedScreenshotToScenario(scenario);
        currentStepDefIndex += 1;
    }

    @After()
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed() && readConfigFile.isScreenshotTakenWhenScenarioFails())
            takeAndEmbedScreenshotToScenario(scenario);
    }

    private void takeAndEmbedScreenshotToScenario(Scenario scenario) {
        String date = new SimpleDateFormat("yyMMdd-HHmmss-SS").format(new Date());
        byte[] sourcePath = ((TakesScreenshot) ThreadLocalDriver.getTLDriver()).getScreenshotAs(OutputType.BYTES);
        scenario.attach(sourcePath, "image/png", date);
    }

    private String getCurrentStep(Scenario scenario) throws NoSuchFieldException, IllegalAccessException {
        Field f = scenario.getClass().getDeclaredField("delegate");
        f.setAccessible(true);
        TestCaseState sc = (TestCaseState) f.get(scenario);
        Field f1 = sc.getClass().getDeclaredField("testCase");
        f1.setAccessible(true);
        TestCase testCase = (TestCase) f1.get(sc);
        List<PickleStepTestStep> testSteps = testCase.getTestSteps().stream()
                .filter(x -> x instanceof PickleStepTestStep)
                .map(x -> (PickleStepTestStep) x)
                .collect(Collectors.toList());
        return testSteps.get(currentStepDefIndex).getStep().getText();
    }
}
