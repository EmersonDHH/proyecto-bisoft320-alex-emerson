package proyecto.pages.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import proyecto.pages.BasePage;
import proyecto.pages.CartPage;
import proyecto.pages.InventoryPage;
import proyecto.pages.LoginPage;

/** Encabezado comun: menu lateral (hamburguesa) e icono del carrito con su contador. */
public class HeaderComponent extends BasePage {

    private static final By MENU_BUTTON = By.id("react-burger-menu-btn");
    private static final By MENU_CLOSE = By.id("react-burger-cross-btn");
    private static final By ALL_ITEMS = dataTest("inventory-sidebar-link");
    private static final By ABOUT = dataTest("about-sidebar-link");
    private static final By LOGOUT = dataTest("logout-sidebar-link");
    private static final By RESET = dataTest("reset-sidebar-link");
    private static final By CART_LINK = dataTest("shopping-cart-link");
    private static final By CART_BADGE = dataTest("shopping-cart-badge");

    public HeaderComponent(WebDriver driver) {
        super(driver);
    }

    private void openMenu() {
        click(MENU_BUTTON);
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGOUT));
    }

    private void closeMenu() {
        click(MENU_CLOSE);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(LOGOUT));
    }

    public LoginPage logout() {
        openMenu();
        click(LOGOUT);
        LoginPage login = new LoginPage(driver);
        waitForUrlContains("saucedemo.com");
        return login;
    }

    public void resetAppState() {
        openMenu();
        click(RESET);
        closeMenu();
    }

    public InventoryPage allItems() {
        openMenu();
        click(ALL_ITEMS);
        return new InventoryPage(driver).waitUntilLoaded();
    }

    public String getAboutLink() {
        openMenu();
        String href = visible(ABOUT).getDomAttribute("href");
        closeMenu();
        return href;
    }

    /** Numero mostrado en el contador del carrito; 0 si el contador no esta visible. */
    public int getCartCount() {
        if (!isDisplayed(CART_BADGE)) {
            return 0;
        }
        return Integer.parseInt(driver.findElement(CART_BADGE).getText().trim());
    }

    public boolean isCartBadgeDisplayed() {
        return isDisplayed(CART_BADGE);
    }

    public CartPage openCart() {
        click(CART_LINK);
        return new CartPage(driver).waitUntilLoaded();
    }

    /** Posicion y tamano del icono del carrito (x, y, ancho, alto). */
    public long[] getCartIconRect() {
        return rect(CART_LINK);
    }
}
