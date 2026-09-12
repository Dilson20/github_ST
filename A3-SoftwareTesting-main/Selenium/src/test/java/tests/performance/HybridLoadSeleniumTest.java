package tests.performance;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Hybrid Performance Test: Real Browser UI Latency under JMeter Concurrency")
public class HybridLoadSeleniumTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String SERVICES_URL = BASE_URL + "/owners/6/pets/7/visits/1/services";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Compare browser rendering and click latency: Baseline vs 50-User Background Load")
    void testFrontendLatencyUnderBackgroundLoad() throws Exception {
        // -------------------------------------------------------------
        // Phase 1: Measure Baseline (Zero Background Load)
        // -------------------------------------------------------------
        System.out.println("\n>>> [Phase 1] Measuring Baseline Browser Latency (No Background Load)...");
        driver.get(SERVICES_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));

        Map<String, Object> baselineNav = getNavigationTimings();
        long baselineTtfb = ((Number) baselineNav.getOrDefault("ttfb", 0)).longValue();
        long baselineDomInteractive = ((Number) baselineNav.getOrDefault("domInteractive", 0)).longValue();
        long baselinePageLoad = ((Number) baselineNav.getOrDefault("pageLoad", 0)).longValue();

        long baselineInteractionMs = measureAddServiceLatency();

        System.out.printf("Baseline - TTFB: %d ms | DOM Interactive: %d ms | Total Page: %d ms | Add Service Click: %d ms%n",
                baselineTtfb, baselineDomInteractive, baselinePageLoad, baselineInteractionMs);

        // -------------------------------------------------------------
        // Phase 2: Start Background JMeter Load (50 Virtual Users)
        // -------------------------------------------------------------
        System.out.println("\n>>> [Phase 2] Spawning Background JMeter Load (50 Virtual Users)...");
        Process jmeterProcess = startBackgroundJMeterLoad();

        try {
            // Give JMeter 3 seconds to ramp up background traffic
            Thread.sleep(3000);

            // -------------------------------------------------------------
            // Phase 3: Measure Frontend Latency Under High Background Load
            // -------------------------------------------------------------
            System.out.println(">>> [Phase 3] Measuring Browser Latency under Concurrent Backend Load...");
            driver.get(SERVICES_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));

            Map<String, Object> underLoadNav = getNavigationTimings();
            long loadedTtfb = ((Number) underLoadNav.getOrDefault("ttfb", 0)).longValue();
            long loadedDomInteractive = ((Number) underLoadNav.getOrDefault("domInteractive", 0)).longValue();
            long loadedPageLoad = ((Number) underLoadNav.getOrDefault("pageLoad", 0)).longValue();

            long loadedInteractionMs = measureAddServiceLatency();

            System.out.printf("Under Load - TTFB: %d ms | DOM Interactive: %d ms | Total Page: %d ms | Add Service Click: %d ms%n",
                    loadedTtfb, loadedDomInteractive, loadedPageLoad, loadedInteractionMs);

            // -------------------------------------------------------------
            // Comparative Summary
            // -------------------------------------------------------------
            System.out.println("\n==========================================================================");
            System.out.println("                HYBRID TESTING: FRONTEND LATENCY COMPARISON               ");
            System.out.println("==========================================================================");
            System.out.printf("%-25s | %-15s | %-15s | %-10s%n", "Metric", "Baseline", "Under 50 Users", "Impact");
            System.out.println("--------------------------------------------------------------------------");
            printComparisonRow("Time To First Byte (TTFB)", baselineTtfb, loadedTtfb);
            printComparisonRow("DOM Interactive Time", baselineDomInteractive, loadedDomInteractive);
            printComparisonRow("Full Page Load Event", baselinePageLoad, loadedPageLoad);
            printComparisonRow("UI Click Reaction Time", baselineInteractionMs, loadedInteractionMs);
            System.out.println("==========================================================================\n");

            // Assertions: Verify application remained usable under background stress
            assertTrue(loadedPageLoad < 5000, "Page load under background load must stay under 5000ms, but was " + loadedPageLoad);
            assertTrue(loadedInteractionMs < 3000, "Service interaction latency must stay under 3000ms, but was " + loadedInteractionMs);

        } finally {
            // Clean up background JMeter process
            if (jmeterProcess != null && jmeterProcess.isAlive()) {
                System.out.println(">>> Stopping background JMeter process...");
                jmeterProcess.destroyForcibly();
                jmeterProcess.waitFor();
                System.out.println(">>> Background JMeter process terminated.");
            }
        }
    }

    private void printComparisonRow(String label, long base, long load) {
        long delta = load - base;
        String impact = (delta >= 0 ? "+" : "") + delta + " ms";
        System.out.printf("%-25s | %-12d ms | %-12d ms | %-10s%n", label, base, load, impact);
    }

    private long measureAddServiceLatency() {
        long start = System.currentTimeMillis();
        try {
            WebElement addBtn = driver.findElement(By.cssSelector("tr:has(form[action$='/add']) button[type='submit']"));
            WebElement oldDoc = driver.findElement(By.tagName("html"));
            addBtn.click();
            wait.until(ExpectedConditions.stalenessOf(oldDoc));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btn-secondary")));
        } catch (Exception e) {
            // Fallback if no add button present (e.g. all added)
            driver.navigate().refresh();
        }
        return System.currentTimeMillis() - start;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getNavigationTimings() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script =
                "var timing = window.performance.timing;" +
                "var start = timing.navigationStart;" +
                "return {" +
                "  ttfb: Math.max(0, timing.responseStart - timing.requestStart)," +
                "  domInteractive: Math.max(0, timing.domInteractive - start)," +
                "  pageLoad: Math.max(0, timing.loadEventEnd > 0 ? (timing.loadEventEnd - start) : (timing.responseEnd - start))" +
                "};";
        return (Map<String, Object>) js.executeScript(script);
    }

    private Process startBackgroundJMeterLoad() throws Exception {
        File jmxFile = new File("../JMeter/PetClinic_Performance_Test.jmx");
        if (!jmxFile.exists()) {
            jmxFile = new File("JMeter/PetClinic_Performance_Test.jmx");
        }

        ProcessBuilder pb = new ProcessBuilder(
                "jmeter", "-n",
                "-t", jmxFile.getAbsolutePath(),
                "-JHOST=localhost",
                "-JPORT=8080"
        );
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        return pb.start();
    }
}
