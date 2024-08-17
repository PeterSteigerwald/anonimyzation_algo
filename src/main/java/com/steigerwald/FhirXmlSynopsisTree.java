package com.steigerwald;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public void writeXmlFilesPerRecordId(Path outputDir) throws Exception {
        Set<Integer> recordIds = collectRecordIds(root);

        for (Integer recordId : recordIds) {
            String xmlContent = toXmlForRecordId(recordId);
            String fileName = "record_" + recordId + ".xml";
            File outputFile = outputDir.resolve(fileName).toFile();
            writeXmlToFile(xmlContent, outputFile);
        }
    }

    private Set<Integer> collectRecordIds(SynopsisTreeNode node) {
        Set<Integer> recordIds = new HashSet<>();
        for (SynopsisTreeNode child : node.getChildren()) {
            recordIds.addAll(child.getRecordIds());
        }
        return recordIds;
    }

    private String toXmlForRecordId(int recordId) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // Create a new XML Document
        Document doc = docBuilder.newDocument();

        // Convert the root node to an XML element, filtering by recordId
        Element rootElement = createXmlElementForRecordId(doc, root, recordId);
        doc.appendChild(rootElement);

        // Convert the Document to a string
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);

        return writer.toString();
    }

    private Element createXmlElementForRecordId(Document doc, SynopsisTreeNode node, int recordId) {
        if (!node.getRecordIds().contains(recordId) && !node.getName().equals("root")) {
            return null;
        }

        // Create an element with the node's name
        Element element = doc.createElement(node.getName());

        // Add attributes to the element that belong to the specified recordId
        for (Map.Entry<String, Attribute> entry : node.getAttributes().entrySet()) {
            if (entry.getValue().getRecordIds().contains(recordId)) {
                element.setAttribute(entry.getValue().getName(), escapeXmlValue(entry.getValue().getValue()));
            }
        }

        // Recursively create child elements
        for (SynopsisTreeNode child : node.getChildren()) {
            Element childElement = createXmlElementForRecordId(doc, child, recordId);
            if (childElement != null) {
                element.appendChild(childElement);
            }
        }

        return element;
    }

    private String escapeXmlValue(String input) {
        // Replace special characters with their XML escape sequences
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private void writeXmlToFile(String xmlContent, File file) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(xmlContent);
        }
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
