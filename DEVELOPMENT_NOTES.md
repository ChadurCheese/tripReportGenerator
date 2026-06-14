# Development Notes - Trip Report PDF Generator

## Architecture Overview

### Package Structure

```
com.tripreport
├── app          - Application entry point
├── model        - Data model classes
├── ui           - JavaFX UI components
└── pdf          - PDF generation logic
```

### Design Patterns

1. **Model-View-Separation**: UI (MainView) is separate from data models (TripReport, TripEntry)
2. **Single Responsibility**: Each class has one clear purpose
3. **Validation Layer**: Input validation is centralized in PdfGenerator
4. **Constant Pooling**: All PDF coordinates stored as constants to facilitate template adjustments

---

## Class Descriptions

### Model Classes

#### `TripEntry.java`
- Represents a single row in the trip entry table
- Contains fields: mileage, city/zip, shipper, consignee, type, trailer, duty code
- Method `isPopulated()` checks if row has data
- Immutable getters/setters pattern for clean API

#### `TripReport.java`
- Container for complete trip report data
- Holds header info: mileage start/finish, dates
- Manages collection of TripEntry objects
- Method `getTotalMileage()` calculates difference

### UI Classes

#### `MainView.java` (extends Application)
- Main JavaFX application class
- Handles all UI rendering and user interactions
- Updates model from form fields
- Manages table editing and validation display

**Key Components:**
- Header Section: Mileage and date inputs
- Table Section: Trip entries with editable cells
- Button Section: Add/Remove rows, Preview, Generate

**Inner Classes:**
- `EditingCell`: Custom TableCell for text editing
- `ComboBoxTableCell`: Custom TableCell for dropdown selection

### PDF Processing

#### `PdfGenerator.java`
- Loads PDF template from resources
- Fills form fields with data
- Places checkboxes for freight type
- Saves completed PDF

**PDF Coordinates:**
All coordinates are in **points** (1/72 inch). Adjust these constants based on your template:

```java
// Header section
HEADER_MILEAGE_START_X = 100
HEADER_MILEAGE_START_Y = 750
HEADER_MILEAGE_FINISH_X = 250
// ... etc
```

**Finding Coordinates:**
1. Open PDF template in Adobe Acrobat or similar
2. Use Tools > Measure or position indicator
3. Note X,Y values for each field
4. Update constants in PdfGenerator

---

## Data Flow

### User Input → PDF Generation

```
┌─────────────────────────┐
│   User enters data      │
│   in MainView form      │
└────────────┬────────────┘
             │
             ↓
┌─────────────────────────┐
│  Update TripReport      │
│  model from UI fields   │
└────────────┬────────────┘
             │
             ↓
┌─────────────────────────┐
│  Validate with          │
│  PdfGenerator           │
└────────────┬────────────┘
             │
        ┌────┴────┐
        │          │
    Invalid    Valid
        │          │
        ↓          ↓
    Show Error  Load Template
                    │
                    ↓
            Fill header fields
                    │
                    ↓
            Fill trip rows
                    │
                    ↓
            Fill checkboxes
                    │
                    ↓
            Save PDF
```

---

## PDF Template Integration

### Important Notes

1. **Do NOT modify** pre-filled fields:
   - Driver Name
   - Driver Number
   - Power Unit

2. **Template placement** must be exact:
   - `src/main/resources/templates/Trip Report Form 2026.pdf`

3. **Coordinates system**:
   - Origin (0,0) is typically bottom-left in PDFs
   - Y increases upward
   - All measurements in points

4. **Row height spacing**:
   - Current: 20 points per row
   - Adjust `ROW_HEIGHT` constant if template differs

### Calibrating Coordinates

If text appears in wrong positions:

1. Generate a test PDF with debug logging
2. Compare positions with template
3. Calculate offset difference
4. Update constants in PdfGenerator
5. Rebuild and test

Example:
```java
// If text appears 50 points too far left:
// Original:
private static final float HEADER_MILEAGE_START_X = 100;
// Adjusted:
private static final float HEADER_MILEAGE_START_X = 150;
```

---

