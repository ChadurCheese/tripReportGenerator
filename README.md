# Trip Report PDF Generator

A simple desktop application for truck drivers to generate completed Trip Report PDFs from a pre-existing company PDF template.

## Features

- **Simple Data Entry**: Clean, user-friendly form for entering trip information
- **PDF Generation**: Automatically fills a PDF template with entered data
- **Preview**: Preview the generated PDF before saving
- **Validation**: User-friendly error messages for invalid input
- **No Technical Knowledge Required**: Designed for non-technical users

## Technology Stack

- **Language**: Java 21
- **UI Framework**: JavaFX
- **PDF Library**: Apache PDFBox
- **Build Tool**: Maven

## Prerequisites

- Java 21 or higher (JDK)
- Maven 3.8.1 or higher

## Installation

### 1. Download and Install Java 21

Download from: https://www.oracle.com/java/technologies/downloads/#java21

Verify installation:
```bash
java -version
```

### 2. Download and Install Maven

Download from: https://maven.apache.org/download.cgi

Verify installation:
```bash
mvn -version
```

### 3. Set Up the Project

Clone or extract the project to your desired directory.

## Adding the PDF Template

1. Obtain the Trip Report Form 2026.pdf from your company
2. Place the PDF file in: `src/main/resources/templates/Trip Report Form 2026.pdf`

**Note**: The application assumes the PDF template exists at this location. The file should NOT be renamed.

## Building the Project

### Using Maven

From the project root directory:

```bash
mvn clean install
```

This command will:
- Download dependencies
- Compile source code
- Run tests (if any)
- Package the application

### Build Output

The compiled JAR will be available at: `target/trip-report-generator-1.0.0-shaded.jar`

## Running the Application

### From Maven

```bash
mvn javafx:run
```

### From the Command Line (with compiled JAR)

```bash
java -jar target/trip-report-generator-1.0.0-shaded.jar
```

### From an IDE

Most IDEs (IntelliJ IDEA, Eclipse, VS Code) can open Maven projects directly:

1. Open the project folder
2. Maven dependencies will auto-download
3. Run `Main.java` from the IDE

## Usage

### Starting the Application

1. Launch the application using one of the methods above
2. The main window will open

### Entering Trip Information

1. **Mileage Start**: Enter the odometer reading at trip start (numeric only)
2. **Mileage Finish**: Enter the odometer reading at trip end (numeric only)
3. **Total Mileage**: Automatically calculated (read-only)
4. **Trip Start Date**: Click the date picker to select the trip start date
5. **Trip End Date**: Click the date picker to select the trip end date

### Adding Trip Entries

1. Click **Add Row** to add a new trip entry
2. Fill in the row data:
   - **Mileage**: Odometer reading at this location
   - **City / Zip Code**: Location information
   - **Shipper**: Name of the shipper
   - **Consignee**: Name of the consignee
   - **Type**: Select from EM, TL, or LTL (dropdown)
   - **Trailer Number**: Trailer identification number
   - **Duty Code**: Select from A, B, C, D, E, F, G, H, J, K, N, S, or T (dropdown)

### Removing Entries

1. Click on a row to select it
2. Click **Remove Selected Row** to delete it
3. At least one row must remain in the table

### Previewing the PDF

1. Click **Preview** to generate a temporary PDF
2. The PDF will open in your system's default PDF viewer
3. Review the filled information
4. Close the preview window to return to the application

### Generating the Final PDF

1. Click **Generate PDF**
2. A file save dialog will open
3. Choose a location to save the PDF (defaults to Downloads folder)
4. The filename is automatically formatted as: `TripReport_YYYY-MM-DD_HH-mm.pdf`
5. Click Save
6. A confirmation message will appear with the file location

## Project Structure

```
tripReportGenerator/
├── pom.xml                          # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/com/tripreport/
│   │   │   ├── app/
│   │   │   │   └── Main.java        # Application entry point
│   │   │   ├── model/
│   │   │   │   ├── TripEntry.java   # Data model for single entry
│   │   │   │   └── TripReport.java  # Data model for complete report
│   │   │   ├── ui/
│   │   │   │   └── MainView.java    # JavaFX UI implementation
│   │   │   └── pdf/
│   │   │       └── PdfGenerator.java # PDF generation logic
│   │   └── resources/
│   │       ├── templates/
│   │       │   └── Trip Report Form 2026.pdf  # PDF template (add manually)
│   │       └── logback.xml          # Logging configuration
│   └── test/                        # Test files (if any)
├── target/                          # Build output
└── README.md                        # This file
```

## PDF Coordinate Configuration

The PDF coordinates are defined as constants in `PdfGenerator.java`. If your template has different dimensions or field positions, you can adjust:

```java
// Example from PdfGenerator.java
private static final float HEADER_MILEAGE_START_X = 100;
private static final float HEADER_MILEAGE_START_Y = 750;
```

To find the correct coordinates:
1. Open the PDF template in a PDF editor or coordinate inspector
2. Identify the X,Y position of each field
3. Update the constants in `PdfGenerator.java`
4. Rebuild the project

## Troubleshooting

### "Template file not found" Error

- Ensure the PDF file is placed at: `src/main/resources/templates/Trip Report Form 2026.pdf`
- Check that the filename is spelled exactly as specified
- Make sure to rebuild the project after adding the template

### JavaFX Runtime Components Missing

If you see errors about JavaFX missing:
```bash
# Make sure dependencies are downloaded
mvn dependency:resolve

# Then rebuild
mvn clean install
```

### PDF Opens But Fields Are Blank

The PDF coordinates may not match your template. Check `PdfGenerator.java` and adjust the X,Y constants based on your template dimensions.

### Application Won't Start

1. Ensure Java 21 is installed: `java -version`
2. Try clearing Maven cache: `mvn clean`
3. Check that all dependencies downloaded: `mvn dependency:resolve`
4. Check the log file: `trip-report-generator.log`

## Logging

Application logs are written to:
- **Console**: Visible during execution
- **File**: `trip-report-generator.log` in the working directory

Log level can be adjusted in `src/main/resources/logback.xml`

## Validation Rules

The application enforces the following validation:

**Header Fields:**
- Mileage Start: Required, must be numeric
- Mileage Finish: Required, must be numeric
- Trip Start Date: Required
- Trip End Date: Required

**For Each Populated Row:**
- Mileage: Required
- City/Zip Code: Required
- Shipper: Required
- Consignee: Required
- Type: Required (EM, TL, or LTL)
- Duty Code: Required (A-K, N, S, or T)

## Limitations

- The application supports a maximum number of trip entries based on your PDF template (currently set to 15)
- The PDF template fields that are pre-filled (Driver Name, Driver Number, Power Unit) are not editable
- The application does not support editing of generated PDFs

## Future Enhancements

Potential features for future versions:
- Remember last save location
- Remember window size and position
- Auto-open generated PDF after saving
- Keyboard shortcuts
- Dark mode
- Data persistence between sessions

## Support

For issues or questions:
1. Check the `trip-report-generator.log` file for error details
2. Verify the PDF template is correctly placed
3. Ensure Java 21 is installed
4. Check that all fields have valid input before generating

## License

[Add your license information here]

## Version

**Current Version**: 1.0.0
**Release Date**: June 2026
**Tested With**: Java 21, JavaFX 21.0.3, Apache PDFBox 3.0.1
