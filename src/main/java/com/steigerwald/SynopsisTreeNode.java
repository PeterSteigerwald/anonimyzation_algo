package com.steigerwald;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SynopsisTreeNode {
    private String name;
    private String value;
    private Set<Integer> recordIds;
    private List<SynopsisTreeNode> children;
    private List<Attribute> attributes;

    public SynopsisTreeNode(String name, String value) {
        this.name = name;
        this.value = value != null ? value.trim() : "";
        this.recordIds = new HashSet<>();
        this.children = new ArrayList<>();
        this.attributes = new ArrayList<>();
    }

    public void addRecordId(int recordId) {
        this.recordIds.add(recordId);
    }

    public void addChild(SynopsisTreeNode child) {
        this.children.add(child);
    }

    public void addValue(String value) {
        this.value = value;
    }

    public void addAttribute(String name, String value) {
        this.attributes.add(new Attribute(name, value));
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Set<Integer> getRecordIds() {
        return recordIds;
    }

    public List<SynopsisTreeNode> getChildren() {
        return children;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public int getAttributeCount() {
        return attributes.size();
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", content='" + value + '\'' +
                ", children=" + children +
                '}';
    }
}

class Attribute {
    private String name;
    private String value;

    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return name + "=\"" + value + "\"";
    }
}
