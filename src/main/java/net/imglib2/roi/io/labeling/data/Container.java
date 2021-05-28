package net.imglib2.roi.io.labeling.data;

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;

public class Container<S, T, I extends IntegerType<I>> {

    ImgLabeling<T, I> imgLabeling;
    S metadata;

    public ImgLabeling<T, I> getImgLabeling() {
        return imgLabeling;
    }

    public void setImgLabeling(ImgLabeling<T, I> imgLabeling) {
        this.imgLabeling = imgLabeling;
    }

    public S getMetadata() {
        return metadata;
    }

    public void setMetadata(S metadata) {
        this.metadata = metadata;
    }
}
