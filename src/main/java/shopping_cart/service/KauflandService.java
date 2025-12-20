package shopping_cart.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import shopping_cart.Dto.KauflandDto;
import shopping_cart.entity.PriceEntity;
import shopping_cart.entity.ProductEntity;
import shopping_cart.mapper.PriceMapper;
import shopping_cart.mapper.ProductMapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class KauflandService {

    private final ChromeDriver driver;
    private final ProductMapper productMapper;
    private final PriceMapper priceMapper;
    private static final UUID KAUFLAND_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Value("${downloads.dir}")
    private String baseDownloadDir;

    @Value("${kaufland.url}")
    private String brochurePageUrl;

    public KauflandService(ChromeDriver driver,
                           ProductMapper productMapper,
                           PriceMapper priceMapper) {
        this.driver = driver;
        this.productMapper = productMapper;
        this.priceMapper = priceMapper;
    }

    private String getKauflandDownloadDir() {
        return new File(baseDownloadDir, "kaufland").getAbsolutePath();
    }

    public KauflandDto downloadBrochure() throws Exception {
        System.out.println(">>> KAUFLAND SERVICE: Започва сваляне на брошура");
        try {
            priceMapper.deletePricesByStoreId(KAUFLAND_STORE_ID);
            System.out.println(">>> Старите цени за Kaufland са изтрити.");
        } catch (Exception e) {
            System.err.println("Грешка при изтриване на стари цени: " + e.getMessage());
        }
        File downloadDir = new File("downloads");
        File[] oldFiles = downloadDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf") || name.endsWith(".crdownload"));
        if (oldFiles != null) {
            for (File f : oldFiles) {
                if (f.delete()) {
                    System.out.println("Изтрит стар файл: " + f.getName());
                }
            }
        }
        File DownloadDir = new File(getKauflandDownloadDir());
        if (!downloadDir.exists()) downloadDir.mkdirs();

        // Изчистваме стари PDFи
        clearOldFiles(downloadDir, ".pdf", ".crdownload");
        clearOldFiles(new File("./pdfimages_products_kaufland"), ".png");

        driver.get(brochurePageUrl);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        WebElement dateElement = driver.findElement(By.cssSelector("p.m-flyer-tile__validity-date"));
        String validityText = dateElement.getText();
        validityText = validityText.replace("–", "-");
        String[] parts = validityText.split("\\s*-\\s*");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate validFrom = LocalDate.parse(parts[0].trim(), formatter);
        LocalDate validTo = LocalDate.parse(parts[1].trim(), formatter);

        System.out.println("Valid from: " + validFrom);
        System.out.println("Valid to: " + validTo);

        // Изчакваме да се появи бутона
        WebElement downloadButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("div.a-button.a-button--download-flyer")
                )
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", downloadButton);

        String pdfFileName = String.format("Kaufland-%s-%s.pdf",
                validFrom.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                validTo.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        File pdfFile = waitForPdfDownload(downloadDir, 90);
        if (pdfFile == null || pdfFile.length() < 500) {
            throw new RuntimeException("PDF не се свали правилно!");
        }


        System.out.println("СВАЛЕН PDF: " + pdfFile.getAbsolutePath());

        int productsSaved = 0;
        int imagesSaved = 0;

        try (PDDocument document = PDDocument.load(pdfFile)) {
            parseProductsFromPdf(document);
            extractProductImagesFromPdf(document);
        } catch (Exception e) {
            e.printStackTrace();
            return KauflandDto.error("Грешка при обработка на PDF: " + e.getMessage());
        }
        System.out.println("ГОТОВ PDF: " + pdfFile.getAbsolutePath() +
                " (" + (pdfFile.length() / 1024 / 1024) + " MB)");
        validFrom = LocalDate.now();
        validTo = validFrom.plusDays(6);

        System.out.println("ГОТОВО KAUFLAND → продукти: " + productsSaved + ", снимки: " + imagesSaved);

        return KauflandDto.success(
                pdfFile.getName(),
                validFrom,
                validTo,
                productsSaved,
                imagesSaved
        );
    }

    private void parseProductsFromPdf(PDDocument document) throws Exception {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        String text = stripper.getText(document);
        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // 1. ТЪРСИМ ЦЕНА (XX,XX ЛВ) - Това е нашият "котва"
            if (line.matches(".*?(\\d+[,.]\\d{2})\\s*(ЛВ|лв|BGN).*")) {
                String price = extractPrice(line);

                // 2. ИГНОРИРАМЕ грешни цени (като датата 15.12)
                if (line.contains("15.12") || line.contains("21.12")) continue;

                Deque<String> nameParts = new LinkedList<>();

                // Връщаме се МАКСИМУМ 2 реда назад
                for (int j = 1; j <= 2 && (i - j) >= 0; j++) {
                    String prev = lines[i - j].trim();

                    // СПИРАЧКИ (Simple & Direct):
                    if (prev.isEmpty() || prev.matches(".*\\d.*") || prev.contains("=")) break;

                    // Игнорираме очевиден шум (инструкции за цветя)
                    if (prev.toUpperCase().matches(".*(СЛЪНЦЕ|СВЕТЛИНА|ПОЛИВАНЕ|СЯНКА|ОБИЛНО|УМЕРЕНО).*")) continue;

                    nameParts.addFirst(prev);
                }

                String fullName = String.join(" ", nameParts).trim();

                if (fullName.contains("  ")) {
                    String[] parts = fullName.split("\\s{2,}");
                    fullName = parts[parts.length - 1];
                }

                if (fullName.length() > 2) {
                    saveProductAndPrice(fullName, price);
                }
            }
        }
    }

    // Максимално опростен метод за чистене
    private String cleanProductName(String rawName) {
        String name = rawName.trim();

        // 1. Махаме всичко в скоби
        name = name.replaceAll("\\(.*?\\)", "");

        if (name.contains("   ") || name.contains(" . ")) {
            String[] parts = name.split("(\\s{3,}|\\s\\.\\s)");
            if (parts.length > 0) name = parts[0];
        }

        return name.replaceAll("^[.\\s\\-]+|[.\\s\\-]+$", "").trim();
    }

    private String extractPrice(String line) {
        Matcher matcher = Pattern.compile("(\\d+[,.]\\d{2})").matcher(line);
        if (matcher.find()) return matcher.group(1).replace(",", ".");
        return "0.00";
    }

    private void extractProductImagesFromPdf(PDDocument document) throws Exception {
        PDFRenderer renderer = new PDFRenderer(document);
        File outDir = new File("./pdfimages_products_kaufland/");
        if (!outDir.exists()) outDir.mkdirs();

        File[] old = outDir.listFiles(f -> f.getName().endsWith(".png"));
        if (old != null) Arrays.stream(old).forEach(File::delete);

        int imageIndex = 1;
        for (int pageNum = 0; pageNum < document.getNumberOfPages(); pageNum++) {
            BufferedImage pageImage = renderer.renderImageWithDPI(pageNum, 250, ImageType.RGB);
            List<Rectangle> priceZones = findKauflandPriceZones(pageImage);
            Set<Integer> usedY = new HashSet<>();

            for (Rectangle zone : priceZones) {
                if (usedY.stream().anyMatch(y -> Math.abs(y - zone.y) < 80)) continue;
                usedY.add(zone.y);

                int centerX = zone.x + zone.width / 2;
                int w = 580;
                int h = 780;
                int x = Math.max(0, centerX - w / 2);
                int y = Math.max(0, zone.y - h + 100);

                if (x + w > pageImage.getWidth()) x = pageImage.getWidth() - w;
                if (y + h > pageImage.getHeight()) h = pageImage.getHeight() - y;
                if (h < 500) continue;

                BufferedImage crop = pageImage.getSubimage(x, y, w, h);
                String filename = String.format("kaufland_product_%03d.png", imageIndex++);
                ImageIO.write(crop, "PNG", new File(outDir, filename));
            }
        }
    }

    private void saveProductAndPrice(String name, String priceStr) {
        String cleanName = name.trim();
        if (cleanName.isBlank()) return;

        try {
            // 1. Проверяваме дали продуктът вече съществува
            ProductEntity product = productMapper.findBySku(cleanName);

            if (product == null) {
                // Ако го няма -> Създаваме нов
                product = new ProductEntity();
                product.setId(UUID.randomUUID());
                product.setName(cleanName);
                product.setSku(cleanName);
                product.setCreatedAt(OffsetDateTime.now());

                try {
                    productMapper.insert(product);
                } catch (Exception e) {
                    // Ако гръмне тук, значи друга нишка го е създала току-що -> взимаме го
                    product = productMapper.findBySku(cleanName);
                    if (product == null) return;
                }
            }

            // 2. Записваме цената
            PriceEntity priceEntity = new PriceEntity();
            priceEntity.setId(UUID.randomUUID());
            priceEntity.setProductId(product.getId());
            priceEntity.setStoreId(KAUFLAND_STORE_ID); // Ползваме константата за Kaufland
            priceEntity.setPrice(new BigDecimal(priceStr));
            priceEntity.setCurrency("BGN");
            priceEntity.setCreatedAt(OffsetDateTime.now());

            priceMapper.insert(priceEntity);
            System.out.println("Kaufland: " + cleanName + " -> " + priceStr);

        } catch (Exception e) {
            System.err.println("Грешка при запис (Kaufland): " + cleanName + " - " + e.getMessage());
        }
    }

    private List<Rectangle> findKauflandPriceZones(BufferedImage img) {
        List<Rectangle> zones = new ArrayList<>();
        boolean[][] visited = new boolean[img.getHeight()][img.getWidth()];

        for (int y = 200; y < img.getHeight() - 100; y += 20) {
            for (int x = 50; x < img.getWidth() - 50; x += 20) {
                if (visited[y][x]) continue;
                Color c = new Color(img.getRGB(x, y));
                if (c.getRed() > 200 && c.getGreen() < 100 && c.getBlue() < 100) {
                    Rectangle r = floodFillColorBlock(img, x, y, visited, c);
                    if (r.width >= 120 && r.width <= 450 && r.height >= 60 && r.height <= 180) {
                        zones.add(r);
                        markVisitedAround(visited, r, 60);
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
                    if (colorDistance(c, target) < 120) {
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
                    name.toLowerCase().endsWith(".pdf") || name.endsWith(".crdownload"));

            if (files != null && files.length > 0) {
                File finishedPdf = null;
                boolean stillDownloading = false;

                for (File file : files) {
                    if (file.getName().endsWith(".crdownload")) stillDownloading = true;
                    else if (file.getName().toLowerCase().endsWith(".pdf")) finishedPdf = file;
                }

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
