package proyecto.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Datos de prueba compartidos: usuarios y catalogo de referencia (oraculo). */
public final class TestData {

    private TestData() {
    }

    public static final String STANDARD = "standard_user";
    public static final String LOCKED = "locked_out_user";
    public static final String PROBLEM = "problem_user";
    public static final String PERFORMANCE = "performance_glitch_user";
    public static final String ERROR = "error_user";
    public static final String VISUAL = "visual_user";

    public static final String BACKPACK = "Sauce Labs Backpack";
    public static final String BIKE_LIGHT = "Sauce Labs Bike Light";
    public static final String BOLT_TSHIRT = "Sauce Labs Bolt T-Shirt";
    public static final String FLEECE = "Sauce Labs Fleece Jacket";
    public static final String ONESIE = "Sauce Labs Onesie";
    public static final String RED_TSHIRT = "Test.allTheThings() T-Shirt (Red)";

    /** Catalogo oficial (oraculo) con el precio correcto de cada producto. */
    public static final Map<String, Double> CATALOGO = new LinkedHashMap<>();

    static {
        CATALOGO.put(BACKPACK, 29.99);
        CATALOGO.put(BIKE_LIGHT, 9.99);
        CATALOGO.put(BOLT_TSHIRT, 15.99);
        CATALOGO.put(FLEECE, 49.99);
        CATALOGO.put(ONESIE, 7.99);
        CATALOGO.put(RED_TSHIRT, 15.99);
    }

    public static List<String> productos() {
        return List.copyOf(CATALOGO.keySet());
    }

    /** Tasa de impuesto aplicada por Swag Labs en el resumen de compra. */
    public static final double TAX_RATE = 0.08;

    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
