package com.steigerwald;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private SynopsisTreeNode buildFhirResource(Element element, int recordId, boolean firstRun) {
        SynopsisTreeNode node;
        if (firstRun) {
            node = root;
        } else {
            node = new SynopsisTreeNode(element.getNodeName(), null);
        }

        node.addRecordId(recordId);

        if (!firstRun) {
            // Add attributes
            String elemKey = element.getNodeName();
            NamedNodeMap attributes = element.getAttributes();
            Attr attr = (Attr) attributes.item(0);
            if (attr == null) {
                if (!arrayList.containsKey(elemKey)) {
                    arrayList.put(elemKey, new ArrayEntry(""));
                }
            } else if (attr.getName().equals("value")) {
                node.addValue(attr.getValue());
                elemKey = element.getNodeName() + ":" + attr.getValue();
                if (!arrayList.containsKey(elemKey)) {
                    arrayList.put(elemKey, new ArrayEntry(attr.getValue()));
                }

            } else {
                if (!arrayList.containsKey(elemKey)) {
                    arrayList.put(elemKey, new ArrayEntry(""));
                }
            }

            ArrayEntry entry = arrayList.get(elemKey);
            entry.addRecordId(recordId);
            entry.addSidelink(node);
        }

        // Recursively add child element
        if (firstRun) {
            SynopsisTreeNode lastNode = buildFhirResource(element, recordId, false);
            return lastNode;
        } else {
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) children.item(i);

                    SynopsisTreeNode childNode = buildFhirResource(childElement, recordId, false);
                    node.addChild(childNode);
                }
            }
        }
        return node;
    }

    private SynopsisTreeNode buildFhirResourceTest(Element element, SynopsisTreeNode currentNode, int recordId) {
        // Use currentNode instead of root in all cases
        SynopsisTreeNode node = findOrCreateNode(currentNode, element, recordId);

        node.addRecordId(recordId);

        // Add attributes and values
        String elemKey = element.getNodeName();
        NamedNodeMap attributes = element.getAttributes();
        Attr attr = (Attr) attributes.item(0);
        if (attr != null && attr.getName().equals("value")) {
            node.addValue(attr.getValue());
            elemKey = element.getNodeName() + ":" + attr.getValue();
        }

        ArrayEntry entry = arrayList.computeIfAbsent(elemKey, k -> new ArrayEntry(attr == null ? "" : attr.getValue()));
        entry.addRecordId(recordId);
        entry.addSidelink(node);

        // Recursively process child elements
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) children.item(i);
                SynopsisTreeNode childNode = buildFhirResourceTest(childElement, node, recordId);
                node.addChild(childNode);
            }
        }

        return node;
    }

    // New method to find an existing node or create a new one
    private SynopsisTreeNode findOrCreateNode(SynopsisTreeNode parent, Element element, int recordId) {
        for (SynopsisTreeNode child : parent.getChildren()) {
            if (child.getName().equals(element.getNodeName()) && attributesMatch(child, element)) {
                child.addRecordId(recordId);
                return child;
            }
        }

        // No matching child found, create a new node
        SynopsisTreeNode newNode = new SynopsisTreeNode(element.getNodeName(), null);
        parent.addChild(newNode);
        return newNode;
    }

    // Helper method to compare attributes of a node with an element
    private boolean attributesMatch(SynopsisTreeNode node, Element element) {
        NamedNodeMap attributes = element.getAttributes();

        if (attributes.getLength() < 2) {
            return false;
        }

        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            if (!node.getValue().equals(attr.getValue())) {
                return false;
            }
        }
        return true;
    }
}