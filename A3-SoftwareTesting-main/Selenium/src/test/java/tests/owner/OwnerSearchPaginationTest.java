package tests.owner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Owner Search and Pagination — FR-O3, FR-O4, FR-PG1 (PC-103)")
public class OwnerSearchPaginationTest {
    static WebDriver driver;
    static WebDriverWait wait;
    static final String BASE_URL = "http://localhost:8080";

    @BeforeAll
    static void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("TC-OWN-13 [FR-O3]: Empty search returns all registered owners")
    void emptySearchReturnsAllOwners(){
        driver.get(BASE_URL + "/owners/find");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("lastName"))).clear();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#search-owner-form button[type='submit']"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("owners")));
        List<WebElement> owners = driver.findElements(By.cssSelector("#owners tbody tr"));
        assertFalse(owners.isEmpty(), "Empty search should return all owner list");
    }

    @Test
    @DisplayName("TC-OWN-14 [FR-O3]: Search by non-existent last name shows 'has not been found' error")
    void searchWithNoMatchOwner(){
        driver.get(BASE_URL + "/owners/find");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lastName"))).sendKeys("Nguyen");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#search-owner-form button[type='submit']"))).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".invalid-feedback")));
        WebElement notFoundError = driver.findElement(By.cssSelector(".invalid-feedback"));
        assertTrue(notFoundError.getText().toLowerCase().contains("has not been found"),"Expected not found message to be displayed");
    }

    @Test
    @DisplayName("TC-OWN-15 [FR-O3]: Single-match search navigates directly to owner detail page")
    void searchWithOnlyOneMatchOwner(){
        driver.get(BASE_URL + "/owners/find");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lastName"))).sendKeys("Franklin");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#search-owner-form button[type='submit']"))).click();

        wait.until(ExpectedConditions.urlMatches(".*/owners/\\d+"));
        assertTrue(driver.getCurrentUrl().matches(".*/owners/\\d+"));
    }

    @Test
    @DisplayName("TC-OWN-16 [FR-O3]: Multi-match search displays paginated owner list")
    void searchWithMultipleMatchOwner(){
        driver.get(BASE_URL + "/owners/find");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lastName"))).sendKeys("Davis");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#search-owner-form button[type='submit']"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("owners")));
        List<WebElement> rows = driver.findElements(By.cssSelector("#owners tbody tr"));
        assertTrue(rows.size() > 1, "Multiple owner search should return more than one owner");
    }

    @ParameterizedTest(name = "TC-OWN-17 [FR-O4] case-insensitive search: '{0}' should find={1}")
    @CsvSource({"Davis, true", "davis, true"})
    @DisplayName("TC-OWN-17 [FR-O4]: Owner search is case-insensitive (DEFECT: lowercase 'davis' fails)")
    void searchIsCaseInsensitive(String lastname, boolean shouldFind){
        driver.get(BASE_URL + "/owners/find");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lastName"))).sendKeys(lastname);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#search-owner-form button[type='submit']"))).click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.id("owners")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".invalid-feedback"))
        ));
        List<WebElement> rows = driver.findElements(By.cssSelector("#owners tbody tr"));
        boolean found = rows.size() > 1;
        assertEquals(shouldFind, found, "Search for '" + lastname + "' should find a match");
    }

    @Test
    @DisplayName("TC-OWN-18 [FR-PG1]: Owner list pagination navigates to page 2")
    void testOwnerListPagination(){
        driver.get(BASE_URL + "/owners/find");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("lastName"))).clear();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#search-owner-form button[type='submit']"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("owners")));
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Next"))).click();

        wait.until(ExpectedConditions.urlContains("page=2"));
        assertTrue(driver.getCurrentUrl().contains("page=2"));
    }
}
