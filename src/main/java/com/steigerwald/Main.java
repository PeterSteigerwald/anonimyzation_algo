package com.steigerwald;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("src/main/resources");
        FhirXmlSynopsisTree tree = new FhirXmlSynopsisTree();

        tree.parseFhirFiles(dir);
        System.out.println(tree);
    }
}
