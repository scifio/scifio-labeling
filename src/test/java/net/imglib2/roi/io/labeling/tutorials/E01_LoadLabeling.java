package net.imglib2.roi.io.labeling.tutorials;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.io.labeling.LabelingIOService;
import net.imglib2.roi.io.labeling.data.ImgLabelingContainer;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class E01_LoadLabeling {

    Context context;

    @Before
    public void beforeTests() {
        context = new Context();
    }


    @Test
    public void loadBasicLabeling() throws IOException {
        // get the LabelingIO service from the context
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        // open a bson file with IntType labels
        // the container contains an ImgLabeling of that type as well as an optional sourcemap
        // the sourcemap is a mapping of a source img to a list of labels that where contained in it and added to 
        // the ImgLabeling
        ImgLabelingContainer<Integer, IntType> container = labelingIOService.open("src/test/resources/labeling/labelSaveTestSimple.bson");
        ImgLabeling<Integer, IntType> mapping = container.getImgLabeling();
        Assert.assertNotNull(mapping);
        Assert.assertNotNull(container.getSourceToLabel());
        Assert.assertTrue(container.getSourceToLabel().isEmpty());

    }

    @Test
    public void loadBasicLabeling2() throws IOException {
        // get the LabelingIO service from the context
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        // open a bson file with IntType labels
        // the container contains an ImgLabeling of that type as well as an optional sourcemap
        // the sourcemap is a mapping of a source img to a list of labels that where contained in it and added to
        // the ImgLabeling
        ImgLabelingContainer<Integer, IntType> container = labelingIOService.open("src/test/resources/labeling/example1.bson");
        ImgLabeling<Integer, IntType> mapping = container.getImgLabeling();
        Assert.assertNotNull(mapping);
        Assert.assertNotNull(container.getSourceToLabel());
        Assert.assertTrue(!container.getSourceToLabel().isEmpty());

    }

    @Test
    public void loadFunctionBasedLabeling() throws IOException {
        // get the LabelingIO service from the context
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        // creating a mapping for label value to class
        // this could be the case when your label is actually a more complex data structure which contains more
        // information. in this case, the user takes care of the mapping from label(id) to data structure
        List<Example> labels = getComplexImgLabeling();
        Map<Long, Example> map = new HashMap<>();
        AtomicLong atomicLong = new AtomicLong(0);
        labels.forEach(label -> map.put(atomicLong.getAndIncrement(), label));
        //get the ImgLabeling with Example.class from our mapping through the map.get() function
        ImgLabelingContainer<Example, IntType> container = labelingIOService.open("src/test/resources/labeling/labelSaveTestComplexFunction.bson", map::get);
        ImgLabeling<Example, IntType> mapping = container.getImgLabeling();
        Assert.assertNotNull(mapping);
        Assert.assertNotNull(container.getSourceToLabel());
        Assert.assertTrue(container.getSourceToLabel().isEmpty());
    }

    // ToDo: fix usage of mapping
    @Test
    public void loadClassBasedLabeling(){
        // get the LabelingIO service from the context
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        ImgLabelingContainer<Example, IntType> container = labelingIOService.open("src/test/resources/labeling/labelSaveTestComplex.bson", Example.class, new ExampleCodec());
        ImgLabeling<Example, IntType> mapping = container.getImgLabeling();
        Assert.assertNotNull(mapping);
        Assert.assertNotNull(container.getSourceToLabel());
        Assert.assertTrue(container.getSourceToLabel().isEmpty());
    }


    /*
        Utility classes and functions start here
     */

    private List<Example> getComplexImgLabeling() {
        Example[] values1 = new Example[]{new Example("a", 1.0, 1), new Example("b", 2.24121, 2)};
        Example[] values2 = new Example[]{new Example("a", 1.0, 1)};
        Example[] values3 = new Example[]{new Example("b", 2.24121, 2), new Example("a", 1.0, 1), new Example("a", 1.0, 3)};
        // setup

        Img<UnsignedByteType> indexImg = ArrayImgs.unsignedBytes(new byte[]{1, 3, 2}, 1);
        List<Example> labelSets = Arrays.asList(new Example(), new Example("a", 1.0, 1), new Example("b", 2.24121, 2)
                , new Example("b", 2.24121, 2), new Example("a", 1.0, 3));
        return labelSets;
        //return ImgLabeling.fromImageAndLabelSets(indexImg, labelSets);
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> asSet(T... values) {
        return new TreeSet<>(Arrays.asList(values));
    }


}
