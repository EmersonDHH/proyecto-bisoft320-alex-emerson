# Pruebas automatizadas de Swag Labs con Selenium y Page Object Model

Proyecto final del curso **BISOFT-32 Calidad, Verificación y Validación del Software** (Universidad CENFOTEC).
Automatiza los casos de prueba funcionales de [Swag Labs](https://www.saucedemo.com) con **Java 17, Selenium 4, JUnit 5 y Maven**, aplicando el patrón **Page Object Model (POM)**.

## Ejecución con un solo comando

```bash
mvn test
```

Al terminar se genera el reporte HTML con capturas de pantalla de cada fallo:

```
target/reports/reporte-ejecucion.html
target/reports/screenshots/
```

Una copia de la última ejecución oficial está en `docs/reporte-ejecucion/reporte-ejecucion.html`.

### Requisitos

- JDK 17 o superior
- Maven 3.9 o superior
- Google Chrome, Firefox o Edge instalado. Selenium Manager descarga el driver correcto automáticamente.

### Opciones de ejecución

Toda la configuración está en `src/test/resources/config.properties`, y cualquier clave se puede sobrescribir con `-Dclave=valor`:

| Comando | Qué hace |
|---|---|
| `mvn test` | Ejecuta toda la suite en Chrome headless |
| `mvn test -Dbrowser=firefox` | Ejecuta en Firefox (compatibilidad) |
| `mvn test -Dbrowser=edge` | Ejecuta en Microsoft Edge |
| `mvn test -Dheadless=false` | Muestra el navegador mientras se ejecuta |
| `mvn test -Dgroups=regresion` | Solo la regresión (casos que deben pasar) |
| `mvn test -Dgroups=defecto` | Solo los casos que detectan defectos conocidos |
| `mvn test -Dtest=LoginTest` | Solo una clase de pruebas |

> **Nota sobre los fallos esperados:** los tests con la etiqueta `defecto` verifican el comportamiento **correcto** sobre los usuarios con fallas intencionales (`problem_user`, `error_user`, `visual_user`, `performance_glitch_user`). Por eso **fallan**, y cada fallo es la evidencia de un defecto reportado (DEF-01 a DEF-13). El `pom.xml` usa `testFailureIgnore=true` para que el build termine y siempre se genere el reporte.

> **Driver local distinto a Chrome:** si en el equipo hay un `chromedriver` antiguo en el PATH, se puede indicar uno específico con `mvn test -Dchrome.driver=/ruta/chromedriver`. Si la versión no coincide con Chrome, se puede añadir `-Dchrome.disable.build.check=true`.

## Arquitectura (POM)

```
proyecto-pruebas/
├── pom.xml                         Selenium 4, JUnit 5, ExtentReports
├── README.md
├── docs/                           Casos de prueba, defectos, evidencias y reporte HTML
└── src/test/
    ├── java/proyecto/
    │   ├── base/
    │   │   └── BaseTest.java       Abre y cierra un navegador por test (independencia)
    │   ├── pages/                  Page Objects: SOLO localizadores y acciones
    │   │   ├── BasePage.java       Esperas explícitas (WebDriverWait) y acciones comunes
    │   │   ├── LoginPage.java
    │   │   ├── InventoryPage.java
    │   │   ├── ProductDetailPage.java
    │   │   ├── CartPage.java
    │   │   ├── CheckoutInfoPage.java
    │   │   ├── CheckoutOverviewPage.java
    │   │   ├── CheckoutCompletePage.java
    │   │   └── components/
    │   │       └── HeaderComponent.java   Menú lateral y contador del carrito
    │   ├── tests/                  Casos de prueba: pasos y verificaciones (asserts)
    │   │   ├── LoginTest.java
    │   │   ├── InventoryTest.java
    │   │   ├── CartTest.java
    │   │   └── CheckoutTest.java
    │   └── utils/
    │       ├── Config.java         Lectura de config.properties y -D
    │       ├── DriverFactory.java  Chrome, Firefox o Edge según configuración
    │       ├── ReportExtension.java ExtentReports y captura de pantalla al fallar
    │       └── TestData.java       Usuarios y catálogo de referencia (oráculo)
    └── resources/
        ├── config.properties       URL base, navegador, tiempos de espera
        ├── login_cases.csv         Tabla de decisión del login (@CsvFileSource)
        └── checkout_cases.csv      Tabla de decisión del checkout (@CsvFileSource)
```

### Cumplimiento de las reglas obligatorias

| # | Regla | Dónde se cumple |
|---|---|---|
| 1 | Los Page Objects no tienen asserts | `pages/*` solo exponen acciones y consultas |
| 2 | Los tests no tienen localizadores ni `findElement` | `tests/*` usan solo métodos de las páginas |
| 3 | Esperas explícitas, sin `Thread.sleep` | `BasePage` usa `WebDriverWait` y `ExpectedConditions` |
| 4 | Localizadores estables | `data-test` e `id`; no se usa XPath |
| 5 | Tests independientes | `BaseTest` crea un navegador limpio por test |
| 6 | Configuración fuera del código | `config.properties` más `-D` |
| 7 | Datos externos con `@ParameterizedTest` y `@CsvFileSource` | `LoginTest` y `CheckoutTest` |
| 8 | Reporte HTML con captura en fallos | `ReportExtension` (ExtentReports) |
| 9 | Identificador del caso en cada test | `@DisplayName("TCxx ...")` |
| 10 | Un solo comando | `mvn test` |
| 11 | Commits de todo el equipo | Ver historial de Git |

## Resumen de la última ejecución

| Métrica | Valor |
|---|---|
| Casos diseñados | 46 (41 automatizados y 5 manuales) |
| Ejecuciones automatizadas (con parametrizadas) | 43 |
| Pasaron / fallaron | 30 / 13 |
| Defectos detectados | 13 (DEF-01 a DEF-13) |
| Tiempo de la suite automatizada | 85 s |

