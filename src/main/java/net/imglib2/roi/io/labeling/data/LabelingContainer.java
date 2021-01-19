package net.imglib2.roi.io.labeling.data;

import net.imglib2.roi.labeling.LabelingMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LabelingContainer<T> {

    List<Set<T>> labelSets;
    Map<String, Set<Integer>> sourceToLabel;
    LabelingMapping<T> labelingMapping;


    public LabelingContainer(List<Set<T>> labelSets, Map<String, Set<Integer>> sourceToLabel, LabelingMapping<T> labelingMapping) {
        this.labelSets = labelSets;
        this.sourceToLabel = sourceToLabel;
        this.labelingMapping = labelingMapping;
    }

    public LabelingContainer() {

    }

    public void addLabelToSource(String source, Integer label) {
        sourceToLabel.putIfAbsent(source, new HashSet<Integer>());
        sourceToLabel.get(source).add(label);
    }

    public List<Set<T>> getLabelSets() {
        return labelSets;
    }

    public void setLabelSets(List<Set<T>> labelSets) {
        if(labelingMapping != null)
            labelingMapping.setLabelSets(labelSets);
        this.labelSets = labelSets;
    }

    public Map<String, Set<Integer>> getSourceToLabel() {
        return sourceToLabel;
    }

    public void setSourceToLabel(Map<String, Set<Integer>> sourceToLabel) {
        this.sourceToLabel = sourceToLabel;
    }

    public LabelingMapping<T> getLabelingMapping() {
        return labelingMapping;
    }

    public void setLabelingMapping(LabelingMapping<T> labelingMapping) {
        this.labelingMapping = labelingMapping;
        if(labelSets != null)
            labelingMapping.setLabelSets(labelSets);
    }
}
