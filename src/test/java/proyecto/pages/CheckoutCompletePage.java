package proyecto.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import proyecto.pages.components.HeaderComponent;

/** Paso final del checkout: confirmacion de la orden (checkout-complete.html). */
public class CheckoutCompletePage extends BasePage {

    private static final By HEADER = dataTest("complete-header");
    private static final By TEXT = dataTest("complete-text");
    private static final By BACK_HOME = dataTest("back-to-products");

    private final HeaderComponent header;

    public CheckoutCompletePage(WebDriver driver) {
        super(driver);
        this.header = new HeaderComponent(driver);
    }

    public CheckoutCompletePage waitUntilLoaded() {
        visible(HEADER);
        return this;
    }

    public HeaderComponent header() {
        return header;
    }

    public String getHeader() {
        return text(HEADER);
    }

    public String getText() {
        return text(TEXT);
    }

    public boolean isLoaded() {
        return isDisplayed(HEADER);
    }

    public InventoryPage backHome() {
        click(BACK_HOME);
        return new InventoryPage(driver).waitUntilLoaded();
    }
}
