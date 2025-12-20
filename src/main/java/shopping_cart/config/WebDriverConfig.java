// WebDriverConfig.java
package shopping_cart.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebDriverConfig {

    private static final String DOWNLOAD_DIR = Paths.get("downloads").toAbsolutePath().toString();

    @Bean
    public ChromeDriver chromeDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-infobars");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", DOWNLOAD_DIR);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("plugins.always_open_pdf_externally", true);  // ТОВА Е КЛЮЧЪТ за PDF!
        prefs.put("safebrowsing.enabled", true);
        prefs.put("profile.default_content_setting_values.automatic_downloads", 1);

        options.setExperimentalOption("prefs", prefs);

        options.setExperimentalOption("excludeSwitches", java.util.Collections.singletonList("enable-automation"));
        options.addArguments("--disable-blink-features=AutomationControlled");

        return new ChromeDriver(options);
    }
}