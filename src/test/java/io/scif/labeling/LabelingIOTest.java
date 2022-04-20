/*-
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2020 - 2022 SCIFIO developers.
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
package io.scif.labeling;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.scif.labeling.data.Container;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labeling.data.LabelingData;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class LabelingIOTest {
    Context context;

    @Before
    public void beforeTests() {
        context = new Context();
    }

    @Test
    public void testEquality() throws IOException {
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        ImgLabeling<Integer, IntType> imgLabeling = labelingIOService.load("src/test/resources/labeling/labelSaveTestSimple", Integer.class, IntType.class);
        labelingIOService.save(imgLabeling, "src/test/resources/labeling/example1_sav");
        ImgLabeling<Integer, IntType> imgLabeling2 = labelingIOService.load("src/test/resources/labeling/example1_sav", Integer.class, IntType.class);
        Assert.assertEquals(imgLabeling.getMapping().getLabels(), imgLabeling2.getMapping().getLabels());
    }

    @Test
    public void testEquality2() throws IOException {
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        ImgLabeling<Integer, IntType> imgLabeling = labelingIOService.load("src/test/resources/labeling/test", Integer.class, IntType.class);
        labelingIOService.save(imgLabeling, "src/test/resources/labeling/test2");
        ImgLabeling<Integer, IntType> imgLabeling2 = labelingIOService.load("src/test/resources/labeling/test2", Integer.class, IntType.class);
        Assert.assertEquals(imgLabeling.getMapping().getLabels(), imgLabeling2.getMapping().getLabels());
    }

    @Test
    public void saveLabelingWithMetadataPrimitiveTest() throws IOException {
        ImgLabeling<Integer, UnsignedByteType> labeling = getSimpleImgLabeling();
        context.getService(LabelingIOService.class).saveWithMetaData(labeling, new File("src/test/resources/labeling/labelSaveTestSimple.tif").getAbsolutePath(), new Example("a", 2.0, 1));
    }

    @Test
    public void loadLabelingWithMetadataPrimitiveTest() throws IOException {
        Container<Example, Integer, IntType>
            container = context.getService(LabelingIOService.class).loadWithMetadata("src/test/resources/labeling/labelSaveTestSimpleMeta.tif", Example.class, Integer.class, IntType.class);
        ImgLabeling<Integer, IntType> mapping = container.getImgLabeling();
        Example e = container.getMetadata();
        Assert.assertNotNull(e);
        Assert.assertEquals(getSimpleImgLabeling().getMapping().getLabels(), mapping.getMapping().getLabels());
    }

    @Test
    public void saveLabelingWithMetadataComplexTest() throws IOException {
        ImgLabeling<Example, IntType> labeling = getComplexImgLabeling();
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        labelingIOService.saveWithMetaData(labeling, new File("src/test/resources/labeling/labelSaveTestComplexMeta.tif").getAbsolutePath(), new Example("a", 2.0, 1));
    }

    @Test
    public void loadLabelingWithMetadataComplexWithCodecTest() throws IOException {
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        Container<Example, Example, IntType> container = labelingIOService.loadWithMetadata("src/test/resources/labeling/labelSaveTestComplexMeta", Example.class, Example.class, IntType.class);
        ImgLabeling<Example, IntType> mapping = container.getImgLabeling();
        Example e = container.getMetadata();
        Assert.assertNotNull(e);
        Assert.assertEquals(getComplexImgLabeling().getMapping().getLabels(), mapping.getMapping().getLabels());
    }

    @Test
    public void t() throws IOException {
        GsonBuilder builder = new GsonBuilder();
        Reader reader = Files.newBufferedReader(Paths.get("src/test/resources/labeling/labelSaveTestComplexMeta.lbl.json"));
        Type labelingDataType = new TypeToken<LabelingData<Example,Example>>() {}.getType();
        LabelingData<Example,Example> labelingData = builder.create().fromJson(reader, labelingDataType);
    }

    private ImgLabeling<Integer, UnsignedByteType> getSimpleImgLabeling() {
        Integer[] values1 = new Integer[]{42, 13};
        Integer[] values2 = new Integer[]{1};
        Integer[] values3 = new Integer[]{1, 13, 42};
        // setup
        Img<UnsignedByteType> indexImg = ArrayImgs.unsignedBytes(new byte[]{1, 0, 2}, 1);
        List<Set<Integer>> labelSets = Arrays.asList(asSet(), asSet(values1), asSet(values2), asSet(values3));
        return ImgLabeling.fromImageAndLabelSets(indexImg, labelSets);
    }

    private ImgLabeling<Example, IntType> getComplexImgLabeling() {
        Example[] values1 = new Example[]{new Example("a", 1.0, 1), new Example("b", 2.24121, 2)};
        Example[] values2 = new Example[]{new Example("a", 1.0, 1)};
        Example[] values3 = new Example[]{new Example("b", 2.24121, 2), new Example("a", 1.0, 1), new Example("a", 1.0, 3)};
        // setup
        Img<IntType> indexImg = ArrayImgs.ints(new int[]{1, 0, 2}, 1);
        List<Set<Example>> labelSets = Arrays.asList(asSet(), asSet(values1), asSet(values2), asSet(values3));
        return ImgLabeling.fromImageAndLabelSets(indexImg, labelSets);
    }


    @SuppressWarnings("unchecked")
    private <T> Set<T> asSet(T... values) {
        return new TreeSet<>(Arrays.asList(values));
    }


    private static class Example implements Comparable<Example> {

        private final String a;

        private final double b;

        private final int c;

        public Example(String a, double b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Example example = (Example) o;
            return Double.compare(example.b, b) == 0 &&
                    c == example.c &&
                    Objects.equals(a, example.a);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b, c);
        }

        @Override
        public int compareTo(Example o) {
            return this.equals(o) ? 0 : 1;
        }
    }

}
