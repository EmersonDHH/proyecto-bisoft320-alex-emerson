package proyecto.base;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import proyecto.pages.CartPage;
import proyecto.pages.CheckoutInfoPage;
import proyecto.pages.InventoryPage;
import proyecto.pages.LoginPage;
import proyecto.utils.DriverFactory;
import proyecto.utils.ReportExtension;

/**
 * Clase base de todos los tests: abre un navegador nuevo antes de cada test y lo
 * cierra al terminar. Cada test es independiente (no comparte sesion ni estado).
 */
@ExtendWith(ReportExtension.class)
public abstract class BaseTest {

    public static final String PASSWORD = "secret_sauce";

    protected WebDriver driver;
    protected LoginPage loginPage;

    @BeforeEach
    void abrirNavegador() {
        driver = DriverFactory.create();
        loginPage = new LoginPage(driver);
        loginPage.open();
    }

    @AfterEach
    void cerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Usado por ReportExtension para tomar la captura de pantalla cuando un test falla. */
    public WebDriver getDriver() {
        return driver;
    }

    // ---- Atajos de preparacion (precondiciones), no contienen verificaciones ----

    protected InventoryPage loginComo(String usuario) {
        return loginPage.loginAs(usuario, PASSWORD);
    }

    protected CartPage carritoCon(String usuario, String... productos) {
        InventoryPage inventory = loginComo(usuario);
        for (String producto : productos) {
            inventory.addToCart(producto);
        }
        return inventory.header().openCart();
    }

    protected CheckoutInfoPage checkoutCon(String usuario, String... productos) {
        return carritoCon(usuario, productos).checkout();
    }
}
