package proyecto.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import proyecto.pages.components.HeaderComponent;

import java.util.List;

/** Pagina del carrito de compras (cart.html). */
public class CartPage extends BasePage {

    private static final By CART_LIST = dataTest("cart-list");
    private static final By ITEM = dataTest("inventory-item");
    private static final By ITEM_NAME = dataTest("inventory-item-name");
    private static final By CHECKOUT = dataTest("checkout");
    private static final By CONTINUE_SHOPPING = dataTest("continue-shopping");

    private final HeaderComponent header;

    public CartPage(WebDriver driver) {
        super(driver);
        this.header = new HeaderComponent(driver);
    }

    public CartPage waitUntilLoaded() {
        visible(CART_LIST);
        return this;
    }

    public HeaderComponent header() {
        return header;
    }

    public boolean isLoaded() {
        return isDisplayed(CART_LIST);
    }

    public int getItemCount() {
        return driver.findElements(ITEM).size();
    }

    public List<String> getItemNames() {
        if (getItemCount() == 0) {
            return List.of();
        }
        return texts(ITEM_NAME);
    }

    public void removeItem(String productName) {
        By remove = dataTest("remove-" + InventoryPage.slug(productName));
        click(remove);
        wait.until(d -> !isPresent(remove));
    }

    public InventoryPage continueShopping() {
        click(CONTINUE_SHOPPING);
        return new InventoryPage(driver).waitUntilLoaded();
    }

    public CheckoutInfoPage checkout() {
        click(CHECKOUT);
        return new CheckoutInfoPage(driver).waitUntilLoaded();
    }

    /** Presiona Checkout sin asumir a que pagina se llega. */
    public void pressCheckout() {
        click(CHECKOUT);
    }
}
