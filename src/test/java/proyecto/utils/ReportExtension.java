package proyecto.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import proyecto.base.BaseTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extension de JUnit 5 que genera el reporte HTML (ExtentReports) en
 * target/reports/reporte-ejecucion.html y adjunta una captura de pantalla cuando
 * un test falla. La captura se toma ANTES de @AfterEach, mientras el navegador sigue abierto.
 */
public class ReportExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, TestWatcher {

    private static final Path REPORT_DIR = Path.of("target", "reports");
    private static final Path SHOTS_DIR = REPORT_DIR.resolve("screenshots");
    private static final ExtentReports EXTENT = crearReporte();
    private static final Map<String, ExtentTest> TESTS = new ConcurrentHashMap<>();
    private static final Map<String, Long> INICIO = new ConcurrentHashMap<>();

    private static ExtentReports crearReporte() {
        try {
            Files.createDirectories(SHOTS_DIR);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_DIR.resolve("reporte-ejecucion.html").toString());
        spark.config().setDocumentTitle("BISOFT-32 - Reporte de ejecucion");
        spark.config().setReportName("Swag Labs - Pruebas automatizadas con Selenium y POM");
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setEncoding("UTF-8");
        ExtentReports extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Aplicacion", Config.baseUrl());
        extent.setSystemInfo("Navegador", Config.browser() + (Config.getBool("headless") ? " (headless)" : ""));
        extent.setSystemInfo("Sistema operativo", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        extent.setSystemInfo("Java", System.getProperty("java.version"));
        Runtime.getRuntime().addShutdownHook(new Thread(extent::flush));
        return extent;
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        String clase = context.getRequiredTestClass().getSimpleName();
        ExtentTest test = EXTENT.createTest(context.getDisplayName(), clase);
        test.assignCategory(clase);
        context.getTags().forEach(test::assignCategory);
        TESTS.put(context.getUniqueId(), test);
        INICIO.put(context.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        ExtentTest test = TESTS.get(context.getUniqueId());
        long duracion = System.currentTimeMillis() - INICIO.getOrDefault(context.getUniqueId(), System.currentTimeMillis());
        test.info("Duracion: " + duracion + " ms");
        Optional<Throwable> error = context.getExecutionException();
        if (error.isPresent()) {
            String captura = tomarCaptura(context);
            if (captura != null) {
                test.fail(error.get().getMessage(),
                        MediaEntityBuilder.createScreenCaptureFromBase64String(captura, "Captura al fallar").build());
            } else {
                test.fail(error.get());
            }
        } else {
            test.pass("Resultado obtenido igual al esperado");
        }
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        EXTENT.createTest(context.getDisplayName()).log(Status.SKIP, reason.orElse("Deshabilitado"));
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        ExtentTest test = TESTS.get(context.getUniqueId());
        if (test != null) {
            test.skip("Bloqueado: " + cause.getMessage());
        }
    }

    private String tomarCaptura(ExtensionContext context) {
        Object instancia = context.getRequiredTestInstance();
        if (!(instancia instanceof BaseTest base) || base.getDriver() == null) {
            return null;
        }
        WebDriver driver = base.getDriver();
        try {
            // Si hay una alerta abierta no se puede capturar la pagina; se acepta primero.
            try {
                driver.switchTo().alert().accept();
            } catch (Exception ignored) {
                // no habia alerta
            }
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            String nombre = context.getDisplayName().replaceAll("[^A-Za-z0-9_-]+", "_")
                    + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")) + ".png";
            Files.write(SHOTS_DIR.resolve(nombre), png);
            return java.util.Base64.getEncoder().encodeToString(png);
        } catch (Exception e) {
            return null;
        }
    }
}
