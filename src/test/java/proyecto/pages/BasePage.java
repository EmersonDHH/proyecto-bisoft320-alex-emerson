package proyecto.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import proyecto.utils.Config;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Funcionalidad comun a todas las paginas: esperas explicitas y acciones basicas.
 * Las paginas NO contienen asserts: solo localizadores y acciones/consultas.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.getInt("timeout.seconds")));
    }

    /** Localizador por atributo data-test (el mas estable en Swag Labs). */
    protected static By dataTest(String value) {
        return By.cssSelector("[data-test=\"" + value + "\"]");
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement element = visible(locator);
        element.clear();
        if (text != null && !text.isEmpty()) {
            element.sendKeys(text);
        }
    }

    protected String text(By locator) {
        return visible(locator).getText().trim();
    }

    protected String value(By locator) {
        return visible(locator).getDomProperty("value");
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    protected boolean isDisplayed(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        return !elements.isEmpty() && elements.get(0).isDisplayed();
    }

    protected List<String> texts(By locator) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        return driver.findElements(locator).stream().map(e -> e.getText().trim()).toList();
    }

    protected List<String> attributes(By locator, String attribute) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        return driver.findElements(locator).stream().map(e -> e.getDomAttribute(attribute)).toList();
    }

    protected void waitForUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    // ---- Consultas publicas comunes ----

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public void goBack() {
        driver.navigate().back();
    }

    public void refresh() {
        driver.navigate().refresh();
    }

    /** Si aparece una alerta JavaScript en el tiempo indicado, devuelve su texto y la acepta. */
    public Optional<String> acceptAlertIfPresent(Duration timeout) {
        try {
            Alert alert = new WebDriverWait(driver, timeout).until(ExpectedConditions.alertIsPresent());
            String message = alert.getText();
            alert.accept();
            return Optional.of(message);
        } catch (org.openqa.selenium.TimeoutException e) {
            return Optional.empty();
        }
    }

    /** Informacion de layout de un elemento (x, y, ancho, alto) para pruebas visuales. */
    protected long[] rect(By locator) {
        WebElement element = visible(locator);
        Object r = ((JavascriptExecutor) driver).executeScript(
                "const b=arguments[0].getBoundingClientRect();return [b.x,b.y,b.width,b.height];", element);
        List<?> list = (List<?>) r;
        return list.stream().mapToLong(v -> Math.round(((Number) v).doubleValue())).toArray();
    }
}
