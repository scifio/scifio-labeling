package net.imglib2.labeling.data;

import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LabelingData<T, S> {

    private int version = 3;
    private int numSets = 0;
    private int numSources = 0;
    private String indexImg;
    private Map<Integer, T> labelMapping;
    private Map<String, Set<Integer>> labelSets;
    private S metadata;

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getNumSets() {
        return this.numSets;
    }

    public void setNumSets(int numSets) {
        this.numSets = numSets;
    }

    public int getNumSources() {
        return this.numSources;
    }

    public void setNumSources(int numSources) {
        this.numSources = numSources;
    }

    public String getIndexImg() {
        return this.indexImg;
    }

    public void setIndexImg(String indexImg) {
        this.indexImg = indexImg;
    }

    public Map<Integer, T> getLabelMapping() {
        return this.labelMapping;
    }

    public void setLabelMapping(Map<Integer, T> labelMapping) {
        this.labelMapping = labelMapping;
    }

    public Map<String, Set<Integer>> getLabelSets() {
        return this.labelSets;
    }

    public void setLabelSets(Map<String, Set<Integer>> labelSets) {
        this.labelSets = labelSets;
    }

    public S getMetadata() {
        return this.metadata;
    }

    public void setMetadata(S metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        LabelingData that = (LabelingData) o;
        return this.numSets == that.numSets && this.indexImg.equals(that.indexImg) && this.labelSets.equals(that.labelSets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.numSets, this.indexImg, this.labelSets);
    }

    public String toJson(){
        return new Gson().toJson(this);
    }

    public LabelingData fromJson(String json){
        return new Gson().fromJson(json, this.getClass());
    }
}