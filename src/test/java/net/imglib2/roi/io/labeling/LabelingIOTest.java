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
package net.imglib2.roi.io.labeling;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.io.labeling.codecs.LabelingMappingCodec;
import net.imglib2.roi.io.labeling.data.Container;
import net.imglib2.roi.io.labeling.data.ImgLabelingContainer;
import net.imglib2.roi.io.labeling.data.LabelingContainer;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.IntegerCodec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class LabelingIOTest {
    Context context;

    @Before
    public void beforeTests() {
        context = new Context();
    }

    @Test
    public void test() throws IOException {
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        ImgLabeling imgLabeling = labelingIOService.load("src/test/resources/labeling/example1.bson");
        labelingIOService.save(imgLabeling, "src/test/resources/labeling/example1_sav.bson");
    }

    @Test
    public void saveLabelingWithMetadataPrimitiveTest() {
        ImgLabeling<Integer, UnsignedByteType> labeling = getSimpleImgLabeling();
        Container container = new Container();
        container.setImgLabeling(labeling);
        container.setMetadata(new Example("a",2.0,1));
        context.getService(LabelingIOService.class).saveWithMetaData(container, new File("src/test/resources/labeling/labelSaveTestSimple.tif").getAbsolutePath(), Example.class, new ExampleCodec());
    }


    @Test
    public void loadLabelingWithMetadataPrimitiveTest() throws IOException {
        Container<Example, Integer, IntType> container = context.getService(LabelingIOService.class).loadWithMetadata("src/test/resources/labeling/labelSaveTestSimple.bson", Example.class, new ExampleCodec());
        ImgLabeling<Integer, IntType> mapping = container.getImgLabeling();
        Example e = container.getMetadata();
        Assert.assertNotNull(e);
        Assert.assertEquals(getSimpleImgLabeling().getMapping().getLabels(), mapping.getMapping().getLabels());
    }

    @Test
    public void saveLabelingWithMetadataComplexWithCodecTest() {
        ImgLabeling<Example, IntType> labeling = getComplexImgLabeling();
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        Container container = new Container();
        container.setImgLabeling(labeling);
        container.setMetadata(new Example("a",2.0,1));
        labelingIOService.saveWithMetaData(container, new File("src/test/resources/labeling/labelSaveTestComplex.tif").getAbsolutePath(), Example.class, Example.class, new ExampleCodec());
    }

    @Test
    public void loadLabelingWithMetadataComplexWithCodecTest() throws IOException {
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        Container<Example, Example, IntType> container = labelingIOService.loadWithMetadata("src/test/resources/labeling/labelSaveTestComplex.bson", Example.class,  Example.class, new ExampleCodec());
        ImgLabeling<Example, IntType> mapping = container.getImgLabeling();
        Example e = container.getMetadata();
        Assert.assertNotNull(e);
        Assert.assertEquals(getComplexImgLabeling().getMapping().getLabels(), mapping.getMapping().getLabels());
    }

    @Test
    public void saveLabelingWithMetadataComplexWithFunctionTest() {
        ImgLabeling<Example, IntType> labeling = getComplexImgLabeling();
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        Map<Example, Long> mapping = new HashMap<>();
        AtomicLong atomicLong = new AtomicLong(0);
        labeling.getMapping().getLabels().forEach(label -> mapping.put(label, atomicLong.getAndIncrement()));
        Container container = new Container();
        container.setImgLabeling(labeling);
        container.setMetadata(new Example("a",2.0,1));
        labelingIOService.saveWithMetaData(container, new File("src/test/resources/labeling/labelSaveTestComplexFunction.tif").getAbsolutePath(),
                mapping::get, Example.class, new ExampleCodec());
    }

    @Test
    public void loadLabelingWithMetadataComplexWithFunctionTest() throws IOException {
        Set<Example> labels = getComplexImgLabeling().getMapping().getLabels();
        LabelingIOService labelingIOService = context.getService(LabelingIOService.class);
        Map<Long, Example> map = new HashMap<>();
        AtomicLong atomicLong = new AtomicLong(0);
        labels.forEach(label -> map.put(atomicLong.getAndIncrement(), label));
        Container<Example, Example, UnsignedByteType> container = labelingIOService.loadWithMetadata("src/test/resources/labeling/labelSaveTestComplexFunction.bson", map::get,
                Example.class, new ExampleCodec());
        Example metadata = container.getMetadata();
        Assert.assertNotNull(metadata);
        Assert.assertEquals(new Example("a",2.0,1), metadata);
        Assert.assertEquals(labels, container.getImgLabeling().getMapping().getLabels());
    }

    @Test
    public void encoderClassTest() {
        LabelingMappingCodec<?, ?, ?> labelingMappingCodec = new LabelingMappingCodec.Builder<>().build();
        Class c = labelingMappingCodec.getEncoderClass();

        Assert.assertEquals(Container.class, c);
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

    private ImgLabeling<Example, IntType> getComplexImgLabeling() {
        Example[] values1 = new Example[]{new Example("a", 1.0, 1), new Example("b", 2.24121, 2)};
        Example[] values2 = new Example[]{new Example("a", 1.0, 1)};
        Example[] values3 = new Example[]{new Example("b", 2.24121, 2), new Example("a", 1.0, 1), new Example("a", 1.0, 3)};
        // setup
        Img<IntType> indexImg = ArrayImgs.ints(new int[]{1, 0, 2}, 3);
        List<Set<Example>> labelSets = Arrays.asList(asSet(), asSet(values1), asSet(values2), asSet(values3));
        return ImgLabeling.fromImageAndLabelSets(indexImg, labelSets);
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> asSet(T... values) {
        return new TreeSet<>(Arrays.asList(values));
    }

    private static class ExampleCodec implements Codec<Example> {

        @Override
        public Example decode(BsonReader reader, DecoderContext decoderContext) {
            reader.readStartDocument();
            String a = reader.readString("a");
            double b = reader.readDouble("b");
            int c = reader.readInt32("c");
            reader.readEndDocument();
            return new Example(a, b, c);
        }

        @Override
        public void encode(BsonWriter writer, Example value, EncoderContext encoderContext) {
            writer.writeStartDocument();
            writer.writeString("a", value.a);
            writer.writeDouble("b", value.b);
            writer.writeInt32("c", value.c);
            writer.writeEndDocument();
        }

        @Override
        public Class<Example> getEncoderClass() {
            return Example.class;
        }
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
