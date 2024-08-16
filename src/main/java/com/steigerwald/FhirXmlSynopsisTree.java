package com.steigerwald;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class FhirXmlSynopsisTree {

    private SynopsisTreeNode root;
    private Map<String, ArrayEntry> arrayList;

    public FhirXmlSynopsisTree() {
        this.root = new SynopsisTreeNode("root", null);
        this.arrayList = new HashMap<>();

    }

    public SynopsisTreeNode getRoot() {
        return root;
    }

    public void parseFhirFiles(Path dir) throws IOException {

        try (Stream<Path> paths = Files.walk(dir)) {
            AtomicInteger fileNmbr = new AtomicInteger(0); // Since this method uses a lambda expression and variables
                                                           // need to be effectively final we use AtomicInteger here as
                                                           // workaroud
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".xml"))
                    .forEach(path -> {
                        int fileNumber = fileNmbr.incrementAndGet(); // Count up and get and int
                        parseXML(path.toString(), fileNumber);
                    });

        }
    }

    private void parseXML(String filePath, int recordId) {
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // Handle XML namespaces
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            // Get the root element of the current XML file
            Element fileRootElement = document.getDocumentElement();

            // Create a new child for the common root node
            // Find if a corresponding root node already exists
            SynopsisTreeNode existingRootNode = findChildWithName(root.getChildren(), fileRootElement.getNodeName());

            // If not found, create a new child node for the common root
            if (existingRootNode == null) {
                existingRootNode = new SynopsisTreeNode(fileRootElement.getTagName(), null);
                root.addChild(existingRootNode);
            }

            // Add the current file ID to this root node
            existingRootNode.addRecordId(recordId);

            traverseAndAdd(fileRootElement, existingRootNode, recordId);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void traverseAndAdd(Element element, SynopsisTreeNode currentNode, int recordId) {
        // Add current file ID to this node
        currentNode.addRecordId(recordId);
        String elemKey = element.getNodeName();
        NamedNodeMap attributes = element.getAttributes();// Add element's attributes to the current node
        for (int j = 0; j < attributes.getLength(); j++) {
            Attr attr = (Attr) attributes.item(j);
            Attribute attribute = currentNode.getAttributes().get(attr.getNodeValue());
            if (attribute == null) {
                currentNode.addAttribute(attr.getNodeName(), attr.getNodeValue(), recordId);
            } else {
                attribute.addRecordId(recordId);
            }

            if (attr.getName().equals("value")) {
                elemKey = element.getNodeName() + ":" + attr.getValue();
                if (!arrayList.containsKey(elemKey)) {
                    arrayList.put(elemKey, new ArrayEntry(attr.getValue()));
                }

            } else {
                if (!arrayList.containsKey(elemKey)) {
                    arrayList.put(elemKey, new ArrayEntry(""));
                }
            }
        }
        ArrayEntry entry = arrayList.get(elemKey);
        if (entry != null) {
            entry.addRecordId(recordId);
            entry.addSidelink(currentNode);
        }
        // Process child elements
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) node;
                String childTag = childElement.getTagName();

                // Find if a child with the same name already exists
                SynopsisTreeNode existingChildNode = findChildWithName(currentNode.getChildren(), childTag);

                // If not found, create a new child node and add it to the current node
                if (existingChildNode == null) {
                    SynopsisTreeNode newChildNode = new SynopsisTreeNode(childTag, null);
                    currentNode.addChild(newChildNode);
                    existingChildNode = newChildNode;
                }

                // Set the text value of the current node (if it has any)
                // String textContent = element.getTextContent().trim();
                // if (!textContent.isEmpty()) {
                // existingChildNode.addValue(textContent);
                // }

                // Recursively traverse the child element
                traverseAndAdd(childElement, existingChildNode, recordId);
            }
        }
    }

    // Helper method to find a child node with a specific name
    private SynopsisTreeNode findChildWithName(List<SynopsisTreeNode> children, String name) {
        for (SynopsisTreeNode child : children) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    public double calculateRPD(SynopsisTreeNode node) {
        List<List<SynopsisTreeNode>> allPaths = new ArrayList<>();
        findAllPaths(node, new ArrayList<>(), allPaths);
        double totalRPD = 0.0;

        for (List<SynopsisTreeNode> path : allPaths) {
            totalRPD += calculateRPDForPath(path);
        }

        return totalRPD / allPaths.size();
    }

    private void findAllPaths(SynopsisTreeNode node, List<SynopsisTreeNode> currentPath,
            List<List<SynopsisTreeNode>> allPaths) {
        currentPath.add(node);

        if (node.getChildren().isEmpty()) {
            allPaths.add(new ArrayList<>(currentPath));
        } else {
            for (SynopsisTreeNode child : node.getChildren()) {
                findAllPaths(child, currentPath, allPaths);
            }
        }

        currentPath.remove(currentPath.size() - 1);
    }

    private double calculateRPDForPath(List<SynopsisTreeNode> path) {
        double domainProduct = 1.0;

        for (SynopsisTreeNode node : path) {
            int d = node.getDepth();
            int c = node.getCardinality();
            domainProduct *= (d > 0 ? d : 1) * (c > 0 ? c : 1);
        }

        return 1.0 / domainProduct;
    }

    public double calculateRPDForTree() {
        return calculateRPD(root);
    }

    public double calculateML2(FhirXmlSynopsisTree originalTree, int maxGeneralizationLevel) {
        int totalOriginalSubtrees = 0;
        int totalAnonymizedSubtrees = 0;

        for (int level = 0; level <= maxGeneralizationLevel; level++) {
            int originalSubtreesAtLevel = countFrequentSubtreesAtLevel(originalTree.getRoot(), level);
            int anonymizedSubtreesAtLevel = countFrequentSubtreesAtLevel(this.root, level);
            totalOriginalSubtrees += originalSubtreesAtLevel;
            totalAnonymizedSubtrees += anonymizedSubtreesAtLevel;
        }

        return 1 - (double) totalAnonymizedSubtrees / totalOriginalSubtrees;
    }

    private int countFrequentSubtreesAtLevel(SynopsisTreeNode node, int level) {
        return 0;
        // Placeholder for subtree counting logic
        // TODO: FInd a way on how to count the Subtrees
    }
}
