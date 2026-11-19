package proyecto.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import proyecto.base.BaseTest;
import proyecto.pages.CartPage;
import proyecto.pages.InventoryPage;
import proyecto.pages.ProductDetailPage;
import proyecto.utils.TestData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF07 Carrito de compras (agregar, quitar, contador) y RF11 Reset App State. */
@DisplayName("Carrito")
class CartTest extends BaseTest {

    @Test
    @Tag("regresion")
    @DisplayName("TC24 Frontera 0: carrito vacio no muestra contador")
    void carritoVacioSinContador() {
        InventoryPage inventory = loginComo(TestData.STANDARD);

        assertAll(
                () -> assertFalse(inventory.header().isCartBadgeDisplayed()),
                () -> assertEquals(0, inventory.header().openCart().getItemCount()));
    }

    @Test
    @Tag("regresion")
    @DisplayName("TC25 Frontera 1: agregar un producto muestra contador 1")
    void agregarUnProducto() {
        InventoryPage inventory = loginComo(TestData.STANDARD);

        inventory.addToCart(TestData.BACKPACK);

        assertAll(
                () -> assertEquals(1, inventory.header().getCartCount()),
                () -> assertEquals("Remove", inventory.getButtonText(TestData.BACKPACK)));
        CartPage cart = inventory.header().openCart();
        assertEquals(List.of(TestData.BACKPACK), cart.getItemNames());
    }

    @Test
    @Tag("regresion")
    @DisplayName("TC26 Frontera maxima: agregar los 6 productos muestra contador 6")
    void agregarSeisProductos() {
        InventoryPage inventory = loginComo(TestData.STANDARD);

        inventory.addAllToCart();

        assertEquals(6, inventory.header().getCartCount());
        assertEquals(6, inventory.header().openCart().getItemCount());
    }

    @Test
    @Tag("regresion")
    @DisplayName("TC27 Frontera maximo-1: quitar un producto del carrito con 6 deja 5")
    void quitarProductoDelCarrito() {
        InventoryPage inventory = loginComo(TestData.STANDARD);
        inventory.addAllToCart();
        CartPage cart = inventory.header().openCart();

        cart.removeItem(TestData.ONESIE);

        assertAll(
                () -> assertEquals(5, cart.getItemCount()),
                () -> assertEquals(5, cart.header().getCartCount()),
                () -> assertFalse(cart.getItemNames().contains(TestData.ONESIE)));
    }

    @Test
    @Tag("regresion")
    @DisplayName("TC28 El carrito se conserva al navegar entre detalle, carrito e inventario")
    void carritoPersisteAlNavegar() {
        InventoryPage inventory = loginComo(TestData.STANDARD);
        ProductDetailPage detalle = inventory.openProduct(TestData.FLEECE);
        detalle.addToCart();

        CartPage cart = detalle.header().openCart();
        InventoryPage deVuelta = cart.continueShopping();
        deVuelta.refresh();

        assertAll(
                () -> assertEquals(1, deVuelta.header().getCartCount()),
                () -> assertEquals("Remove", deVuelta.getButtonText(TestData.FLEECE)));
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC29 problem_user puede agregar los 6 productos al carrito")
    void problemUserAgregaSeis() {
        InventoryPage inventory = loginComo(TestData.PROBLEM);

        inventory.addAllToCart();

        assertEquals(6, inventory.header().getCartCount(), "No todos los productos se agregaron al carrito");
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC30 error_user puede agregar los 6 productos al carrito")
    void errorUserAgregaSeis() {
        InventoryPage inventory = loginComo(TestData.ERROR);

        inventory.addAllToCart();

        assertEquals(6, inventory.header().getCartCount(), "No todos los productos se agregaron al carrito");
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC40 Reset App State vacia el carrito y restablece los botones")
    void resetAppState() {
        InventoryPage inventory = loginComo(TestData.STANDARD);
        inventory.addToCart(TestData.BACKPACK);
        inventory.addToCart(TestData.BIKE_LIGHT);

        inventory.header().resetAppState();

        assertAll(
                () -> assertEquals(0, inventory.header().getCartCount(), "El contador debe desaparecer"),
                () -> assertEquals("Add to cart", inventory.getButtonText(TestData.BACKPACK),
                        "El boton del producto debe volver a 'Add to cart'"),
                () -> assertEquals("Add to cart", inventory.getButtonText(TestData.BIKE_LIGHT),
                        "El boton del producto debe volver a 'Add to cart'"));
    }
}
