package com.tripreport.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tripreport.model.TripEntry;
import com.tripreport.model.TripReport;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generates PDF files from Trip Report data using a template PDF.
 *
 * This class handles:
 * - Loading the PDF template
 * - Filling in header fields (mileage, dates)
 * - Filling in trip entry rows
 * - Placing checkboxes for freight type
 * - Saving the completed PDF
 */
public class PdfGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PdfGenerator.class);

    // DEBUG MODE - Set to true to overlay coordinates on PDF for position debugging
    // When enabled, small coordinate labels will appear on the PDF showing text positions
    private static boolean debugMode = false;

    // PDF Coordinate Constants - These should be adjusted based on the actual template
    // All coordinates are in points (1/72 inch)

    // ===== HEADER SECTION COORDINATES =====
    private static final float HEADER_MILEAGE_START_X = 475;
    private static final float HEADER_MILEAGE_START_Y = 475;

    private static final float HEADER_MILEAGE_FINISH_X = 475;
    private static final float HEADER_MILEAGE_FINISH_Y = 490;

    private static final float HEADER_TRIP_START_DATE_X = 655;
    private static final float HEADER_TRIP_START_DATE_Y = 490;

    private static final float HEADER_TRIP_END_DATE_X = 655;
    private static final float HEADER_TRIP_END_DATE_Y = 475;

    // ===== CHECKBOX COORDINATES (for Type: EM, TL, LTL) =====
    // These are the positions where "X" will be placed for each freight type
    private static final float CHECKBOX_EM_X = 410;
    private static final float CHECKBOX_EM_Y = 430;

    private static final float CHECKBOX_TL_X = 430;
    private static final float CHECKBOX_TL_Y = 430;

    private static final float CHECKBOX_LTL_X = 455;
    private static final float CHECKBOX_LTL_Y = 430;

    // ===== ROW SECTION COORDINATES =====
    // Row starting Y position and spacing
    private static final float ROW_START_Y = 408;
    private static final float ROW_HEIGHT = 18;  // Space between rows

    // Maximum number of rows the PDF template can hold
    private static final int MAX_ROWS = 15;  // Adjust based on actual template

    // X coordinates for each column in the trip entry rows
    private static final float ROW_MILEAGE_X = 15;
    private static final float ROW_CITY_X = 85;
    private static final float ROW_SHIPPER_X = 185;
    private static final float ROW_CONSIGNEE_X = 290;
    private static final float ROW_TYPE_X = 415; // This is where the checkbox will be placed, so text is not needed
    private static final float ROW_TRAILER_X = 475;
    private static final float ROW_DUTY_CODE_X = 550;

    // Coordinates for detail rows (H/J duty codes) - merged shipper/consignee field
    private static final float DETAIL_ROW_X = 183;
    private static final float DETAIL_ROW_WIDTH = 220;

    // Shared min font floor for the shrink-then-wrap text fitting used across all row columns
    private static final float MIN_FONT = 5;
    private static final float COLUMN_PADDING = 10;
    // When text wraps to a 2nd line, nudge the whole 2-line block up by this much so it doesn't
    // dip into the row underneath. Single-line text is unaffected.
    private static final float WRAP_Y_OFFSET = 6;

    // Column width budgets, derived from the X-coordinate spacing between adjacent columns
    private static final float MILEAGE_WIDTH = ROW_CITY_X - ROW_MILEAGE_X - COLUMN_PADDING;
    private static final float CITY_WIDTH = ROW_SHIPPER_X - ROW_CITY_X - COLUMN_PADDING;
    private static final float SHIPPER_WIDTH = ROW_CONSIGNEE_X - ROW_SHIPPER_X - COLUMN_PADDING;
    private static final float CONSIGNEE_WIDTH = ROW_TYPE_X - ROW_CONSIGNEE_X - COLUMN_PADDING;
    private static final float TRAILER_WIDTH = ROW_DUTY_CODE_X - ROW_TRAILER_X - COLUMN_PADDING;
    private static final float DUTY_CODE_WIDTH = 40;


    private static final float FONT_SIZE = 9;
    private static final String TEMPLATE_RESOURCE_PATH = "templates/Trip Report Form 2026.pdf";

    public PdfGenerator() {
        // PDFBox initialization is done per document
    }

    /**
     * Enable or disable debug mode for coordinate debugging.
     * When enabled, PDF will show coordinate labels next to text fields.
     */
    public void setDebugMode(boolean enabled) {
        debugMode = enabled;
        logger.info("Debug mode {}", enabled ? "enabled" : "disabled");
    }

    /**
     * Get current debug mode status.
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Generate a completed Trip Report PDF and save it to the specified location.
     *
     * @param tripReport The trip report data to fill into the template
     * @param outputPath The path where the completed PDF should be saved
     * @throws IOException If template cannot be read or PDF cannot be written
     */
    public void generatePdf(TripReport tripReport, String outputPath) throws IOException {
        // Load template PDF from resources
        PDDocument document = loadTemplate();

        try {
            PDPage page = document.getPage(0);

            // Fill in header information
            fillHeaderFields(document, page, tripReport);

            // Fill in trip entry rows
            fillTripRows(document, page, tripReport);

            // Fill in checkboxes based on freight type
            fillCheckboxes(document, page, tripReport);

            // Save the completed PDF
            document.save(outputPath);
            logger.info("PDF generated successfully: {}", outputPath);

        } finally {
            document.close();
        }
    }

    /**
     * Load the PDF template from resources.
     *
     * @return PDDocument representing the template
     * @throws IOException If template cannot be found or loaded
     */
    private PDDocument loadTemplate() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        var resourceStream = classLoader.getResourceAsStream(TEMPLATE_RESOURCE_PATH);
        
        if (resourceStream == null) {
            // Try alternate methods for better error reporting
            var resourceUrl = classLoader.getResource(TEMPLATE_RESOURCE_PATH);
            if (resourceUrl == null) {
                throw new IOException("Template file not found in classpath: " + TEMPLATE_RESOURCE_PATH + 
                    ". Please ensure Trip Report Form 2026.pdf is in src/main/resources/templates/");
            }
            try {
                resourceStream = resourceUrl.openStream();
            } catch (Exception e) {
                throw new IOException("Unable to open template: " + e.getMessage());
            }
        }
        
        logger.info("Loading template from: {}", TEMPLATE_RESOURCE_PATH);
        return PDDocument.load(resourceStream);
    }

    /**
     * Fill in the header fields (mileage start, mileage finish, dates).
     */
    private void fillHeaderFields(PDDocument document, PDPage page, TripReport tripReport) throws IOException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        // Mileage Start
        if (tripReport.getMileageStart() != null && !tripReport.getMileageStart().isEmpty()) {
            addTextToPage(document, page, tripReport.getMileageStart(),
                    HEADER_MILEAGE_START_X, HEADER_MILEAGE_START_Y);
        }

        // Mileage Finish
        if (tripReport.getMileageFinish() != null && !tripReport.getMileageFinish().isEmpty()) {
            addTextToPage(document, page, tripReport.getMileageFinish(),
                    HEADER_MILEAGE_FINISH_X, HEADER_MILEAGE_FINISH_Y);
        }

        // Trip Start Date
        if (tripReport.getTripStartDate() != null) {
            String formattedStartDate = tripReport.getTripStartDate().format(dateFormatter);
            addTextToPage(document, page, formattedStartDate,
                    HEADER_TRIP_START_DATE_X, HEADER_TRIP_START_DATE_Y);
        }

        // Trip End Date
        if (tripReport.getTripEndDate() != null) {
            String formattedEndDate = tripReport.getTripEndDate().format(dateFormatter);
            addTextToPage(document, page, formattedEndDate,
                    HEADER_TRIP_END_DATE_X, HEADER_TRIP_END_DATE_Y);
        }
    }

    /**
     * Fill in the trip entry rows.
     */
    private void fillTripRows(PDDocument document, PDPage page, TripReport tripReport) throws IOException {
        List<TripEntry> entries = tripReport.getEntries();

        for (int i = 0; i < entries.size() && i < MAX_ROWS; i++) {
            TripEntry entry = entries.get(i);

            // Skip empty rows
            if (!entry.isPopulated()) {
                continue;
            }

            float yPosition = ROW_START_Y - (i * ROW_HEIGHT);

            if (entry.isDetailRow()) {
                drawDetailRow(document, page, entry, yPosition);
                continue;
            }

            // Mileage
            if (entry.getMileage() != null && !entry.getMileage().isEmpty()) {
                addFittedTextToPage(document, page, entry.getMileage(), ROW_MILEAGE_X, yPosition, MILEAGE_WIDTH);
            }

            // City/Zip Code
            if (entry.getCityZipCode() != null && !entry.getCityZipCode().isEmpty()) {
                addFittedTextToPage(document, page, entry.getCityZipCode(), ROW_CITY_X, yPosition, CITY_WIDTH);
            }

            // Shipper
            if (entry.getShipper() != null && !entry.getShipper().isEmpty()) {
                addFittedTextToPage(document, page, entry.getShipper(), ROW_SHIPPER_X, yPosition, SHIPPER_WIDTH);
            }

            // Consignee
            if (entry.getConsignee() != null && !entry.getConsignee().isEmpty()) {
                addFittedTextToPage(document, page, entry.getConsignee(), ROW_CONSIGNEE_X, yPosition, CONSIGNEE_WIDTH);
            }

            // Type (will be handled by checkboxes)
            // Trailer Number
            if (entry.getTrailerNumber() != null && !entry.getTrailerNumber().isEmpty()) {
                addFittedTextToPage(document, page, entry.getTrailerNumber(), ROW_TRAILER_X, yPosition, TRAILER_WIDTH);
            }

            // Duty Code
            if (entry.getDutyCode() != null && !entry.getDutyCode().isEmpty()) {
                addFittedTextToPage(document, page, entry.getDutyCode(), ROW_DUTY_CODE_X, yPosition, DUTY_CODE_WIDTH);
            }
        }
    }

    /**
     * Fill in checkboxes based on the Type field for each row.
     * Places an "X" in the appropriate checkbox position.
     */
    private void fillCheckboxes(PDDocument document, PDPage page, TripReport tripReport) throws IOException {
        List<TripEntry> entries = tripReport.getEntries();

        for (int i = 0; i < entries.size() && i < MAX_ROWS; i++) {
            TripEntry entry = entries.get(i);

            if (!entry.isPopulated()) {
                continue;
            }

            String type = entry.getType();
            float yPosition = ROW_START_Y - (i * ROW_HEIGHT);

            if (type != null && !type.isEmpty()) {
                switch (type.toUpperCase()) {
                    case "EM":
                        addTextToPage(document, page, "X", CHECKBOX_EM_X, yPosition);
                        break;
                    case "TL":
                        addTextToPage(document, page, "X", CHECKBOX_TL_X, yPosition);
                        break;
                    case "LTL":
                        addTextToPage(document, page, "X", CHECKBOX_LTL_X, yPosition);
                        break;
                    default:
                        logger.warn("Unknown freight type: {}", type);
                }
            }
        }
    }

    /* Draw detail rows for H/J duty codes, which use a merged shipper/consignee field for explanations.
     * These rows can have variable length text, so we need to fit it within a defined area.
     */
    private void drawDetailRow(
        PDDocument document,
        PDPage page,
        TripEntry entry,
        float yPosition) throws IOException {

        String text = entry.getDetailText();

        if (text == null || text.trim().isEmpty()) {
            String shipper = entry.getShipper() == null ? "" : entry.getShipper();
            String consignee = entry.getConsignee() == null ? "" : entry.getConsignee();
            text = shipper + "    " + consignee;
        }

        if (text.trim().isEmpty()) {
            return;
        }

        addFittedTextToPage(
                document,
                page,
                text,
                DETAIL_ROW_X,
                yPosition,
                DETAIL_ROW_WIDTH);
    }

    
    /**
     * Measure a string's width in points at a given font size.
     */
    private float measureWidth(String text, float fontSize) throws IOException {
        return PDType1Font.HELVETICA.getStringWidth(text) / 1000f * fontSize;
    }

    /**
     * Greedily wrap text into two lines that each fit within maxWidth at the given font size.
     * If a single word still overflows maxWidth on its own, it is left on its own line as-is
     * rather than being truncated.
     */
    private String[] wrapToTwoLines(String text, float fontSize, float maxWidth) throws IOException {
        String[] words = text.trim().split("\\s+");
        StringBuilder firstLine = new StringBuilder();
        int splitIndex = words.length;

        for (int i = 0; i < words.length; i++) {
            String candidate = firstLine.length() == 0 ? words[i] : firstLine + " " + words[i];
            if (firstLine.length() > 0 && measureWidth(candidate, fontSize) > maxWidth) {
                splitIndex = i;
                break;
            }
            firstLine = new StringBuilder(candidate);
        }

        String secondLine = String.join(" ", Arrays.copyOfRange(words, splitIndex, words.length));
        return new String[] { firstLine.toString(), secondLine };
    }

    /**
     * Fit the provided text within the specified width by shrinking the font size, and if it still
     * doesn't fit at the minimum font size, wrap it onto a second line within the same row.
     */
    private void addFittedTextToPage(
        PDDocument document,
        PDPage page,
        String text,
        float x,
        float y,
        float maxWidth) throws IOException {

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        String trimmed = text.trim();
        float fontSize = FONT_SIZE;
        boolean fitsOnOneLine = false;

        while (true) {
            if (measureWidth(trimmed, fontSize) <= maxWidth) {
                fitsOnOneLine = true;
                break;
            }
            if (fontSize <= MIN_FONT) {
                break;
            }
            fontSize -= 0.5f;
        }

        PDPageContentStream contentStream = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true, true);

        try {
            if (fitsOnOneLine) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(trimmed);
                contentStream.endText();
            } else {
                // Doesn't fit on one line even at the minimum font size - wrap to a second line.
                // Nudge the whole 2-line block up so the 2nd line has room without dipping into
                // the row underneath; single-line text (the common case) is left untouched.
                String[] lines = wrapToTwoLines(trimmed, MIN_FONT, maxWidth);
                float lineHeight = MIN_FONT + 1;
                float wrappedY = y + WRAP_Y_OFFSET;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, MIN_FONT);
                contentStream.newLineAtOffset(x, wrappedY);
                contentStream.showText(lines[0]);
                contentStream.endText();

                if (!lines[1].isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, MIN_FONT);
                    contentStream.newLineAtOffset(x, wrappedY - lineHeight);
                    contentStream.showText(lines[1]);
                    contentStream.endText();
                }
            }

            if (debugMode) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 6);
                contentStream.newLineAtOffset(x, y + 8);
                contentStream.showText(String.format("(%.0f,%.0f)", x, y));
                contentStream.endText();
            }
        } finally {
            contentStream.close();
        }
    }

    /**
     * Add text to a specific position on the PDF page.
     * This method writes text directly to the PDF using PDFBox.
     *
     * @param document The PDF document
     * @param page The page to add text to
     * @param text The text to add
     * @param x X coordinate in points
     * @param y Y coordinate in points
     * @throws IOException If text cannot be added
     */
    private void addTextToPage(PDDocument document, PDPage page, String text, float x, float y) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return;  // Skip empty text
        }

        PDPageContentStream contentStream = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true, true);
        
        try {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text.trim());
            contentStream.endText();
            
            // DEBUG MODE: Add coordinate labels above each field
            if (debugMode) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 6);  // Smaller font for coordinates
                contentStream.newLineAtOffset(x, y + 8);  // Slightly above the text
                contentStream.showText(String.format("(%.0f,%.0f)", x, y));
                contentStream.endText();
            }
            
            logger.debug("Added text '{}' at position ({}, {})", text, x, y);
        } finally {
            contentStream.close();
        }
    }

    /**
     * Get the maximum number of rows this template can hold.
     * @return Maximum number of rows
     */
    public int getMaxRows() {
        return MAX_ROWS;
    }

    /**
     * Validate that the trip report can be exported.
     * @param tripReport The trip report to validate
     * @return List of validation errors (empty if valid)
     */
    public List<String> validateTripReport(TripReport tripReport) {
        List<String> errors = new ArrayList<>();

        // Check mileage start
        if (tripReport.getMileageStart() == null || tripReport.getMileageStart().trim().isEmpty()) {
            errors.add("Mileage Start is required");
        } else {
            try {
                Long.parseLong(tripReport.getMileageStart().trim());
            } catch (NumberFormatException e) {
                errors.add("Mileage Start must be numeric");
            }
        }

        // Check mileage finish
        if (tripReport.getMileageFinish() == null || tripReport.getMileageFinish().trim().isEmpty()) {
            errors.add("Mileage Finish is required");
        } else {
            try {
                Long.parseLong(tripReport.getMileageFinish().trim());
            } catch (NumberFormatException e) {
                errors.add("Mileage Finish must be numeric");
            }
        }

        // Check trip dates
        if (tripReport.getTripStartDate() == null) {
            errors.add("Trip Start Date is required");
        }
        if (tripReport.getTripEndDate() == null) {
            errors.add("Trip End Date is required");
        }

        // Validate populated rows
        for (int i = 0; i < tripReport.getEntries().size(); i++) {
            TripEntry entry = tripReport.getEntries().get(i);
            if (!entry.isPopulated()) {
                continue;
            }

            String rowNum = "Row " + (i + 1);

            // Detail rows (H/J duty codes) have relaxed validation - only merged fields
            if (entry.isDetailRow()) {
                // Detail rows are optional and only contain shipper/consignee for explanation
                continue;
            }

            // Normal rows require all fields
            if (entry.getMileage() == null || entry.getMileage().trim().isEmpty()) {
                errors.add(rowNum + ": Mileage is required");
            }
            if (entry.getCityZipCode() == null || entry.getCityZipCode().trim().isEmpty()) {
                errors.add(rowNum + ": City/Zip Code is required");
            }
            if (entry.getShipper() == null || entry.getShipper().trim().isEmpty()) {
                errors.add(rowNum + ": Shipper is required");
            }
            if (entry.getConsignee() == null || entry.getConsignee().trim().isEmpty()) {
                errors.add(rowNum + ": Consignee is required");
            }
            if (entry.getType() == null || entry.getType().trim().isEmpty()) {
                errors.add(rowNum + ": Type is required");
            }
            if (entry.getDutyCode() == null || entry.getDutyCode().trim().isEmpty()) {
                errors.add(rowNum + ": Duty Code is required");
            }
        }

        return errors;
    }
}
