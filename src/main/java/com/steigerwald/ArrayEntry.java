package com.steigerwald;

import java.util.*;

public class ArrayEntry {
    private String label;
    private Set<Integer> recordIds;
    private List<SynopsisTreeNode> sidelinks;

    public ArrayEntry(String label) {
        this.label = label;
        this.recordIds = new HashSet<>();
        this.sidelinks = new ArrayList<>();
    }

    public String getLabel() {
        return label;
    }

    public Set<Integer> getRecordIds() {
        return recordIds;
    }

    public List<SynopsisTreeNode> getSidelinks() {
        return sidelinks;
    }

    public void addRecordId(int recordId) {
        this.recordIds.add(recordId);
    }

    public void addSidelink(SynopsisTreeNode node) {
        this.sidelinks.add(node);
    }

    @Override
    public String toString() {
        return "ArrayEntry{" +
                "label='" + label + '\'' +
                ", recordIds=" + recordIds +
                ", sidelinks=" + sidelinks +
                '}';
    }
}
