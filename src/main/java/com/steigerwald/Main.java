package com.steigerwald;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("src/main/resources");
        FhirXmlSynopsisTree tree = new FhirXmlSynopsisTree();

        tree.parseFhirFiles(dir);
        System.out.println(tree);
        double rdp = tree.calculateRPDForTree();

        System.out.println("Tree-RDP = " + rdp);

        Path outdir = getOutputDirectory("FHIR-XML-Resources-Output");
        try {
            tree.writeXmlFilesPerRecordId(outdir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Path getOutputDirectory(String folderName) throws IOException {
        // Get the user's home directory
        String userHome = System.getProperty("user.home");

        // Create the path to the desktop
        Path desktopPath = Paths.get(userHome, "Desktop");

        // Create the path to the desired output folder on the desktop
        Path outputDir = desktopPath.resolve(folderName);

        // Create the folder if it doesn't exist
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        return outputDir;
    }
}
