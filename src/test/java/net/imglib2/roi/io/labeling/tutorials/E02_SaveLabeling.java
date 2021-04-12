package net.imglib2.roi.io.labeling.tutorials;

import io.scif.services.DatasetIOService;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.io.labeling.LabelingIO;
import net.imglib2.roi.io.labeling.LabelingIOService;
import net.imglib2.roi.io.labeling.data.ImgLabelingContainer;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class E02_SaveLabeling {

    Context context;

    @Before
    public void beforeTests() {
        context = new Context();
    }


    @Test
    public void saveLabelingTest() throws IOException {
        ImgLabeling<Integer, UnsignedByteType> labeling = getSimpleImgLabeling();
        ImgLabelingContainer<Integer, UnsignedByteType> container = new ImgLabelingContainer<>();
        container.setImgLabeling(labeling);
        Map<String, Set<Integer>> sources = new HashMap<>();
        Set set = new HashSet<>();
        set.add(1);
        set.add(13);
        set.add(42);
        sources.put("1",set);

        // get the LabelingIO service from the context
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        labelingIOService.save(container, new File("src/test/resources/labeling/labelSaveTestSimple.tif").getAbsolutePath());

    }


    private ImgLabeling<Integer, UnsignedByteType> getSimpleImgLabeling() {
        Integer[] values1 = new Integer[]{42, 13};
        Integer[] values2 = new Integer[]{1};
        Integer[] values3 = new Integer[]{1, 13, 42};
        // setup
        Img<UnsignedByteType> indexImg = ArrayImgs.unsignedBytes(new byte[]{1, 0, 2}, 3);
        List<Set<Integer>> labelSets = Arrays.asList(asSet(), asSet(values1), asSet(values2), asSet(values3));
        return ImgLabeling.fromImageAndLabelSets(indexImg, labelSets);
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> asSet(T... values) {
        return new TreeSet<>(Arrays.asList(values));
    }

}
