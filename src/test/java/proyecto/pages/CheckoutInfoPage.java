package proyecto.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Paso 1 del checkout: datos del comprador (checkout-step-one.html). */
public class CheckoutInfoPage extends BasePage {

    private static final By FIRST_NAME = dataTest("firstName");
    private static final By LAST_NAME = dataTest("lastName");
    private static final By POSTAL_CODE = dataTest("postalCode");
    private static final By CONTINUE = dataTest("continue");
    private static final By CANCEL = dataTest("cancel");
    private static final By ERROR = dataTest("error");

    public CheckoutInfoPage(WebDriver driver) {
        super(driver);
    }

    public CheckoutInfoPage waitUntilLoaded() {
        visible(FIRST_NAME);
        return this;
    }

    public boolean isLoaded() {
        return isDisplayed(FIRST_NAME);
    }

    public void fillForm(String firstName, String lastName, String postalCode) {
        type(FIRST_NAME, firstName);
        type(LAST_NAME, lastName);
        type(POSTAL_CODE, postalCode);
    }

    /** Presiona Continue sin asumir el resultado (puede quedar en esta pagina con error). */
    public void pressContinue() {
        click(CONTINUE);
    }

    public CheckoutOverviewPage continueToOverview(String firstName, String lastName, String postalCode) {
        fillForm(firstName, lastName, postalCode);
        click(CONTINUE);
        return new CheckoutOverviewPage(driver).waitUntilLoaded();
    }

    public CartPage cancel() {
        click(CANCEL);
        return new CartPage(driver).waitUntilLoaded();
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(ERROR);
    }

    public String getErrorMessage() {
        return text(ERROR);
    }

    public String getFirstNameValue() {
        return value(FIRST_NAME);
    }

    public String getLastNameValue() {
        return value(LAST_NAME);
    }

    public String getPostalCodeValue() {
        return value(POSTAL_CODE);
    }
}
