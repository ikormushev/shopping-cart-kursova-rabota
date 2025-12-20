package shopping_cart.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import shopping_cart.Dto.FantastikoDto;
import shopping_cart.mapper.PriceMapper;
import shopping_cart.mapper.ProductMapper;
import shopping_cart.repository.FantastikoRepository;
import shopping_cart.entity.ProductEntity;
import shopping_cart.entity.PriceEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.time.Duration;

@Service
public class FantastikoService {

    private final ChromeDriver driver;
    private final ProductMapper productMapper;
    private final PriceMapper priceMapper;
    private static final UUID FANTASTIKO_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Value("${fantastiko.url}")
    private String url;

    public FantastikoService(
            ChromeDriver driver,
            FantastikoRepository brochureRepository,
            ProductMapper productMapper,
            PriceMapper priceMapper
    ) {
        this.driver = driver;
        this.productMapper = productMapper;
        this.priceMapper = priceMapper;
    }

    public FantastikoDto downloadBrochure() throws Exception {
        System.out.println(">>> SERVICE ENTERED downloadBrochure()");
        priceMapper.deletePricesByStoreId(FANTASTIKO_STORE_ID);
        //Изтриваме старите PDFи
        File downloadDir = new File("downloads");
        File[] oldFiles = downloadDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf") || name.endsWith(".crdownload"));
        if (oldFiles != null) {
            for (File f : oldFiles) {
                if (f.delete()) {
                    System.out.println("Изтрит стар файл: " + f.getName());
                }
            }
        }
        //изтриване на старите снимки
        File outDir = new File("./pdfimages/");
        File[] oldImages = outDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg")
        );

        if (oldImages != null) {
            for (File f : oldImages) {
                if (f.delete()) {
                    System.out.println("Изтрит стар файл: " + f.getName());
                } else {
                    System.out.println("Неуспешно изтриване на: " + f.getName());
                }
            }
        }
        Document html = Jsoup.connect(url).get();

        var element = html.select("div.brochure-container.first div.hold-options").first();
        if (element == null) {
            throw new IllegalStateException("Не може да се намери flippingbook URL на страницата: " + url);
        }

        String flippingBookUrl = element.attr("data-url");

        System.out.println("FlippingBook URL: " + flippingBookUrl);

        driver.get(flippingBookUrl);

