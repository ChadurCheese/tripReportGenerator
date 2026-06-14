package com.tripreport.ui;

import com.tripreport.model.TripEntry;
import com.tripreport.model.TripReport;
import com.tripreport.pdf.PdfGenerator;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main UI for the Trip Report Generator application.
 *
 * Provides:
 * - Header fields for mileage and trip dates
 * - Table for trip entries
 * - Buttons to add/remove rows
 * - Preview and Generate PDF buttons
 */
public class MainView extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainView.class);
    private static final String APP_TITLE = "Trip Report Generator";

    private TripReport tripReport;
    private PdfGenerator pdfGenerator;
    private String lastPreviewFilePath;  // Track preview file for cleanup

    // Header fields
    private TextField mileageStartField;
    private TextField mileageFinishField;
    private Label totalMileageLabel;
    private DatePicker tripStartDatePicker;
    private DatePicker tripEndDatePicker;

    // Trip entries table
    private TableView<TripEntry> tripTable;
    private ObservableList<TripEntry> tripEntries;

    // Buttons
    private Button addRowButton;
    private Button removeRowButton;
    private Button previewButton;
    private Button generateButton;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize model and PDF generator
            tripReport = new TripReport();
            pdfGenerator = new PdfGenerator();

            // Create main layout
            BorderPane root = createMainLayout();

            // Create scene
            Scene scene = new Scene(root, 1200, 800);

            // Set up stage
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.show();

            logger.info("Application started successfully");
        } catch (Exception e) {
            logger.error("Error starting application", e);
            showErrorDialog("Error", "Failed to start application", e.getMessage());
        }
    }

    /**
     * Create the main layout with all UI components.
     */
    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Create sections in order (buttons before table to avoid null pointer)
        VBox headerSection = createHeaderSection();
        HBox buttonSection = createButtonSection();
        VBox tableSection = createTableSection();

        // Combine sections
        VBox mainContent = new VBox(15);
        mainContent.getChildren().addAll(headerSection, tableSection, buttonSection);
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        root.setCenter(mainContent);
        return root;
    }

    /**
     * Create the header section with mileage and date fields.
     */
    private VBox createHeaderSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 10;");

        Label sectionTitle = new Label("Trip Information");
        sectionTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Mileage row
        HBox mileageBox = new HBox(20);
        mileageBox.setAlignment(Pos.CENTER_LEFT);

        Label mileageStartLabel = new Label("Mileage Start:");
        mileageStartLabel.setPrefWidth(120);
        mileageStartField = new TextField();
        mileageStartField.setPrefWidth(100);
        mileageStartField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[0-9]*") ? change : null));
        mileageStartField.setOnKeyReleased(e -> updateTotalMileage());

        Label mileageFinishLabel = new Label("Mileage Finish:");
        mileageFinishLabel.setPrefWidth(120);
        mileageFinishField = new TextField();
        mileageFinishField.setPrefWidth(100);
        mileageFinishField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[0-9]*") ? change : null));
        mileageFinishField.setOnKeyReleased(e -> updateTotalMileage());

        Label totalMileageLabelText = new Label("Total Mileage:");
        totalMileageLabelText.setPrefWidth(120);
        totalMileageLabel = new Label("0");
        totalMileageLabel.setPrefWidth(100);
        totalMileageLabel.setStyle("-fx-padding: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 3;");

        mileageBox.getChildren().addAll(
                mileageStartLabel, mileageStartField,
                mileageFinishLabel, mileageFinishField,
                totalMileageLabelText, totalMileageLabel
        );

        // Date row
        HBox dateBox = new HBox(20);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        Label tripStartDateLabel = new Label("Trip Start Date:");
        tripStartDateLabel.setPrefWidth(120);
        tripStartDatePicker = new DatePicker();
        tripStartDatePicker.setPrefWidth(150);

        Label tripEndDateLabel = new Label("Trip End Date:");
        tripEndDateLabel.setPrefWidth(120);
        tripEndDatePicker = new DatePicker();
        tripEndDatePicker.setPrefWidth(150);

        dateBox.getChildren().addAll(
                tripStartDateLabel, tripStartDatePicker,
                tripEndDateLabel, tripEndDatePicker
        );

        // Debug checkbox row
        HBox debugBox = new HBox(20);
        debugBox.setAlignment(Pos.CENTER_LEFT);
        CheckBox debugCheckBox = new CheckBox("Debug Mode (shows coordinates on PDF)");
        debugCheckBox.setStyle("-fx-font-size: 11;");
        debugCheckBox.setOnAction(e -> pdfGenerator.setDebugMode(debugCheckBox.isSelected()));
        debugBox.getChildren().add(debugCheckBox);

        section.getChildren().addAll(sectionTitle, mileageBox, dateBox, debugBox);
        return section;
    }

    /**
     * Create the table section for trip entries.
     */
    private VBox createTableSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 10;");

        Label sectionTitle = new Label("Trip Entries");
        sectionTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Create table columns
        tripTable = new TableView<>();
        tripTable.setEditable(true);

        // Mileage column
        TableColumn<TripEntry, String> mileageCol = createEditableColumn("Mileage", "mileage", 80);

        // City/Zip Code column
        TableColumn<TripEntry, String> cityCol = createEditableColumn("City / Zip Code", "cityZipCode", 100);

        // Shipper column
        TableColumn<TripEntry, String> shipperCol = createEditableColumn("Shipper", "shipper", 110);

        // Consignee column
        TableColumn<TripEntry, String> consigneeCol = createEditableColumn("Consignee", "consignee", 110);

        // Type column (ComboBox)
        TableColumn<TripEntry, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(70);
        typeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        typeCol.setCellFactory(col -> new ComboBoxTableCell<>(FXCollections.observableArrayList("EM", "TL", "LTL")));
        typeCol.setOnEditCommit(event ->
                event.getRowValue().setType(event.getNewValue() != null ? event.getNewValue() : "")
        );

        // Trailer Number column
        TableColumn<TripEntry, String> trailerCol = createEditableColumn("Trailer Number", "trailerNumber", 100);

        // Duty Code column (ComboBox)
        TableColumn<TripEntry, String> dutyCol = new TableColumn<>("Duty Code");
        dutyCol.setPrefWidth(80);
        dutyCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDutyCode()));
        dutyCol.setCellFactory(col -> new ComboBoxTableCell<>(FXCollections.observableArrayList(
                "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "N", "S", "T"
        )));
        dutyCol.setOnEditCommit(event -> {
            int rowIndex = event.getTablePosition().getRow();
            TripEntry entry = event.getRowValue();
            String newDutyCode = event.getNewValue();
            entry.setDutyCode(newDutyCode != null ? newDutyCode : "");
            
            // Handle H and J duty codes - they require a detail row below
            if ("H".equals(newDutyCode) || "J".equals(newDutyCode)) {
                handleHJDutyCode(rowIndex, entry);
            }
        });

        tripTable.getColumns().addAll(mileageCol, cityCol, shipperCol, consigneeCol, typeCol, trailerCol, dutyCol);

        // Initialize table data
        tripEntries = FXCollections.observableArrayList();
        tripTable.setItems(tripEntries);

        // Add initial empty row
        addEmptyRow();

        section.getChildren().addAll(sectionTitle, tripTable);
        VBox.setVgrow(tripTable, Priority.ALWAYS);

        return section;
    }

    /**
     * Create an editable text column for the table.
     */
    private TableColumn<TripEntry, String> createEditableColumn(String headerText, String property, double width) {
        TableColumn<TripEntry, String> col = new TableColumn<>(headerText);
        col.setPrefWidth(width);

        col.setCellValueFactory(cellData -> {
            String value = switch (property) {
                case "mileage" -> cellData.getValue().getMileage();
                case "cityZipCode" -> cellData.getValue().getCityZipCode();
                case "shipper" -> cellData.getValue().getShipper();
                case "consignee" -> cellData.getValue().getConsignee();
                case "trailerNumber" -> cellData.getValue().getTrailerNumber();
                default -> "";
            };
            return new javafx.beans.property.SimpleStringProperty(value);
        });

        col.setCellFactory(column -> new EditingCell());
        col.setOnEditCommit(event -> {
            TripEntry entry = event.getRowValue();
            String newValue = event.getNewValue();

            switch (property) {
                case "mileage" -> entry.setMileage(newValue != null ? newValue : "");
                case "cityZipCode" -> entry.setCityZipCode(newValue != null ? newValue : "");
                case "shipper" -> entry.setShipper(newValue != null ? newValue : "");
                case "consignee" -> entry.setConsignee(newValue != null ? newValue : "");
                case "trailerNumber" -> entry.setTrailerNumber(newValue != null ? newValue : "");
            }

            // Refresh table to show updates
            tripTable.refresh();
        });

        return col;
    }

    /**
     * Create the button section with Add, Remove, Preview, and Generate buttons.
     */
    private HBox createButtonSection() {
        HBox section = new HBox(10);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setPadding(new Insets(10));

        addRowButton = new Button("Add Row");
        addRowButton.setPrefWidth(100);
        addRowButton.setOnAction(e -> addEmptyRow());

        removeRowButton = new Button("Remove Selected Row");
        removeRowButton.setPrefWidth(150);
        removeRowButton.setOnAction(e -> removeSelectedRow());

        Button loadSampleButton = new Button("Load Sample Data");
        loadSampleButton.setPrefWidth(140);
        loadSampleButton.setStyle("-fx-font-size: 10;");
        loadSampleButton.setOnAction(e -> loadSampleData());

        Separator spacer = new Separator();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        previewButton = new Button("Preview");
        previewButton.setPrefWidth(100);
        previewButton.setStyle("-fx-font-size: 12;");
        previewButton.setOnAction(e -> previewPdf());

        generateButton = new Button("Generate PDF");
        generateButton.setPrefWidth(120);
        generateButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        generateButton.setOnAction(e -> generatePdf());

        section.getChildren().addAll(addRowButton, removeRowButton, loadSampleButton, spacer, previewButton, generateButton);

        return section;
    }

    /**
     * Add an empty row to the table.
     */
    private void addEmptyRow() {
        if (tripEntries.size() < pdfGenerator.getMaxRows()) {
            tripEntries.add(new TripEntry());
            updateAddRowButtonState();
        } else {
            showWarningDialog("Maximum Rows Reached",
                    "Cannot add more rows. This PDF template supports a maximum of " + pdfGenerator.getMaxRows() + " entries.");
        }
    }

    /**
     * Remove the selected row from the table.
     */
    private void removeSelectedRow() {
        int selectedIndex = tripTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && tripEntries.size() > 1) {
            tripEntries.remove(selectedIndex);
            updateAddRowButtonState();
        } else if (tripEntries.size() <= 1) {
            showWarningDialog("Cannot Remove", "You must keep at least one row.");
        } else {
            showWarningDialog("No Selection", "Please select a row to remove.");
        }
    }

    /**
     * Handle H and J duty codes - automatically create a detail row below the current row.
     * Detail rows have merged shipper/consignee cells and are used for extra explanation.
     */
    private void handleHJDutyCode(int rowIndex, TripEntry parentEntry) {

        if (rowIndex + 1 < tripEntries.size()) {
            TripEntry nextRow = tripEntries.get(rowIndex + 1);

            if (nextRow.isDetailRow()) {
                tripTable.refresh();
                return;
            }
        }

        if (tripEntries.size() < pdfGenerator.getMaxRows()) {
            TripEntry detailRow = new TripEntry();
            detailRow.setDetailRow(true);

            // Leave fields empty for user input

            tripEntries.add(rowIndex + 1, detailRow);
            updateAddRowButtonState();
        }

        tripTable.refresh();
    }

    /**
     * Update the trip report model from UI fields.
     */
    private void updateTripReportFromUI() {
        tripReport.setMileageStart(mileageStartField.getText());
        tripReport.setMileageFinish(mileageFinishField.getText());
        tripReport.setTripStartDate(tripStartDatePicker.getValue());
        tripReport.setTripEndDate(tripEndDatePicker.getValue());
        tripReport.setEntries(new java.util.ArrayList<>(tripEntries));
    }

    /**
     * Update the total mileage label based on start and finish values.
     */
    private void updateTotalMileage() {
        tripReport.setMileageStart(mileageStartField.getText());
        tripReport.setMileageFinish(mileageFinishField.getText());

        long totalMileage = tripReport.getTotalMileage();
        if (totalMileage >= 0) {
            totalMileageLabel.setText(String.valueOf(totalMileage));
        } else {
            totalMileageLabel.setText("-");
        }
    }

    /**
     * Update the state of the Add Row button based on row count.
     */
    private void updateAddRowButtonState() {
        if (addRowButton != null) {
            addRowButton.setDisable(tripEntries.size() >= pdfGenerator.getMaxRows());
        }
    }

    /**
     * Load sample data into the form for testing purposes.
     */
    private void loadSampleData() {
        // Clear existing entries
        tripEntries.clear();

        // Fill header
        mileageStartField.setText("12500");
        mileageFinishField.setText("12650");
        tripStartDatePicker.setValue(java.time.LocalDate.of(2026, 6, 14));
        tripEndDatePicker.setValue(java.time.LocalDate.of(2026, 6, 15));

        // Add sample trip entries
        TripEntry entry1 = new TripEntry("12500", "Chicago, IL 60601", "ABC Shipping", "XYZ Receiver", "TL", "TR-001", "A");
        tripEntries.add(entry1);

        TripEntry entry2 = new TripEntry("12575", "Indianapolis, IN 46202", "Quick Delivery", "Fast Logistics", "LTL", "TR-002", "B");
        tripEntries.add(entry2);

        TripEntry entry3 = new TripEntry("12650", "Cincinnati, OH 45202", "Premium Transport", "Standard Warehouse", "EM", "TR-003", "J");
        tripEntries.add(entry3);

        // Add detail row for H/J duty code
        TripEntry detailRow = new TripEntry();
        detailRow.setDetailRow(true);
        detailRow.setShipper("Special handling for hazmat materials");
        detailRow.setConsignee("See shipping documentation");
        tripEntries.add(detailRow);

        tripTable.refresh();
        updateTotalMileage();
        updateAddRowButtonState();
        logger.info("Sample data loaded for testing");
    }

    /**
     * Generate a preview PDF and open it in the system viewer.
     */
    private void previewPdf() {
        try {
            // Delete previous preview file if it exists
            if (lastPreviewFilePath != null) {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(lastPreviewFilePath));
            }

            updateTripReportFromUI();

            // Validate input
            List<String> validationErrors = pdfGenerator.validateTripReport(tripReport);
            if (!validationErrors.isEmpty()) {
                showErrorDialog("Validation Error", "Please fix the following errors:",
                        String.join("\n", validationErrors));
                return;
            }

            // Generate temporary PDF with timestamp to avoid conflicts
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            String timestamp = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String tempPath = System.getProperty("java.io.tmpdir") + File.separator + "TripReport_Preview_" + timestamp + ".pdf";
            lastPreviewFilePath = tempPath;  // Track for later deletion
            
            pdfGenerator.generatePdf(tripReport, tempPath);

            // Open with system viewer
            File file = new File(tempPath);
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
                logger.info("Preview PDF opened: {}", tempPath);
                
                // Schedule deletion of preview file after a delay (gives time for viewer to open)
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(tempPath));
                            logger.info("Temporary preview file deleted: {}", tempPath);
                        } catch (Exception e) {
                            logger.warn("Could not delete temporary preview file: {}", tempPath);
                        }
                    }
                }, 5000);  // Delete after 5 seconds to allow viewer time to open
            }
        } catch (Exception e) {
            logger.error("Error generating preview", e);
            showErrorDialog("Error", "Failed to generate preview", e.getMessage());
        }
    }

    /**
     * Generate and save the final PDF.
     */
    private void generatePdf() {
        try {
            updateTripReportFromUI();

            // Validate input
            List<String> validationErrors = pdfGenerator.validateTripReport(tripReport);
            if (!validationErrors.isEmpty()) {
                showErrorDialog("Validation Error", "Please fix the following errors:",
                        String.join("\n", validationErrors));
                return;
            }

            // Show save dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Trip Report PDF");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Downloads"));

            // Generate filename with timestamp
            String filename = generateFileName();
            fileChooser.setInitialFileName(filename);

            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File selectedFile = fileChooser.showSaveDialog(((Button) generateButton).getScene().getWindow());

            if (selectedFile != null) {
                pdfGenerator.generatePdf(tripReport, selectedFile.getAbsolutePath());
                showInfoDialog("Success", "PDF Generated Successfully",
                        "Your Trip Report has been saved to:\n" + selectedFile.getAbsolutePath());
                logger.info("PDF generated successfully: {}", selectedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Error generating PDF", e);
            showErrorDialog("Error", "Failed to generate PDF", e.getMessage());
        }
    }

    /**
     * Generate a filename with the current date and time.
     */
    private String generateFileName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        return "TripReport_" + java.time.LocalDateTime.now().format(formatter) + ".pdf";
    }

    /**
     * Show an error dialog.
     */
    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show a warning dialog.
     */
    private void showWarningDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show an information dialog.
     */
    private void showInfoDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Custom table cell for editing text fields in table.
     */
    private static class EditingCell extends TableCell<TripEntry, String> {
        private TextField textField;

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
                textField.requestFocus();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                setGraphic(null);
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    commitEdit(textField.getText());
                } else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    commitEdit(textField.getText());
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }

    /**
     * Custom cell for ComboBox in table.
     */
    private static class ComboBoxTableCell<S, T> extends TableCell<S, T> {
        private final ObservableList<T> items;
        private ComboBox<T> comboBox;

        public ComboBoxTableCell(ObservableList<T> items) {
            this.items = items;
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createComboBox();
                setText(null);
                setGraphic(comboBox);
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem() == null ? "" : getItem().toString());
            setGraphic(null);
        }

        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item == null ? "" : item.toString());
                setGraphic(null);
            }
        }

        private void createComboBox() {
            comboBox = new ComboBox<>(items);
            comboBox.setValue(getItem());
            comboBox.setOnAction(event -> {
                T selectedValue = comboBox.getValue();
                if (selectedValue != null) {
                    commitEdit(selectedValue);
                }
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
