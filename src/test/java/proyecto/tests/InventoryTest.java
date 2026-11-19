package proyecto.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import proyecto.base.BaseTest;
import proyecto.pages.InventoryPage;
import proyecto.pages.ProductDetailPage;
import proyecto.utils.TestData;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF04 Catalogo, RF05 Ordenamiento, RF06 Detalle de producto. */
@DisplayName("Inventario")
class InventoryTest extends BaseTest {

    @Test
    @Tag("regresion")
    @DisplayName("TC13 El catalogo muestra los 6 productos con nombre, descripcion y precio")
    void catalogoCompleto() {
        InventoryPage inventory = loginComo(TestData.STANDARD);

        assertAll(
                () -> assertEquals(6, inventory.getProductCount()),
                () -> assertEquals(new HashSet<>(TestData.productos()), new HashSet<>(inventory.getProductNames())),
                () -> assertTrue(inventory.getProductDescriptions().stream().noneMatch(String::isBlank)),
                () -> assertEquals(6, inventory.getImageSources().size()));
    }

    /** Particion de equivalencias sobre las 4 opciones de ordenamiento (TC14-TC17). */
    @Tag("regresion")
    @ParameterizedTest(name = "{0} Ordenar por {1}")
    @CsvSource({
            "TC14, az,   Name (A to Z)",
            "TC15, za,   Name (Z to A)",
            "TC16, lohi, Price (low to high)",
            "TC17, hilo, Price (high to low)"
    })
    @DisplayName("TC14-TC17 Ordenamiento del inventario")
    void ordenamiento(String id, String opcion, String etiqueta) {
        InventoryPage inventory = loginComo(TestData.STANDARD);

        inventory.sortBy(opcion);

        assertEquals(etiqueta, inventory.getActiveSortLabel());
        verificarOrden(inventory, opcion);
    }

    @Test
    @Tag("regresion")
    @DisplayName("TC18 El detalle del producto coincide con el listado")
    void detalleCoincideConListado() {
        InventoryPage inventory = loginComo(TestData.STANDARD);

        ProductDetailPage detalle = inventory.openProduct(TestData.BACKPACK);

        assertAll(
                () -> assertEquals(TestData.BACKPACK, detalle.getName()),
                () -> assertEquals(29.99, detalle.getPrice()),
                () -> assertTrue(detalle.getCurrentUrl().contains("inventory-item.html?id=")));
        InventoryPage deVuelta = detalle.backToProducts();
        assertTrue(deVuelta.isLoaded(), "Back to products debe regresar al inventario");
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC19 problem_user ve una imagen distinta para cada producto")
    void problemUserImagenesUnicas() {
        InventoryPage inventory = loginComo(TestData.PROBLEM);

        List<String> imagenes = inventory.getImageSources();

        assertEquals(6, new HashSet<>(imagenes).size(),
                "Se esperaban 6 imagenes distintas, se obtuvieron: " + new HashSet<>(imagenes));
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC20 problem_user puede ordenar por precio de menor a mayor")
    void problemUserOrdenamiento() {
        InventoryPage inventory = loginComo(TestData.PROBLEM);

        inventory.sortBy("lohi");

        verificarOrden(inventory, "lohi");
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC21 error_user ordena Z-A sin mensajes de error")
    void errorUserOrdenamientoSinError() {
        InventoryPage inventory = loginComo(TestData.ERROR);

        inventory.sortBy("za");
        Optional<String> alerta = inventory.acceptAlertIfPresent(Duration.ofSeconds(2));

        assertTrue(alerta.isEmpty(), "Aparecio una alerta inesperada: " + alerta.orElse(""));
        verificarOrden(inventory, "za");
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC22 visual_user ve los precios oficiales del catalogo")
    void visualUserPreciosCorrectos() {
        InventoryPage inventory = loginComo(TestData.VISUAL);

        List<String> nombres = inventory.getProductNames();
        List<Double> precios = inventory.getProductPrices();
        List<String> diferencias = new ArrayList<>();
        for (int i = 0; i < nombres.size(); i++) {
            double esperado = TestData.CATALOGO.get(nombres.get(i));
            if (esperado != precios.get(i)) {
                diferencias.add(nombres.get(i) + ": esperado $" + esperado + ", mostrado $" + precios.get(i));
            }
        }

        assertTrue(diferencias.isEmpty(), "Precios incorrectos: " + diferencias);
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC23 problem_user abre el detalle del producto seleccionado")
    void problemUserDetalleCorrecto() {
        InventoryPage inventory = loginComo(TestData.PROBLEM);

        ProductDetailPage detalle = inventory.openProduct(TestData.BACKPACK);

        assertEquals(TestData.BACKPACK, detalle.getName(), "El detalle muestra otro producto");
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC42 visual_user: icono del carrito alineado igual que para standard_user")
    void visualUserIconoCarritoAlineado() {
        InventoryPage estandar = loginComo(TestData.STANDARD);
        long[] referencia = estandar.header().getCartIconRect();
        estandar.header().logout().waitUntilLoaded();

        long[] visual = loginComo(TestData.VISUAL).header().getCartIconRect();

        assertAll(
                () -> assertTrue(Math.abs(referencia[0] - visual[0]) <= 2, "Posicion X distinta: " + referencia[0] + " vs " + visual[0]),
                () -> assertTrue(Math.abs(referencia[1] - visual[1]) <= 2, "Posicion Y distinta: " + referencia[1] + " vs " + visual[1]));
    }

    // ---- Oraculo de ordenamiento (verificacion, por eso vive en el test y no en la pagina) ----

    private void verificarOrden(InventoryPage inventory, String opcion) {
        List<String> nombres = inventory.getProductNames();
        List<Double> precios = inventory.getProductPrices();
        switch (opcion) {
            case "az" -> assertEquals(ordenado(nombres, Comparator.naturalOrder()), nombres, "Orden A-Z incorrecto");
            case "za" -> assertEquals(ordenado(nombres, Comparator.reverseOrder()), nombres, "Orden Z-A incorrecto");
            case "lohi" -> assertEquals(ordenado(precios, Comparator.naturalOrder()), precios, "Orden de precio ascendente incorrecto");
            case "hilo" -> assertEquals(ordenado(precios, Comparator.reverseOrder()), precios, "Orden de precio descendente incorrecto");
            default -> assertFalse(true, "Opcion desconocida " + opcion);
        }
    }

    private static <T> List<T> ordenado(List<T> lista, Comparator<? super T> comparador) {
        List<T> copia = new ArrayList<>(lista);
        copia.sort(comparador);
        return copia;
    }
}
