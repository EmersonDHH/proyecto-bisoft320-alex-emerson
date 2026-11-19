package proyecto.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import proyecto.utils.Config;

/** Pagina de inicio de sesion (https://www.saucedemo.com/). */
public class LoginPage extends BasePage {

    private static final By USERNAME = dataTest("username");
    private static final By PASSWORD = dataTest("password");
    private static final By LOGIN_BUTTON = dataTest("login-button");
    private static final By ERROR = dataTest("error");
    private static final By ERROR_CLOSE = dataTest("error-button");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(Config.baseUrl());
        visible(LOGIN_BUTTON);
        return this;
    }

    /** Abre una ruta interna directamente (por ejemplo /inventory.html) sin pasar por el login. */
    public LoginPage openPath(String path) {
        driver.get(Config.baseUrl() + path);
        visible(LOGIN_BUTTON);
        return this;
    }

    /** Escribe credenciales y presiona Login, sin asumir el resultado. */
    public void submitCredentials(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
    }

    /** Inicia sesion esperando llegar al inventario. */
    public InventoryPage loginAs(String username, String password) {
        submitCredentials(username, password);
        return new InventoryPage(driver).waitUntilLoaded();
    }

    public LoginPage waitUntilLoaded() {
        visible(LOGIN_BUTTON);
        return this;
    }

    public boolean isLoaded() {
        return isDisplayed(LOGIN_BUTTON);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(ERROR);
    }

    public String getErrorMessage() {
        return text(ERROR);
    }

    public void closeError() {
        click(ERROR_CLOSE);
        wait.until(d -> !isPresent(ERROR));
    }

    public String getUsernameValue() {
        return value(USERNAME);
    }
}