## Key Features & Implementation

### Validation

All validation rules in `PdfGenerator.validateTripReport()`:
- Mileage fields must be numeric
- Required fields cannot be empty
- Row validation runs only on populated rows

Benefits:
- Centralized logic
- Easy to update rules
- Reusable by preview and generate functions

### Table Editing

Uses JavaFX TableCell with inline editing:
- Single-click to edit text fields
- Dropdown menus for Type and Duty Code
- ESC to cancel, ENTER or click elsewhere to commit

### Preview Feature

- Generates temporary PDF in system temp folder
- Opens with system default PDF viewer
- Uses same validation as final PDF
- Allows user verification before saving

### Auto-Calculate Fields

Total Mileage is:
- Calculated when either mileage field changes
- Read-only display
- Validation ensures both fields are numeric

---

## Performance Considerations

1. **PDF Generation** (~1-2 seconds typical)
   - Template loading
   - Content stream manipulation
   - File I/O

2. **Table Rendering**
   - Max 15 rows (configurable)
   - Efficient enough for real-time editing

3. **Memory**
   - PDFBox loads full document in memory
   - Safe for templates up to several MB

---

## Future Enhancements

### Possible Improvements

1. **Coordinate Auto-Detection**
   - Analyze PDF structure programmatically
   - Reduce manual calibration needs

2. **Template Management**
   - Support multiple templates
   - Template selection dropdown

3. **Data Persistence**
   - Save/load sessions
   - Auto-save drafts

4. **Advanced Features**
   - Field masking/formatting
   - Auto-population from database
   - Batch PDF generation

5. **User Preferences**
   - Remember window size
   - Default save location
   - Font size adjustment

---

## Testing Checklist

When modifying code, test:

- [ ] Application starts without errors
- [ ] Form accepts valid input
- [ ] Validation catches invalid input
- [ ] Add/Remove rows work correctly
- [ ] Total mileage auto-calculates
- [ ] Preview opens in PDF viewer
- [ ] Generated PDF has correct values
- [ ] PDF saves to chosen location
- [ ] No crashes with edge cases (empty rows, special characters)

---

## Debugging Tips

### Enable Detailed Logging

Edit `src/main/resources/logback.xml`:
```xml
<logger name="com.tripreport" level="DEBUG"/>
```

### Common Issues

1. **PDF text not appearing**
   - Check coordinates in PdfGenerator
   - Verify template is loading correctly
   - Check for coordinate type errors (float vs int)

2. **UI elements not responding**
   - Check for exception in logs
   - Verify event handlers attached
   - Check thread safety

3. **Table editing not working**
   - Ensure table is editable: `tripTable.setEditable(true)`
   - Check cell factory is correctly set
   - Verify commit events trigger updates

---

## Code Style Guidelines

- Classes: PascalCase
- Methods: camelCase
- Constants: UPPER_SNAKE_CASE
- Meaningful variable names
- Comments for complex logic
- Javadoc for public APIs

---

## Building & Deployment

### Development Build
```bash
mvn clean install
```

### Running from IDE
- Right-click Main.java → Run
- Or use Maven: `mvn javafx:run`

### Standalone JAR
```bash
mvn clean package
java -jar target/trip-report-generator-1.0.0-shaded.jar
```

### Windows Executable (future)
Could use tools like JPackage to create .msi or .exe installer

---

## Troubleshooting Guide for Developers

### Issue: "Cannot find template"
- Check resource folder structure
- Template must be in: `src/main/resources/templates/`
- ClassLoader might not have access to file

### Issue: "PDF opens blank"
- Verify coordinates in PdfGenerator
- Check if PDPageContentStream being used correctly
- Ensure text is being added to page

### Issue: "JavaFX not loading"
- Ensure javafx-maven-plugin in pom.xml
- Check JDK (not JRE) installed
- Module system may require --add-modules flag

### Issue: "Table not editable"
- Verify setEditable(true) called
- Check cell factory attached
- Ensure event listeners set

---

## Version History

- **v1.0.0** - Initial release
  - Basic form entry
  - PDF generation
  - Validation
  - Preview function
