package proyecto.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import proyecto.pages.components.HeaderComponent;

/** Pagina de detalle de un producto (inventory-item.html?id=N). */
public class ProductDetailPage extends BasePage {

    private static final By CONTAINER = dataTest("inventory-container");
    private static final By NAME = dataTest("inventory-item-name");
    private static final By DESC = dataTest("inventory-item-desc");
    private static final By PRICE = dataTest("inventory-item-price");
    private static final By BACK = dataTest("back-to-products");
    private static final By ADD = dataTest("add-to-cart");
    private static final By REMOVE = dataTest("remove");

    private final HeaderComponent header;

    public ProductDetailPage(WebDriver driver) {
        super(driver);
        this.header = new HeaderComponent(driver);
    }

    public ProductDetailPage waitUntilLoaded() {
        visible(BACK);
        visible(CONTAINER);
        return this;
    }

    public HeaderComponent header() {
        return header;
    }

    public String getName() {
        return text(NAME);
    }

    public String getDescription() {
        return text(DESC);
    }

    public double getPrice() {
        return Double.parseDouble(text(PRICE).replace("$", ""));
    }

    public void addToCart() {
        click(ADD);
        visible(REMOVE);
    }

    public InventoryPage backToProducts() {
        click(BACK);
        return new InventoryPage(driver).waitUntilLoaded();
    }
}
