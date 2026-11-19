package proyecto.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

/** Paso 2 del checkout: resumen con subtotal, impuesto y total (checkout-step-two.html). */
public class CheckoutOverviewPage extends BasePage {

    private static final By SUMMARY = dataTest("checkout-summary-container");
    private static final By ITEM_PRICE = dataTest("inventory-item-price");
    private static final By SUBTOTAL = dataTest("subtotal-label");
    private static final By TAX = dataTest("tax-label");
    private static final By TOTAL = dataTest("total-label");
    private static final By FINISH = dataTest("finish");
    private static final By CANCEL = dataTest("cancel");

    public CheckoutOverviewPage(WebDriver driver) {
        super(driver);
    }

    public CheckoutOverviewPage waitUntilLoaded() {
        visible(SUMMARY);
        return this;
    }

    private static double amount(String label) {
        return Double.parseDouble(label.substring(label.indexOf('$') + 1));
    }

    public List<Double> getItemPrices() {
        return texts(ITEM_PRICE).stream().map(p -> Double.parseDouble(p.replace("$", ""))).toList();
    }

    public double getSubtotal() {
        return amount(text(SUBTOTAL));
    }

    public double getTax() {
        return amount(text(TAX));
    }

    public double getTotal() {
        return amount(text(TOTAL));
    }

    public CheckoutCompletePage finish() {
        click(FINISH);
        return new CheckoutCompletePage(driver).waitUntilLoaded();
    }

    /** Presiona Finish sin asumir el resultado. */
    public void pressFinish() {
        click(FINISH);
    }

    public InventoryPage cancel() {
        click(CANCEL);
        return new InventoryPage(driver).waitUntilLoaded();
    }
}
