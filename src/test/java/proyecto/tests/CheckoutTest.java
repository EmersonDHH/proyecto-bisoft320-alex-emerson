package proyecto.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import proyecto.base.BaseTest;
import proyecto.pages.CartPage;
import proyecto.pages.CheckoutCompletePage;
import proyecto.pages.CheckoutInfoPage;
import proyecto.pages.CheckoutOverviewPage;
import proyecto.pages.InventoryPage;
import proyecto.utils.TestData;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF08 Datos del comprador, RF09 Resumen con impuestos, RF10 Confirmacion de compra. */
@DisplayName("Compra (checkout)")
class CheckoutTest extends BaseTest {

    /** Tabla de decision de campos obligatorios del comprador (TC31-TC33), datos en checkout_cases.csv. */
    @Tag("regresion")
    @ParameterizedTest(name = "{0} {1}")
    @CsvFileSource(resources = "/checkout_cases.csv", numLinesToSkip = 1)
    @DisplayName("TC31-TC33 Campos obligatorios del comprador")
    void camposObligatorios(String id, String descripcion, String nombre, String apellido,
                            String postal, String mensaje) {
        CheckoutInfoPage info = checkoutCon(TestData.STANDARD, TestData.BACKPACK);

        info.fillForm(nvl(nombre), nvl(apellido), nvl(postal));
        info.pressContinue();

        assertAll(
                () -> assertTrue(info.isErrorDisplayed(), "Debe mostrarse un error"),
                () -> assertEquals(mensaje, info.getErrorMessage()),
                () -> assertTrue(info.getCurrentUrl().contains("checkout-step-one"), "No debe avanzar al resumen"));
    }

    @Test
    @Tag("regresion")
    @DisplayName("TC34 Compra completa con calculo correcto de impuesto (8%) y total")
    void compraCompleta() {
        CheckoutInfoPage info = checkoutCon(TestData.STANDARD, TestData.BACKPACK, TestData.BOLT_TSHIRT);

        CheckoutOverviewPage resumen = info.continueToOverview("Ana", "Perez", "10101");
        double subtotalEsperado = TestData.redondear(resumen.getItemPrices().stream().mapToDouble(Double::doubleValue).sum());
        double impuestoEsperado = TestData.redondear(subtotalEsperado * TestData.TAX_RATE);

        assertAll(
                () -> assertEquals(45.98, subtotalEsperado),
                () -> assertEquals(subtotalEsperado, resumen.getSubtotal(), "Subtotal"),
                () -> assertEquals(impuestoEsperado, resumen.getTax(), "Impuesto 8%"),
                () -> assertEquals(TestData.redondear(subtotalEsperado + impuestoEsperado), resumen.getTotal(), "Total"));

        CheckoutCompletePage confirmacion = resumen.finish();
        assertAll(
                () -> assertEquals("Thank you for your order!", confirmacion.getHeader()),
                () -> assertEquals(0, confirmacion.header().getCartCount(), "El carrito debe quedar vacio"));
    }

    @Test
    @Tag("regresion")
    @DisplayName("TC35 Cancelar en el resumen regresa al inventario y conserva el carrito")
    void cancelarEnResumen() {
        CheckoutOverviewPage resumen = checkoutCon(TestData.STANDARD, TestData.ONESIE)
                .continueToOverview("Ana", "Perez", "10101");

        InventoryPage inventory = resumen.cancel();

        assertAll(
                () -> assertTrue(inventory.getCurrentUrl().endsWith("/inventory.html")),
                () -> assertEquals(1, inventory.header().getCartCount()));
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC36 No se permite iniciar el checkout con el carrito vacio")
    void checkoutConCarritoVacio() {
        CartPage cart = loginComo(TestData.STANDARD).header().openCart();
        assertEquals(0, cart.getItemCount());

        cart.pressCheckout();

        assertTrue(cart.isLoaded() && !cart.getCurrentUrl().contains("checkout-step-one"),
                "Con el carrito vacio la aplicacion permitio avanzar a: " + cart.getCurrentUrl());
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC37 problem_user puede escribir su apellido en el checkout")
    void problemUserApellido() {
        CheckoutInfoPage info = checkoutCon(TestData.PROBLEM, TestData.BACKPACK);

        info.fillForm("Ana", "Perez", "10101");

        assertAll(
                () -> assertEquals("Ana", info.getFirstNameValue(), "Nombre"),
                () -> assertEquals("Perez", info.getLastNameValue(), "Apellido"));
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC38 error_user puede finalizar la compra")
    void errorUserFinalizaCompra() {
        CheckoutInfoPage info = checkoutCon(TestData.ERROR, TestData.BACKPACK);
        info.fillForm("Ana", "Perez", "10101");
        info.pressContinue();
        CheckoutOverviewPage resumen = new CheckoutOverviewPage(driver).waitUntilLoaded();

        resumen.pressFinish();
        resumen.acceptAlertIfPresent(Duration.ofSeconds(1));

        assertTrue(resumen.getCurrentUrl().contains("checkout-complete"),
                "Tras presionar Finish la aplicacion permanece en: " + resumen.getCurrentUrl());
        assertTrue(new CheckoutCompletePage(driver).isLoaded(), "Debe mostrarse la confirmacion de la orden");
    }

    private static String nvl(String valor) {
        return valor == null ? "" : valor;
    }
}
