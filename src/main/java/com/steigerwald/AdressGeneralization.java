package com.steigerwald;

import java.util.HashMap;
import java.util.Map;

public class AdressGeneralization {
    private final int generalizationLvl;

    public AdressGeneralization(int generalizationLvl) {
        this.generalizationLvl = generalizationLvl;
    }

    // Generalize address information within a given SynopsisTreeNode
    public void generalizeAddress(SynopsisTreeNode node) {
        Map<String, SynopsisTreeNode> addressComponents = extractAddressComponents(node);
        applyGeneralization(node, addressComponents);
    }

    // Extract address components from the tree node
    private Map<String, SynopsisTreeNode> extractAddressComponents(SynopsisTreeNode node) {
        Map<String, SynopsisTreeNode> components = new HashMap<>();
        for (SynopsisTreeNode child : node.getChildren()) {
            String childName = child.getName();
            if (isAddressComponent(childName)) {
                components.put(childName, child);
            }
        }
        return components;
    }

    // Check if the node is an address component
    private boolean isAddressComponent(String nodeName) {
        return nodeName.equals("line") || nodeName.equals("city") || nodeName.equals("district") ||
                nodeName.equals("state") || nodeName.equals("country");
    }

    // Apply generalization by replacing the more specific node with the more
    // generalized one
    private void applyGeneralization(SynopsisTreeNode node, Map<String, SynopsisTreeNode> components) {
        String[] hierarchy = { "line", "city", "district", "state", "country" };

        for (int i = 0; i < generalizationLvl; i++) {
            if (components.containsKey(hierarchy[i])) {
                // Find the most generalized component that exists
                for (int j = i + 1; j < hierarchy.length; j++) {
                    if (components.containsKey(hierarchy[j])) {
                        // Replace the current component with the more generalized one
                        SynopsisTreeNode generalizedNode = components.get(hierarchy[j]);
                        node.addValue(generalizedNode.getValue());
                        node.getChildren().clear(); // Remove other children
                        node.addChild(generalizedNode); // Add the generalized node as the only child
                        node.getAttributes().clear(); // Clear attributes for simplicity
                        node.getAttributes().putAll(generalizedNode.getAttributes());
                        return; // Generalization done
                    }
                }
            }
        }
    }
}
