# QR Code Generator (Java Swing + ZXing)

A desktop QR code generator with a full GUI, built with Java Swing and the
ZXing encoding library, packaged as a standard Maven project so it opens and
runs in VS Code with no manual classpath setup.

## Features

- **Six content types** via tabs: Plain Text, URL, Email, Phone, SMS, WiFi
  network, and Contact card (each formatted so phone camera apps recognize
  it correctly — `mailto:`, `tel:`, `sms:`, `WIFI:`, `MECARD:`).
- **Adjustable options**: output image size, quiet-zone margin, error
  correction level (L/M/Q/H, with recovery-percentage tooltips), and custom
  foreground/background colors via a color picker.
- **Live preview** panel showing the generated QR code at full resolution.
- **Save as PNG** and **Copy to Clipboard** for the generated image.
- **Session history** side panel — every code you generate is kept in a
  list; click any entry to reload it into the preview.
- **Status bar** showing character count, byte size, and image dimensions.
- Full menu bar (File / Edit / Help) with the same actions as the buttons.

## Project structure

```
qr-code-generator/
├── pom.xml                          # Maven build file (dependencies + plugins)
├── .vscode/
│   ├── launch.json                  # Run configuration (F5 to launch the GUI)
│   ├── settings.json
│   └── extensions.json              # Recommends the Java extension pack
└── src/main/java/com/qrgen/
    ├── App.java                     # main() entry point
    ├── gui/
    │   ├── MainFrame.java           # The whole window: tabs, options, preview, history
    │   └── ImageTransferable.java   # Clipboard support for the QR image
    ├── qr/
    │   └── QRCodeService.java       # Wraps ZXing to encode text -> BufferedImage
    ├── util/
    │   └── ContentBuilder.java      # Builds mailto:/tel:/sms:/WIFI:/MECARD: payloads
    └── model/
        └── QRHistoryItem.java       # One entry in the session history list
```

## Requirements

- **JDK 17 or newer** (uses Java `switch` expressions and pattern matching).
- **VS Code** with the **Extension Pack for Java** (`vscjava.vscode-java-pack`)
  and the **Maven for Java** extension — VS Code will prompt you to install
  these automatically when you open the folder (see `.vscode/extensions.json`).
- **Maven** — either installed on your PATH, or just use the Maven extension
  bundled with the Java Extension Pack (it ships its own Maven, so you don't
  need to install anything separately).

## Running in VS Code

1. Open this folder in VS Code (`File > Open Folder...`).
2. Wait a few seconds for the Java extension to import the Maven project and
   download the two dependencies (`com.google.zxing:core` and `:javase`) —
   watch the bottom status bar for "Java: Ready".
3. Press **F5**, or open `src/main/java/com/qrgen/App.java` and click the
   **Run** ▷ button that appears above `public static void main`.
4. The QR Code Generator window opens. Type or paste content, pick a tab,
   adjust options, and click **Generate QR Code**.

## Running from the terminal

```bash
# Run directly (compiles automatically)
mvn compile exec:java

# Or build a self-contained runnable jar and launch it
mvn package
java -jar target/qr-code-generator.jar
```

## Notes

- All generated images use `TYPE_INT_RGB`, so saved PNGs are fully opaque —
  ideal for printing or sharing.
- The WiFi and Contact tabs generate standard `WIFI:` and `MECARD:` payloads
  recognized by iOS and Android camera apps.
- History is kept in memory only for the current run of the app; it is not
  written to disk.
