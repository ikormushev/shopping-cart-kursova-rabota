package shopping_cart.Dto;

import java.time.LocalDate;

public record KauflandDto(
        String pdfFilename,
        LocalDate validFrom,
        LocalDate validTo,
        int productsCount,
        int imagesCount,
        String status,
        String message
) {
    public static KauflandDto success(String pdfFilename, LocalDate from, LocalDate to, int products, int images) {
        return new KauflandDto(pdfFilename, from, to, products, images, "SUCCESS", "Брошурата е обработена успешно");
    }

    public static KauflandDto error(String message) {
        return new KauflandDto(null, null, null, 0, 0, "ERROR", message);
    }
}