        try {
            driver.switchTo().frame(driver.findElement(By.cssSelector("iframe")));
        } catch (Exception e) {
            System.out.println("No iframe found, continuing...");
        }

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("button[title='Download']")
                ));

        var downloadButton = driver.findElements(By.cssSelector("button[title='Download']"));
        if (downloadButton.isEmpty()) {
            throw new IllegalStateException("Не е намерен бутон за сваляне на PDF на страницата: " + flippingBookUrl);
        }

        downloadButton.get(0).click();

        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("a[aria-label='Download the flipbook as a PDF file']")
                ));

        var pdfLinkElements = driver.findElements(By.cssSelector("a[aria-label='Download the flipbook as a PDF file']"));
        if (pdfLinkElements.isEmpty()) {
            throw new IllegalStateException("Не е намерен линк към PDF за сваляне");
        }

        pdfLinkElements.get(0).click();
        System.out.println("Кликнато! Изчаквам сваляне...");

        File pdfFile = waitForPdfDownload(downloadDir, 60);

        if (pdfFile == null || pdfFile.length() < 1_000_000) {
            throw new RuntimeException("PDF не се свали коректно! Размер: " + (pdfFile == null ? "null" : pdfFile.length()));
        }

        String finalName = "fantastiko_brochure_" + LocalDate.now() + ".pdf";
        File finalFile = new File(downloadDir, finalName);
        if (!pdfFile.getAbsolutePath().equals(finalFile.getAbsolutePath())) {
            pdfFile.renameTo(finalFile);
            pdfFile = finalFile;
        }

        System.out.println("ГОТОВ PDF: " + pdfFile.getAbsolutePath() +
                " (" + (pdfFile.length() / 1024 / 1024) + " MB)");

        try (PDDocument document = PDDocument.load(pdfFile)) {
            parseProductsFromPdf(document);
            extractProductImagesFromPages(document);
        }
        catch (Exception e) {
            throw new RuntimeException("Грешка при отваряне на сваления PDF", e);
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);

        return new FantastikoDto(pdfFile.getName(), startDate, endDate);
    }

    private void parseProductsFromPdf(PDDocument document) throws Exception {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        String text = stripper.getText(document);
        String[] lines = text.split("\n");

        List<String> stopPhrases = Arrays.asList(
                "цена за", "цена на", "лв", "лв.", "bgn", "€", "eur",
                "фантастико", "оферта за периода", "продуктите се продават",
                "www.", ".bg", ".com", "произход", "вакуум", "слайс"
        );

        List<String> skipPhrases = Arrays.asList(
                "%", "отстъпка", "промоция", "евтин", "събира ни вкусът",
                "декорацията", "изображенията", "са с илюстративна", "без консерванти"
        );

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.contains("%") || line.matches(".*-\\d+\\s*%?.*")) continue;

            if (line.matches(".*?(\\d+[.,]\\d+)\\s*(ЛВ|лв|BGN).*")) {

                String price = extractPrice(line);
                try {
                    double priceVal = Double.parseDouble(price);
                    if (priceVal < 0.10 || priceVal > 5000) continue;
                } catch (Exception e) { continue; }

                Deque<String> nameParts = new LinkedList<>();

                String currentLineCleaned = line.replaceAll("(\\d+[.,]\\d+)|(ЛВ|лв|€|BGN)|(-?\\d+%)", "").trim();

                currentLineCleaned = currentLineCleaned.replaceAll("^\\d+\\s*(г|кг|мл|л|ml|kg|g|бр|бр\\.)\\.?\\s+", "");

                currentLineCleaned = currentLineCleaned.replaceAll("^\\s*[+\\-.]+\\s*", "");

                if (currentLineCleaned.length() > 2 && !currentLineCleaned.matches("^[\\d\\s.,-]+$")) {
                    nameParts.addFirst(currentLineCleaned);
                }

                for (int j = 1; j <= 6 && i - j >= 0; j++) {
                    String prevLine = lines[i - j].trim();
                    if (prevLine.isEmpty()) continue;

                    String prevLineLower = prevLine.toLowerCase();

                    if (prevLineLower.matches(".*\\d+\\s*(г|кг|мл|л|ml|kg|g|бр|бр\\.)\\.?$")) {
                        break;
                    }

                    if (prevLine.matches(".*\\d+[.,]\\d+\\s*([Лл][Вв]\\.?|BGN|€).*")) {
                        break;
                    }

                    boolean shouldStop = false;
                    for (String stop : stopPhrases) {
                        if (prevLineLower.contains(stop)) {
                            shouldStop = true;
                            break;
                        }
                    }
                    if (shouldStop) break;

                    boolean shouldSkip = false;
                    for (String skip : skipPhrases) {
                        if (prevLineLower.contains(skip)) {
                            shouldSkip = true;
                            break;
                        }
                    }
                    if (!prevLine.matches(".*[а-яА-Яa-zA-Z].*")) shouldSkip = true;

                    if (shouldSkip) continue;

                    nameParts.addFirst(prevLine);
                }

                String fullName = String.join(" ", nameParts);
                String cleanName = cleanProductName(fullName);

                if (cleanName.length() >= 3) {
                    saveProductAndPrice(cleanName, price);
                }
            }
        }
    }

    private String cleanProductName(String rawName) {
        String name = rawName;

        name = name.replaceAll("^\\s*[+\\-.]+\\s*", "");

        name = name.replaceAll("(?i)(\\d+[.,]\\d+)\\s*(лв|€|bgn)", "");
        name = name.replaceAll("(?i)\\s(лв\\.?|€|bgn)\\s?", " ");

        name = name.replaceAll("(?i)цена за.*", "");
        name = name.replaceAll("(?i)без кост.*", ""); // Опционално, ако искате да махате "без кост"

        name = name.replaceAll("\\s+", " ").trim();

        return name;
    }


    private String extractPrice(String line) {
        String cleanLine = line.replaceAll("(\\d+)-(\\d+)", "$1.$2");

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+[.,]\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(cleanLine);

        List<BigDecimal> numbers = new ArrayList<>();

        while (matcher.find()) {
            try {
                String numStr = matcher.group(1).replace(",", ".");
                numbers.add(new BigDecimal(numStr));
            } catch (NumberFormatException ignored) {}
        }

        if (numbers.isEmpty()) {
            return "0.00";
        }

        if (numbers.size() == 1) {
            return numbers.get(0).toString();
        }

        BigDecimal first = numbers.get(0);
        BigDecimal second = numbers.get(1);
        BigDecimal euroRate = new BigDecimal("1.95583");

        BigDecimal calculatedEuro = first.divide(euroRate, 2, java.math.RoundingMode.HALF_UP);
        if (calculatedEuro.subtract(second).abs().doubleValue() < 0.05) {
            return first.toString();
        }

        BigDecimal calculatedEuroReverse = second.divide(euroRate, 2, java.math.RoundingMode.HALF_UP);
        if (calculatedEuroReverse.subtract(first).abs().doubleValue() < 0.05) {
            return second.toString();
        }

        return numbers.get(0).toString();
    }

    private void saveProductAndPrice(String name, String priceString) {
        String cleanName = name.trim();

        // 1. Търсим или създаваме продукта
        ProductEntity product = productMapper.findBySku(cleanName);

        if (product == null) {
            product = new ProductEntity();
            product.setId(UUID.randomUUID());
            product.setName(cleanName);
            product.setSku(cleanName);
            product.setCreatedAt(OffsetDateTime.now());

            try {
                productMapper.insert(product);
                System.out.println("Създаден нов продукт: " + cleanName);
            } catch (Exception e) {
                System.err.println("Грешка при запис на продукт: " + cleanName);
                return;
            }
        }

        // 2. Валидация на цената
        BigDecimal priceValue;
        try {
            priceValue = new BigDecimal(priceString);
            if (priceValue.compareTo(BigDecimal.ZERO) <= 0) {
                System.err.println("Невалидна цена (0 или отрицателна) за: " + cleanName);
                return;
            }
        } catch (NumberFormatException e) {
            System.err.println("Грешка при парсване на цена: " + priceString + " за продукт: " + cleanName);
            return;
        }

        try {
            PriceEntity priceEntity = new PriceEntity();
            priceEntity.setId(UUID.randomUUID());
            priceEntity.setProductId(product.getId()); // Връзка към продукта
            priceEntity.setStoreId(FANTASTIKO_STORE_ID); // Фантастико ID
            priceEntity.setPrice(priceValue);
            priceEntity.setCurrency("BGN");
            priceEntity.setCreatedAt(OffsetDateTime.now());

            // Тук използвате вашия priceMapper за запис в БД
            priceMapper.insert(priceEntity);

            System.out.println(String.format("Успешно записана цена: %s -> %s лв.", cleanName, priceValue));
        } catch (Exception e) {
            System.err.println("Грешка при запис на цената за: " + cleanName);
            e.printStackTrace();
        }
    }
    private List<Rectangle> findWhiteFieldsWithText(BufferedImage img) {
        List<Rectangle> fields = new ArrayList<>();
        boolean[][] visited = new boolean[img.getHeight()][img.getWidth()];

        for (int y = 100; y < img.getHeight() - 100; y += 20) {
            for (int x = 50; x < img.getWidth() - 50; x += 20) {
                if (visited[y][x]) continue;

                Color bgColor = new Color(img.getRGB(x, y));
                if (bgColor.getRed() > 230 && bgColor.getGreen() > 230 && bgColor.getBlue() > 230) {
                    if (hasDarkTextInField(img, x, y, 200, 100)) {
                        Rectangle field = floodFillWhiteField(img, x, y, visited);
                        if (field.width > 200 && field.width < 600 && field.height > 80 && field.height < 250) {
                            fields.add(field);
                            markVisitedAround(visited, field, 30);
                        }
                    }
                }
            }
        }
        fields.sort(Comparator.comparingInt(r -> r.y)); // отгоре надолу
        return fields;
    }

    private boolean hasDarkTextInField(BufferedImage img, int x, int y, int width, int height) {
        for (int dy = 0; dy < height; dy += 15) {
            for (int dx = 0; dx < width; dx += 15) {
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && nx < img.getWidth() && ny >= 0 && ny < img.getHeight()) {
                    Color c = new Color(img.getRGB(nx, ny));
                    if (c.getRed() < 120 && c.getGreen() < 120 && c.getBlue() < 120) {
                        return true; // има тъмен текст
                    }
                }
            }
        }
        return false;
    }

    private Rectangle floodFillWhiteField(BufferedImage img, int sx, int sy, boolean[][] visited) {
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
                    if (c.getRed() > 220 && c.getGreen() > 220 && c.getBlue() > 220) {
                        visited[ny][nx] = true;
                        q.add(new int[]{nx, ny});
                    }
                }
            }
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
    private void extractProductImagesFromPages(PDDocument document) throws Exception {
        File outDir = new File("./pdfimages_products/");
        if (!outDir.exists()) outDir.mkdirs();

        // Изтриваме старите
        File[] old = outDir.listFiles(f -> f.getName().endsWith(".png"));
        if (old != null) Arrays.stream(old).forEach(File::delete);

        PDFRenderer renderer = new PDFRenderer(document);
        int counter = 1;

        for (int i = 0; i < document.getNumberOfPages(); i++) {
            BufferedImage page = renderer.renderImageWithDPI(i, 250, ImageType.RGB);

            List<Rectangle> priceZones = findAllPriceZones(page);
            System.out.println("Страница " + (i + 1) + " → намерени " + priceZones.size() + " ценови блока");

            Set<Integer> usedPricesByY = new HashSet<>();

            for (Rectangle price : priceZones) {

                // Пропускаме дублиращи продукти
                if (usedPricesByY.stream().anyMatch(pY -> Math.abs(pY - price.y) < 50)) {
                    continue;
                }

                usedPricesByY.add(price.y);

                int priceTopY = price.y;

                int width = 600;
                int height = 800;

                int productBottomY = priceTopY;
                int productTopY = productBottomY - height;

                int centerX = price.x + price.width / 2;
                int x = centerX - width / 2;

                if (x < 0) x = 0;
                if (x + width > page.getWidth()) x = page.getWidth() - width;
                if (productTopY < 0) {
                    productTopY = 0;
                    height = productBottomY - productTopY;
                }
                if (height < 500) continue;

                BufferedImage product = page.getSubimage(x, productTopY, width, height);

                String filename = String.format("product_%03d.png", counter++);
                ImageIO.write(product, "PNG", new File(outDir, filename));

                System.out.println("СНИМКА: " + filename + " (единствена за този продукт)");
            }
        }

        System.out.println("\nГОТОВО — записани са общо " + (counter - 1) + " уникални продуктови изображения!");
    }

    private List<Rectangle> findAllPriceZones(BufferedImage img) {
        boolean[][] visited = new boolean[img.getHeight()][img.getWidth()];
        List<Rectangle> zones = new ArrayList<>();

        zones.addAll(findCleanPriceZones(img, visited));

        for (int y = 250; y < img.getHeight() - 150; y += 25) {
            for (int x = 80; x < img.getWidth() - 80; x += 25) {
                if (visited[y][x]) continue;

                Color c = new Color(img.getRGB(x, y));
                if (c.getRed() > 235 && c.getGreen() > 235 && c.getBlue() > 235) {
                    if (hasDarkTextNearby(img, x, y, 90)) {
                        Rectangle r = floodFillWhiteBlock(img, x, y, visited);
                        if (r.width >= 160 && r.width <= 520 && r.height >= 70 && r.height <= 220) {
                            if (zones.stream().noneMatch(z -> Math.abs(z.getCenterY() - r.getCenterY()) < 90)) {
                                zones.add(r);
                            }
                            markVisitedAround(visited, r, 70);
                        }
                    }
                }
            }
        }

        zones.sort(Comparator.comparingInt(r -> (int) r.getCenterY()));
        return zones;
    }
    private List<Rectangle> findCleanPriceZones(BufferedImage img, boolean[][] visited) {
        List<Rectangle> result = new ArrayList<>();

        for (int y = 300; y < img.getHeight() - 50; y += 15) {
            for (int x = 50; x < img.getWidth() - 50; x += 15) {
                if (visited[y][x]) continue;

                Color c = new Color(img.getRGB(x, y));

                boolean isPriceColor =
                        (c.getRed() > 238 && c.getGreen() > 195 && c.getBlue() < 90) ||   // жълто
                                (c.getRed() > 230 && c.getGreen() < 110 && c.getBlue() < 110);     // червено

                if (isPriceColor) {
                    Rectangle r = floodFillSimple(img, x, y, visited);
                    if (r.width >= 95 && r.width <= 360 && r.height >= 48 && r.height <= 105) {
                        result.add(r);
                        markVisitedAround(visited, r, 50);
                    }
                }
            }
        }
        return result;
    }
    private boolean hasDarkTextNearby(BufferedImage img, int x, int y, int radius) {
        for (int dy = -radius; dy <= radius; dy += 10) {
            for (int dx = -radius; dx <= radius; dx += 10) {
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && nx < img.getWidth() && ny >= 0 && ny < img.getHeight()) {
                    Color c = new Color(img.getRGB(nx, ny));
                    if (c.getRed() < 100 && c.getGreen() < 100 && c.getBlue() < 100) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Rectangle floodFillWhiteBlock(BufferedImage img, int sx, int sy, boolean[][] visited) {
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
                    if (c.getRed() > 220 && c.getGreen() > 220 && c.getBlue() > 220) {
                        visited[ny][nx] = true;
                        q.add(new int[]{nx, ny});
                    }
                }
            }
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private void markVisitedAround(boolean[][] visited, Rectangle r, int padding) {
        for (int y = r.y - padding; y <= r.y + r.height + padding; y++) {
            for (int x = r.x - padding; x <= r.x + r.width + padding; x++) {
                if (y >= 0 && y < visited.length && x >= 0 && x < visited[0].length) {
                    visited[y][x] = true;
                }
            }
        }
    }

    private int colorDistance(Color c1, Color c2) {
        int dr = c1.getRed() - c2.getRed();
        int dg = c1.getGreen() - c2.getGreen();
        int db = c1.getBlue() - c2.getBlue();
        return dr*dr + dg*dg + db*db;
    }
    private Rectangle floodFillSimple(BufferedImage img, int startX, int startY, boolean[][] visited) {
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startX, startY});
        visited[startY][startX] = true;

        int minX = startX, maxX = startX;
        int minY = startY, maxY = startY;

        Color targetColor = new Color(img.getRGB(startX, startY));

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int x = pos[0], y = pos[1];

            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);

            int[][] dirs = {{0,1},{1,0},{0,-1},{-1,0}};
            for (int[] d : dirs) {
                int nx = x + d[0];
                int ny = y + d[1];

                if (nx >= 0 && nx < img.getWidth() && ny >= 0 && ny < img.getHeight() && !visited[ny][nx]) {
                    Color c = new Color(img.getRGB(nx, ny));
                    if (colorDistance(c, targetColor) < 70) {
                        visited[ny][nx] = true;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }

        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private boolean isSimilarPriceColor(Color c) {
        return (c.getRed() > 180 && c.getGreen() > 140 && c.getBlue() < 140) ||
                (c.getRed() > 200 && c.getGreen() < 120 && c.getBlue() < 120);
    }

    private File waitForPdfDownload(File downloadDir, int timeoutSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < deadline) {
            File[] files = downloadDir.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".pdf") || name.endsWith(".crdownload"));

            if (files != null && files.length > 0) {
                File finishedPdf = null;
                boolean stillDownloading = false;

                for (File file : files) {
                    if (file.getName().endsWith(".crdownload")) {
                        stillDownloading = true;           // още се сваля
                    } else if (file.getName().toLowerCase().endsWith(".pdf")) {
                        finishedPdf = file;                // намерен PDF
                    }
                }

                // Ако няма .crdownload и има .pdf → свалянето е приключило
                if (!stillDownloading && finishedPdf != null && finishedPdf.length() > 100_000) {
                    Thread.sleep(800);
                    return finishedPdf;
                }
            }

            Thread.sleep(1000);
        }

        return null;
    }


}
