package proyecto.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import proyecto.base.BaseTest;
import proyecto.pages.InventoryPage;
import proyecto.utils.Config;
import proyecto.utils.TestData;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF01 Autenticacion, RF02 Validacion del login, RF03 Control de acceso, RNF01 Tiempo de respuesta. */
@DisplayName("Inicio de sesion")
class LoginTest extends BaseTest {

    /**
     * Tabla de decision del login (TC01-TC09). Los datos se leen de login_cases.csv.
     */
    @Tag("regresion")
    @ParameterizedTest(name = "{0} {1}")
    @CsvFileSource(resources = "/login_cases.csv", numLinesToSkip = 1)
    @DisplayName("TC01-TC09 Tabla de decision del login")
    void tablaDeDecisionLogin(String id, String descripcion, String usuario, String clave,
                              String resultado, String mensaje) {
        loginPage.submitCredentials(usuario == null ? "" : usuario, clave == null ? "" : clave);

        if ("INVENTARIO".equals(resultado)) {
            InventoryPage inventory = new InventoryPage(driver).waitUntilLoaded();
            assertAll(
                    () -> assertTrue(inventory.getCurrentUrl().endsWith("/inventory.html"), "Debe llegar al inventario"),
                    () -> assertEquals("Products", inventory.getTitle()));
        } else {
            assertAll(
                    () -> assertTrue(loginPage.isErrorDisplayed(), "Debe mostrarse el mensaje de error"),
                    () -> assertEquals(mensaje, loginPage.getErrorMessage()),
                    () -> assertTrue(loginPage.isLoaded(), "Debe permanecer en la pagina de login"));
        }
    }

    @Test
    @Tag("regresion")
    @DisplayName("TC10 Cerrar el mensaje de error del login")
    void cerrarMensajeDeError() {
        loginPage.submitCredentials("", "");
        assertTrue(loginPage.isErrorDisplayed());

        loginPage.closeError();

        assertFalse(loginPage.isErrorDisplayed(), "El mensaje de error debe desaparecer al cerrarlo");
    }

    @Test
    @Tag("regresion")
    @DisplayName("TC11 Acceso directo al inventario sin sesion")
    void accesoDirectoSinSesion() {
        loginPage.openPath("/inventory.html");

        assertAll(
                () -> assertTrue(loginPage.isLoaded(), "Debe mostrarse el formulario de login"),
                () -> assertEquals("Epic sadface: You can only access '/inventory.html' when you are logged in.",
                        loginPage.getErrorMessage()));
    }

    @Test
    @Tag("defecto")
    @DisplayName("TC12 Tiempo de login de performance_glitch_user dentro del umbral (RNF01)")
    void tiempoDeLoginPerformanceGlitch() {
        long umbral = Config.getInt("login.max.millis");

        long inicio = System.nanoTime();
        InventoryPage inventory = loginComo(TestData.PERFORMANCE);
        long ms = (System.nanoTime() - inicio) / 1_000_000;

        assertTrue(inventory.isLoaded());
        assertTrue(ms <= umbral, "El login tardo " + ms + " ms; el maximo permitido es " + umbral + " ms");
    }

    @Test
    @Tag("regresion")
    @DisplayName("TC39 Logout regresa al login y bloquea el boton Atras")
    void logoutYBotonAtras() {
        InventoryPage inventory = loginComo(TestData.STANDARD);

        inventory.header().logout();
        assertTrue(loginPage.isLoaded(), "Tras el logout debe mostrarse el login");
        assertEquals("", loginPage.getUsernameValue(), "El campo usuario debe quedar vacio");

        loginPage.goBack();
        loginPage.waitUntilLoaded();
        assertAll(
                () -> assertTrue(loginPage.isErrorDisplayed(), "Sin sesion no se debe poder volver al inventario"),
                () -> assertEquals("Epic sadface: You can only access '/inventory.html' when you are logged in.",
                        loginPage.getErrorMessage()));
    }
}
