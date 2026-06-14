# Quick Setup Guide - Trip Report PDF Generator

## For the Impatient

This guide gets you running in 5 minutes.

### Step 1: Install Java 21

**Windows:**
1. Download: https://www.oracle.com/java/technologies/downloads/#java21
2. Run installer, accept defaults
3. Verify: Open Command Prompt, type `java -version`

**macOS:**
```bash
brew install java21
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

### Step 2: Install Maven

**Windows:**
1. Download: https://maven.apache.org/download.cgi (Binary zip)
2. Extract to C:\Program Files\Apache\maven
3. Add to System PATH (search "Environment Variables")
4. Add: `C:\Program Files\Apache\maven\bin`
5. Verify: Open Command Prompt, type `mvn -version`

**macOS:**
```bash
brew install maven
mvn -version
```

**Linux:**
```bash
sudo apt update
sudo apt install maven
mvn -version
```

### Step 3: Add the PDF Template

1. Get `Trip Report Form 2026.pdf` from your company
2. Create this folder: `src/main/resources/templates/`
3. Place the PDF file there
4. Result: `src/main/resources/templates/Trip Report Form 2026.pdf`

### Step 4: Build

Open Command Prompt/Terminal in the project folder:

```bash
mvn clean install
```

Wait for "BUILD SUCCESS"

### Step 5: Run

```bash
mvn javafx:run
```

The app will start!

---

## That's It!

If you get errors:
- Did you install Java 21? (Not Java 8 or 11)
- Did you install Maven?
- Did you add the PDF template?
- Check `trip-report-generator.log` for errors

---

## Common Issues

### "mvn command not found"
Maven is not in your PATH. Reinstall and add it to PATH.

### "Java version mismatch"
You have an older Java. Uninstall old Java, install Java 21.

### "Template not found"
Place the PDF at: `src/main/resources/templates/Trip Report Form 2026.pdf`

### Build hangs or is slow
First build takes time downloading dependencies. Be patient (5-10 minutes).

---

## Next Steps

See `README.md` for detailed documentation.
