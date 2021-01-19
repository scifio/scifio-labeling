package net.imglib2.roi.io.labeling.data;

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;

import java.util.Map;
import java.util.Set;

public class ImgLabelingContainer<T, I extends IntegerType<I>> {

    ImgLabeling<T,I> imgLabeling;
    Map<String, Set<Integer>> sourceToLabel;

    public ImgLabelingContainer(ImgLabeling<T, I> imgLabeling, Map<String, Set<Integer>> sourceToLabel) {
        this.imgLabeling = imgLabeling;
        this.sourceToLabel = sourceToLabel;
    }

    public ImgLabelingContainer() {
    }

    public ImgLabeling<T, I> getImgLabeling() {
        return imgLabeling;
    }

    public void setImgLabeling(ImgLabeling<T, I> imgLabeling) {
        this.imgLabeling = imgLabeling;
    }

    public Map<String, Set<Integer>> getSourceToLabel() {
        return sourceToLabel;
    }

    public void setSourceToLabel(Map<String, Set<Integer>> sourceToLabel) {
        this.sourceToLabel = sourceToLabel;
    }
}
