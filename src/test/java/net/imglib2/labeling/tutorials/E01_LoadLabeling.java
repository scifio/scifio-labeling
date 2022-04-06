/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2020 - 2021 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 *             John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 *             Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 *             Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 *             Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 *             Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.labeling.tutorials;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labeling.LabelingIOService;
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
        // load a bson file with IntType labels
        // the container contains an ImgLabeling of that type as well as an optional sourcemap
        // the sourcemap is a mapping of a source img to a list of labels that where contained in it and added to 
        // the ImgLabeling
        ImgLabeling<Integer, IntType> imgLabeling = labelingIOService.load("src/test/resources/labeling/labelSaveTestSimple.lbl.json");
        Assert.assertNotNull(imgLabeling);
        Assert.assertNotNull(imgLabeling.getIndexImg());
        Assert.assertFalse(imgLabeling.getMapping().getLabels().isEmpty());

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
//        Container<Example, Example, IntType> container = labelingIOService.loadWithMetadata("src/test/resources/labeling/labelSaveTestComplexFunction.bson", map::get, Example.class, new ExampleCodec());
//        ImgLabeling<Example, IntType> mapping = container.getImgLabeling();
//        Assert.assertNotNull(mapping);
    }

    @Test
    public void loadClassBasedLabeling() throws IOException {
        // get the LabelingIO service from the context
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
//        Container<Example, Example, IntType> container = labelingIOService.loadWithMetadata("src/test/resources/labeling/labelSaveTestComplex.bson", Example.class, Example.class, new ExampleCodec());
//        ImgLabeling<Example, IntType> mapping = container.getImgLabeling();
//        Assert.assertNotNull(mapping);
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
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> asSet(T... values) {
        return new TreeSet<>(Arrays.asList(values));
    }


}
