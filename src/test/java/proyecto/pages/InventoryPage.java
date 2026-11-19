package proyecto.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import proyecto.pages.components.HeaderComponent;

import java.util.List;

/** Pagina de inventario (listado de productos). */
public class InventoryPage extends BasePage {

    private static final By TITLE = dataTest("title");
    private static final By INVENTORY_LIST = dataTest("inventory-list");
    private static final By ITEM = dataTest("inventory-item");
    private static final By ITEM_NAME = dataTest("inventory-item-name");
    private static final By ITEM_DESC = dataTest("inventory-item-desc");
    private static final By ITEM_PRICE = dataTest("inventory-item-price");
    private static final By ITEM_IMAGE = By.cssSelector("[data-test=\"inventory-list\"] img.inventory_item_img");
    private static final By ADD_BUTTONS = By.cssSelector("button[data-test^=\"add-to-cart\"]");
    private static final By SORT = dataTest("product-sort-container");
    private static final By ACTIVE_SORT = dataTest("active-option");

    private final HeaderComponent header;

    public InventoryPage(WebDriver driver) {
        super(driver);
        this.header = new HeaderComponent(driver);
    }

    public InventoryPage waitUntilLoaded() {
        visible(INVENTORY_LIST);
        return this;
    }

    public HeaderComponent header() {
        return header;
    }

    public boolean isLoaded() {
        return isDisplayed(INVENTORY_LIST);
    }

    public String getTitle() {
        return text(TITLE);
    }

    public int getProductCount() {
        return driver.findElements(ITEM).size();
    }

    public List<String> getProductNames() {
        return texts(ITEM_NAME);
    }

    public List<String> getProductDescriptions() {
        return texts(ITEM_DESC);
    }

    /** Precios como numeros (sin el simbolo $). */
    public List<Double> getProductPrices() {
        return texts(ITEM_PRICE).stream().map(p -> Double.parseDouble(p.replace("$", ""))).toList();
    }

    public List<String> getImageSources() {
        return attributes(ITEM_IMAGE, "src");
    }

    /** Convierte el nombre visible del producto en el sufijo usado por data-test. */
    public static String slug(String productName) {
        return productName.toLowerCase().replace(" ", "-");
    }

    public void addToCart(String productName) {
        click(dataTest("add-to-cart-" + slug(productName)));
    }

    public void removeFromCart(String productName) {
        click(dataTest("remove-" + slug(productName)));
    }

    /** Texto del boton de un producto ("Add to cart" o "Remove"). */
    public String getButtonText(String productName) {
        By add = dataTest("add-to-cart-" + slug(productName));
        By remove = dataTest("remove-" + slug(productName));
        return isPresent(add) ? text(add) : text(remove);
    }

    /** Presiona todos los botones "Add to cart" visibles, uno por uno. */
    public void addAllToCart() {
        List<String> ids = driver.findElements(ADD_BUTTONS).stream()
                .map(e -> e.getDomAttribute("data-test")).toList();
        for (String id : ids) {
            List<WebElement> button = driver.findElements(dataTest(id));
            if (!button.isEmpty()) {
                button.get(0).click();
            }
        }
    }

    /** Ordena el listado. Valores: az, za, lohi, hilo. */
    public void sortBy(String value) {
        new Select(visible(SORT)).selectByValue(value);
    }

    public String getActiveSortLabel() {
        return text(ACTIVE_SORT);
    }

    public ProductDetailPage openProduct(String productName) {
        List<WebElement> names = driver.findElements(ITEM_NAME);
        for (WebElement name : names) {
            if (name.getText().trim().equals(productName)) {
                name.click();
                return new ProductDetailPage(driver).waitUntilLoaded();
            }
        }
        throw new IllegalArgumentException("Producto no encontrado en el inventario: " + productName);
    }
}
