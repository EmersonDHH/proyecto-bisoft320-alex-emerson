package proyecto.utils;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.File;
import java.util.Map;

/**
 * Crea el WebDriver segun la configuracion (browser, headless, tamano de ventana).
 * Selenium Manager resuelve el driver automaticamente si no se indica uno.
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create() {
        boolean headless = Config.getBool("headless");
        WebDriver driver = switch (Config.browser()) {
            case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
            case "edge" -> new EdgeDriver(edgeOptions(headless));
            case "chrome" -> chrome(headless);
            default -> throw new IllegalArgumentException("Navegador no soportado: " + Config.browser());
        };
        driver.manage().window().setSize(new Dimension(Config.getInt("window.width"), Config.getInt("window.height")));
        return driver;
    }

    private static WebDriver chrome(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu",
                "--disable-search-engine-choice-screen", "--disable-features=PasswordLeakDetection");
        // Evita el dialogo "Cambia tu contrasena" de Chrome que bloquea la pantalla tras el login
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        String binary = Config.get("chrome.binary");
        if (!binary.isEmpty()) {
            options.setBinary(binary);
        }
        String driverPath = Config.get("chrome.driver");
        if (!driverPath.isEmpty()) {
            ChromeDriverService service = new ChromeDriverService.Builder()
                    .usingDriverExecutable(new File(driverPath))
                    .withBuildCheckDisabled(Config.getBool("chrome.disable.build.check"))
                    .build();
            return new ChromeDriver(service, options);
        }
        return new ChromeDriver(options);
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }

    private static EdgeOptions edgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        return options;
    }
}
