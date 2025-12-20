package shopping_cart.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.InputStream;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import java.awt.geom.Rectangle2D;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import java.nio.file.StandardCopyOption;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import shopping_cart.Dto.BillaDto; // Увери се, че имаш този DTO
import shopping_cart.entity.PriceEntity;
import shopping_cart.entity.ProductEntity;
import shopping_cart.mapper.PriceMapper;
import shopping_cart.mapper.ProductMapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BillaService {

    private final ChromeDriver driver;
    private final ProductMapper productMapper;
    private final PriceMapper priceMapper;
    private static final UUID BILLA_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Value("${billa.url}")
    private String brochurePageUrl;

    public BillaService(ChromeDriver driver,
                        ProductMapper productMapper,
                        PriceMapper priceMapper) {
        this.driver = driver;
        this.productMapper = productMapper;
        this.priceMapper = priceMapper;
    }


    public BillaDto downloadBrochure() throws Exception {
        System.out.println(">>> BILLA SERVICE: Започва сваляне на брошура от homepage (с навигация до слайдера)");
        try {
            priceMapper.deletePricesByStoreId(BILLA_STORE_ID);
            System.out.println(">>> Старите цени за Billa са изтрити.");
        } catch (Exception e) {
            System.err.println("Грешка при триене на стари цени: " + e.getMessage());
        }

        File downloadDir = new File("downloads");
        if (!downloadDir.exists()) downloadDir.mkdirs();

        //Почистване на стари файлове
        clearOldFiles(downloadDir, ".pdf", ".crdownload");
        clearOldFiles(new File("./pdfimages_products_billa"), ".png");

        driver.get(brochurePageUrl); // https://www.billa.bg/
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // За бавно зареждане

        try {
            WebElement cookieBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("onetrust-accept-btn-handler")));
            cookieBtn.click();
            System.out.println("Бисквитките са приети.");
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println("Няма бисквитки.");
        }

        LocalDate validFrom = LocalDate.now();
        LocalDate validTo = validFrom.plusDays(7);
        boolean navigated = false;

        try {
            WebElement slider = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("ul.ws-slider-group__inner, .ws-slider-group__inner")));
            System.out.println("Намерен слайдер на homepage.");

            WebElement firstTeaser = slider.findElement(By.cssSelector("div.ws-teaser__content.pa-4:first-of-type, .ws-teaser__content:first-child"));
            System.out.println("Намерен първи teaser: " + firstTeaser.getText());

            try {
                String teaserText = firstTeaser.getText();
                if (teaserText.contains("-") && teaserText.matches(".*\\d{2}\\.\\d{2}\\.\\d{4}.*")) {
                    String[] parts = teaserText.split("\\s*-\\s*");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    validFrom = LocalDate.parse(parts[0].trim().replaceAll("[^0-9.]", ""), formatter);
                    validTo = LocalDate.parse(parts[1].trim().replaceAll("[^0-9.]", ""), formatter);
                    System.out.println("Дати от teaser: " + validFrom + " до " + validTo);
                }
            } catch (Exception e) {
                System.out.println("Не можах да извлека дати от teaser.");
            }

            WebElement brochureLink = firstTeaser.findElement(By.tagName("a")); // Или By.cssSelector("a[href*='promocii']")
            String targetUrl = brochureLink.getAttribute("href");
            if (targetUrl.startsWith("/")) {
                targetUrl = "https://www.billa.bg" + targetUrl; // Абсолютен URL
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", brochureLink);
            Thread.sleep(1000);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", brochureLink);
            navigated = true;
            System.out.println("Кликнато върху брошура линк: " + targetUrl);

            // Изчакваме зареждане на подстраницата
            Thread.sleep(3000);

        } catch (Exception e1) {
            System.out.println("Навигация през слайдера не сработи: " + e1.getMessage() + " – fallback към директна навигация.");

            try {
                driver.get("https://www.billa.bg/promocii/sedmichna-broshura");
                navigated = true;
                System.out.println("Fallback: Директно до седмична брошура.");
                Thread.sleep(3000);
            } catch (Exception e2) {
                try {
                    WebElement menuLink = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(text(), 'Промоции') or contains(@href, 'promocii')]")));
                    menuLink.click();
                    Thread.sleep(2000);
                    // После клик на брошура
                    WebElement brochureMenu = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(text(), 'Брошура') or contains(@href, 'broshura')]")));
                    brochureMenu.click();
                    navigated = true;
                    System.out.println("Fallback: Навигация през менюто.");
                } catch (Exception e3) {
                    throw new RuntimeException("Не може да се навигира до брошурата! Провери homepage структурата.", e3);
                }
            }
        }

        // Сваляне на PDF
        String pdfHref = null;
        WebElement pdfElement = null;
        boolean inIframe = false;

        if (navigated) {
            try {
                WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("iframe[src*='publitas.com'], iframe[src*='viewer'], iframe[class*='publication']")));
                driver.switchTo().frame(iframe);
                inIframe = true;
                System.out.println("Превключено към Publitas iframe на брошурата.");

                Thread.sleep(4000);

                // Селектори за PDF в iframe
                List<By> selectors = Arrays.asList(
                        By.id("downloadAsPdf"),
                        By.cssSelector("a[data-href='download_pdf'], a[download]"),
                        By.cssSelector("a[aria-label*='PDF'], a[aria-label*='Download']"),
                        By.cssSelector(".download-button, .pui-download, button[title*='PDF']"),
                        By.xpath("//a[contains(@href, '.pdf') or contains(@href, 'publitas') and (contains(., 'PDF') or contains(., 'Изтегли'))]")
                );

                for (By selector : selectors) {
                    try {
                        pdfElement = wait.until(ExpectedConditions.elementToBeClickable(selector));
                        System.out.println("Намерен PDF елемент в iframe с: " + selector);
                        break;
                    } catch (Exception ignored) {}
                }

                if (pdfElement != null) {
                    pdfHref = pdfElement.getAttribute("href");
                    if (pdfHref != null && pdfHref.contains(".pdf")) {
                        driver.get(pdfHref);
                        System.out.println("Директно сваляне: " + pdfHref);
                    } else {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", pdfElement);
                        System.out.println("Кликнато в iframe.");
                    }
                } else {
                    throw new Exception("PDF елемент не е намерен в iframe.");
                }
            } catch (Exception e) {
                System.out.println("Iframe фейл: " + e.getMessage() + " – директен API fallback.");
                driver.switchTo().defaultContent(); // Излизаме от iframe ако сме в него
                String apiUrl = "https://www.billa.bg/api/brochure/pdf/current"; // Или конкретния от твоя snippet
                driver.get(apiUrl);
            }

            if (inIframe) {
                driver.switchTo().defaultContent();
            }
        }

        // Изчакване на PDF
        File pdfFile = waitForPdfDownload(downloadDir, 90);
        if (pdfFile == null || pdfFile.length() < 500_000) {
            throw new RuntimeException("PDF не се свали (очаквано >5MB)!");
        }

        // Преименуване
        String newName = String.format("Billa-Brochure-%s-%s.pdf",
                validFrom.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                validTo.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        File finalPdf = new File(downloadDir, newName);
        if (pdfFile.renameTo(finalPdf)) {
            pdfFile = finalPdf;
        }
        System.out.println("СВАЛЕН: " + pdfFile.getAbsolutePath());

        try (PDDocument document = PDDocument.load(pdfFile)) {
            parseProductsFromPdf(document);
            extractProductImagesFromPdf(document);
        } catch (Exception e) {
            System.err.println("Обработка PDF: " + e.getMessage());
        }

        return new BillaDto(pdfFile.getName(), validFrom, validTo);
    }

    private void parseProductsFromPdf(PDDocument document) throws Exception {
        System.out.println(">>> ЗАПОЧВА ПАРСВАНЕ (DEBUG MODE)...");

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true); // Важно за подредбата
        String text = stripper.getText(document);

        // 1. ПРОВЕРКА ДАЛИ ИМА ТЕКСТ ИЗОБЩО
        if (text == null || text.trim().isEmpty()) {
            System.err.println("!!! ГРЕШКА: PDFTextStripper върна празен текст! Този PDF е само картинки (Scanned). Трябва OCR.");
            return;
        }

        System.out.println("--- НАЧАЛО НА СУРОВ ТЕКСТ (Първите 500 символа) ---");
        System.out.println(text.substring(0, Math.min(text.length(), 500)));
        System.out.println("--- КРАЙ НА СУРОВ ТЕКСТ ---");

        String[] lines = text.split("\n");
        System.out.println("Общо редове за обработка: " + lines.length);

        // Regex за цена: По-либерален (хваща 9.99, 9,99, с или без лв)
        Pattern pricePattern = Pattern.compile("(\\d+[.,]\\d{2})\\s*(?:лв|BGN|€)?", Pattern.CASE_INSENSITIVE);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            Matcher priceMatcher = pricePattern.matcher(line);
            if (priceMatcher.find()) {
                // Намерен е ред с число, приличащо на цена
                String priceRaw = priceMatcher.group(1).replace(",", ".");

                try {
                    double priceVal = Double.parseDouble(priceRaw);
                    // Филтър за невалидни цени (дати, проценти, твърде малки/големи суми)
                    if (priceVal < 0.10 || priceVal > 200.00) {
                        // System.out.println("SKIP PRICE (Value): " + line);
                        continue;
                    }

                    // Ако стигнем до тук, имаме валидна цена. Търсим името.
                    System.out.println("DEBUG: Намерена цена " + priceRaw + " на ред: " + line);

                    Deque<String> nameParts = new LinkedList<>();

                    // Търсим назад до 5 реда
                    for (int j = 1; j <= 5 && i - j >= 0; j++) {
                        String prev = lines[i - j].trim();
                        if (prev.length() < 2) continue;

                        // Стоп думи
                        if (prev.matches("(?i).*(лв|bgn|цена|отстъпка|супер|промо|без кост|за 1 кг).*")) continue;
                        if (prev.matches(".*\\d+.*")) break; // Спираме при други цифри

                        nameParts.addFirst(prev);
                    }

                    String potentialName = String.join(" ", nameParts);
                    potentialName = cleanProductName(potentialName);

                    if (potentialName.length() > 3) {
                        saveProductAndPrice(potentialName, priceRaw);
                    } else {
                        System.out.println("--> Неуспешно име за цена " + priceRaw + ". Намерено парче: '" + potentialName + "'");
                    }

                } catch (NumberFormatException e) {
                    // Not a number
                }
            }
        }
        System.out.println(">>> ПРИКЛЮЧИ ПАРСВАНЕТО.");
    }
    private String cleanProductName(String rawName) {
        String name = rawName.trim();

        // Махаме маркетингови послания, специфични за Billa
        name = name.replaceAll("(?i)(Весела Коледа|25 години|ВИНАГИ ДО ВАС|СЕГА В BILLA|ПРАЗНУВАЙ С)", "");
        name = name.replaceAll("(?i)(BILLA ready|BILLA Card|Billa)", "");

        // Махаме символи и остатъци от проценти
        name = name.replaceAll("[-–%*]", "");

        // Махаме излишни интервали
        name = name.replaceAll("\\s+", " ").trim();

        return name;
    }

    private String extractPrice(String line) {
        Matcher matcher = Pattern.compile("(\\d+[.,]\\d{2})").matcher(line);
        if (matcher.find()) {
            return matcher.group(1).replace(",", ".");
        }
        return "0.00";
    }

    private void saveProductAndPrice(String name, String priceStr) {
        String cleanName = name.trim();
        if (cleanName.isBlank()) return;

        try {
            // 1. Проверяваме дали продуктът вече съществува по SKU (името)
            ProductEntity product = productMapper.findBySku(cleanName);

            if (product == null) {
                // Няма го -> Създаваме нов
                product = new ProductEntity();
                product.setId(UUID.randomUUID());
                product.setName(cleanName);
                product.setSku(cleanName);
                product.setCreatedAt(OffsetDateTime.now());

                try {
                    productMapper.insert(product);
                } catch (Exception e) {
                    // Ако гръмне тук, значи някой друг го е вкарал току-що -> взимаме го
                    product = productMapper.findBySku(cleanName);
                    if (product == null) return;
                }
            }

            // 2. Записваме цената
            PriceEntity priceEntity = new PriceEntity();
            priceEntity.setId(UUID.randomUUID());
            priceEntity.setProductId(product.getId());
            priceEntity.setStoreId(BILLA_STORE_ID); // Ползваме Billa ID
            priceEntity.setPrice(new BigDecimal(priceStr));
            priceEntity.setCurrency("BGN");
            priceEntity.setCreatedAt(OffsetDateTime.now());

            priceMapper.insert(priceEntity);
            System.out.println("Billa SAVE: " + cleanName + " -> " + priceStr);

        } catch (Exception e) {
            System.err.println("Грешка при запис (Billa): " + cleanName + " - " + e.getMessage());
        }
    }

    private void extractProductImagesFromPdf(PDDocument document) throws Exception {
        PDFRenderer renderer = new PDFRenderer(document);
        File outDir = new File("./pdfimages_products_billa/");
        if (!outDir.exists()) outDir.mkdirs();

        int imageIndex = 1;
        for (int pageNum = 0; pageNum < document.getNumberOfPages(); pageNum++) {
            // Рендерираме страницата като картинка
            BufferedImage pageImage = renderer.renderImageWithDPI(pageNum, 200, ImageType.RGB);

            // Търсим зони с цени
            List<Rectangle> priceZones = findBillaPriceZones(pageImage);

            Set<Integer> usedY = new HashSet<>();

            for (Rectangle zone : priceZones) {
                // Избягваме дублиране на снимки за един и същи ред
                if (usedY.stream().anyMatch(y -> Math.abs(y - zone.y) < 100)) continue;
                usedY.add(zone.y);

                // Изрязваме продукта над цената
                int w = 600;
                int h = 700;
                int centerX = zone.x + zone.width / 2;
                int x = Math.max(0, centerX - w / 2);
                int y = Math.max(0, zone.y - h + 50); // Взимаме малко и от цената

                if (x + w > pageImage.getWidth()) x = pageImage.getWidth() - w;
                if (y + h > pageImage.getHeight()) h = pageImage.getHeight() - y;
                if (h < 300) continue; // Твърде малка снимка

                BufferedImage crop = pageImage.getSubimage(x, y, w, h);
                String filename = String.format("billa_product_%03d.png", imageIndex++);
                ImageIO.write(crop, "PNG", new File(outDir, filename));
            }
        }
    }

    // Търсене на цветовете на BILLA (Жълто и Червено)
    private List<Rectangle> findBillaPriceZones(BufferedImage img) {
        List<Rectangle> zones = new ArrayList<>();
        boolean[][] visited = new boolean[img.getHeight()][img.getWidth()];

        for (int y = 100; y < img.getHeight() - 100; y += 20) {
            for (int x = 50; x < img.getWidth() - 50; x += 20) {
                if (visited[y][x]) continue;
                Color c = new Color(img.getRGB(x, y));

                boolean isYellow = c.getRed() > 200 && c.getGreen() > 180 && c.getBlue() < 100;
                boolean isRed = c.getRed() > 200 && c.getGreen() < 100 && c.getBlue() < 100;

                if (isYellow || isRed) {
                    Rectangle r = floodFillColorBlock(img, x, y, visited, c);
                    if (r.width > 50 && r.width < 500 && r.height > 30 && r.height < 200) {
                        zones.add(r);
                        markVisitedAround(visited, r, 50);
                    }
                }
            }
        }
        zones.sort(Comparator.comparingInt(r -> r.y));
        return zones;
    }

    private Rectangle floodFillColorBlock(BufferedImage img, int sx, int sy, boolean[][] visited, Color target) {
        Queue<int[]> q = new LinkedList<>();
        q.add(new int[]{sx, sy});
        visited[sy][sx] = true;
        int minX = sx, maxX = sx, minY = sy, maxY = sy;

        while (!q.isEmpty()) {
            int[] p = q.poll();
            int x = p[0], y = p[1];
            minX = Math.min(minX, x); maxX = Math.max(maxX, x);
            minY = Math.min(minY, y); maxY = Math.max(maxY, y);
            int[][] dirs = {{0,1},{1,0},{0,-1},{-1,0}};

            for (int[] d : dirs) {
                int nx = x + d[0], ny = y + d[1];
                if (nx >= 0 && nx < img.getWidth() && ny >= 0 && ny < img.getHeight() && !visited[ny][nx]) {
                    Color c = new Color(img.getRGB(nx, ny));
                    if (colorDistance(c, target) < 2500) { // Толеранс за подобен цвят
                        visited[ny][nx] = true;
                        q.add(new int[]{nx, ny});
                    }
                }
            }
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private int colorDistance(Color a, Color b) {
        int dr = a.getRed() - b.getRed();
        int dg = a.getGreen() - b.getGreen();
        int db = a.getBlue() - b.getBlue();
        return dr*dr + dg*dg + db*db;
    }

    private void markVisitedAround(boolean[][] visited, Rectangle r, int pad) {
        for (int y = r.y - pad; y <= r.y + r.height + pad; y++) {
            for (int x = r.x - pad; x <= r.x + r.width + pad; x++) {
                if (y >= 0 && y < visited.length && x >= 0 && x < visited[0].length) {
                    visited[y][x] = true;
                }
            }
        }
    }

    private void clearOldFiles(File dir, String... extensions) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles((d, name) -> {
            for (String ext : extensions) if (name.toLowerCase().endsWith(ext)) return true;
            return false;
        });
        if (files != null) for (File f : files) f.delete();
    }

    private File waitForPdfDownload(File downloadDir, int timeoutSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            File[] files = downloadDir.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".pdf") && !name.endsWith(".crdownload"));

            if (files != null && files.length > 0) {
                // Взимаме най-новия файл
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                File file = files[0];
                if (file.length() > 100_000) {
                    Thread.sleep(1000); // Изчакваме
                    return file;
                }
            }
            Thread.sleep(1000);
        }
        return null;
    }
}