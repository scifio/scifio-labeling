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

import io.scif.services.DatasetIOService;
import net.imagej.ImageJService;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.roi.io.labeling.codecs.ImgLabelingCodec;
import net.imglib2.roi.io.labeling.codecs.LabelingMappingCodec;
import net.imglib2.roi.io.labeling.data.Container;
import net.imglib2.roi.io.labeling.data.ImgLabelingContainer;
import net.imglib2.roi.io.labeling.data.LabelingContainer;
import net.imglib2.roi.io.labeling.utils.LabelingUtil;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.IntegerType;
import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonReader;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.io.BasicOutputBuffer;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

@Plugin(type = ImageJService.class)
public class DefaultLabelingIOService extends AbstractService implements LabelingIOService {
    CodecRegistry registry = CodecRegistries.fromProviders(new BsonValueCodecProvider(), new DocumentCodecProvider()
            , new ValueCodecProvider());
    @Parameter
    private Context context;
    @Parameter
    private DatasetIOService datasetIOService;

    @Override
    public void initialize() {
        addCodecs(new BsonArrayCodec(registry));
    }

    public <T, I extends IntegerType<I>> ImgLabeling<T, I> load(String file) throws IOException {
        ImgLabelingCodec<T, I> imgLabelingCodec = new ImgLabelingCodec.Builder<T, I>().setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, ""))
                .setCodecRegistry(registry).setFile(Paths.get(file)).setDatasetIOService(datasetIOService).build();
        RandomAccessFile aFile = new RandomAccessFile(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.BSON_ENDING, Paths.get(file).getParent().toString()), "r");
        FileChannel inChannel = aFile.getChannel();
        MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        BsonReader bsonReader = new BsonBinaryReader(buffer);
        ImgLabeling<T, I> imgLabeling = imgLabelingCodec.decode(bsonReader, DecoderContext.builder().build());
        return imgLabeling;
    }

    @Override
    public <T, I extends IntegerType<I>> ImgLabeling<T, I> load(String file, Class<T> clazz, Codec<T>... codecs) throws IOException {
        addCodecs(codecs);
        ImgLabelingCodec<T, I> imgLabelingCodec = new ImgLabelingCodec.Builder<T, I>().setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, ""))
                .setClazz(clazz).setCodecRegistry(registry).setFile(Paths.get(file)).setDatasetIOService(datasetIOService).build();
        RandomAccessFile aFile = new RandomAccessFile(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.BSON_ENDING, Paths.get(file).getParent().toString()), "r");
        FileChannel inChannel = aFile.getChannel();
        MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        BsonReader bsonReader = new BsonBinaryReader(buffer);
        ImgLabeling<T, I> imgLabeling = imgLabelingCodec.decode(bsonReader, DecoderContext.builder().build());
        return imgLabeling;
    }

    @Override
    public <T, I extends IntegerType<I>> void save(ImgLabeling<T,I> imgLabeling, String file) throws IOException {
        ImgLabelingCodec<T, I> imgLabelingCodec = new ImgLabelingCodec.Builder<T, I>().setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, Paths.get(file).getParent().toString())).setCodecRegistry(registry).build();
        saveLabeling(imgLabeling, file, imgLabelingCodec);
    }

    @Override
    public <T, I extends IntegerType<I>> void save(ImgLabeling<T,I> imgLabeling, String file, Class<T> clazz, Codec<T>... codecs) throws IOException {
        addCodecs(codecs);
        ImgLabelingCodec<T, I> imgLabelingCodec = new ImgLabelingCodec.Builder<T, I>().setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, Paths.get(file).getParent().toString())).setClazz(clazz).setCodecRegistry(registry).build();
        saveLabeling(imgLabeling, file, imgLabelingCodec);
    }

    private <T, I extends IntegerType<I>> void saveLabeling(ImgLabeling<T, I> imgLabeling, String file, ImgLabelingCodec<T, I> imgLabelingCodec) {
        imgLabelingCodec.setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, ""));
        BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(outputBuffer);
        imgLabelingCodec.encode(writer, imgLabeling, EncoderContext.builder().isEncodingCollectibleDocument(false).build());
        LabelingUtil.writeToFile(outputBuffer, new File(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.BSON_ENDING, Paths.get(file).getParent().toString())));
        final Img<I> img = ImgView.wrap(imgLabeling.getIndexImg(), null);
        LabelingUtil.saveAsTiff(context, LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, Paths.get(file).getParent().toString()), img);
    }

    @Override
    public <S, T, I extends IntegerType<I>> Container<S, T, I> loadWithMetadata(String file, Class<S> metadataClazz, Codec<?>... codecs) throws IOException {
        addCodecs(codecs);
        LabelingMappingCodec<S,T, I> labelingMappingCodec = new LabelingMappingCodec.Builder<S,T, I>()
                .setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, ""))
                .setMetadataClazz(metadataClazz)
                .setFile(Paths.get(file)).setDatasetIOService(datasetIOService)
                .setCodecRegistry(registry).build();
        RandomAccessFile aFile = new RandomAccessFile(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.BSON_ENDING, Paths.get(file).getParent().toString()), "r");
        FileChannel inChannel = aFile.getChannel();
        MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        BsonReader bsonReader = new BsonBinaryReader(buffer);
        Container<S, T, I> container = labelingMappingCodec.decode(bsonReader, DecoderContext.builder().build());
        return container;

    }

    @Override
    public <S, T, I extends IntegerType<I>> Container<S, T, I> loadWithMetadata(String file, Class<T> clazz, Class<S> metadataClazz, Codec<?>... codecs) throws IOException {
        addCodecs(codecs);
        LabelingMappingCodec<S,T, I> labelingMappingCodec = new LabelingMappingCodec.Builder<S,T, I>()
                .setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, ""))
                .setClazz(clazz).setMetadataClazz(metadataClazz)
                .setFile(Paths.get(file)).setDatasetIOService(datasetIOService)
                .setCodecRegistry(registry).build();
        RandomAccessFile aFile = new RandomAccessFile(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.BSON_ENDING, Paths.get(file).getParent().toString()), "r");
        FileChannel inChannel = aFile.getChannel();
        MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        BsonReader bsonReader = new BsonBinaryReader(buffer);
        Container<S, T, I> container = labelingMappingCodec.decode(bsonReader, DecoderContext.builder().build());
        return container;

    }

    @Override
    public <S, T, I extends IntegerType<I>> Container<S, T, I> loadWithMetadata(String file, LongFunction<T> idToLabel, Class<S> metadataClazz, Codec<?>... codecs) throws IOException {
        addCodecs(codecs);
        LabelingMappingCodec<S, T, I> labelingMappingCodec = new LabelingMappingCodec.Builder<S, T, I>()
                .setMetadataClazz(metadataClazz)
                .setFile(Paths.get(file)).setDatasetIOService(datasetIOService)
                .setCodecRegistry(registry).setIdToLabel(idToLabel).build();
        Path labelingFile = Paths.get(file);
        RandomAccessFile aFile = new RandomAccessFile(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.BSON_ENDING, labelingFile.getParent().toString()), "r");
        FileChannel inChannel = aFile.getChannel();
        MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        BsonReader bsonReader = new BsonBinaryReader(buffer);

        Container<S, T, I> container = labelingMappingCodec.decode(bsonReader, DecoderContext.builder().build());
        return container;

    }

    @Override
    public <S, T, I extends IntegerType<I>> void saveWithMetaData(Container<S, T, I> container, String file, Class<S> metadataClazz, Codec<?>... codecs) {
        addCodecs(codecs);
        LabelingMappingCodec<S, T, I> labelingMappingCodec = new LabelingMappingCodec.Builder<S, T, I>()
                .setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, Paths.get(file).getParent().toString()))
                .setMetadataClazz(metadataClazz)
                .setFile(Paths.get(file)).setDatasetIOService(datasetIOService)
                .setCodecRegistry(registry).build();
        labelingMappingCodec.setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, ""));
        BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(outputBuffer);
        labelingMappingCodec.encode(writer, container, EncoderContext.builder().isEncodingCollectibleDocument(false).build());
        LabelingUtil.writeToFile(outputBuffer, new File(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.BSON_ENDING, Paths.get(file).getParent().toString())));
        final Img<I> img = ImgView.wrap(container.getImgLabeling().getIndexImg(), null);
        LabelingUtil.saveAsTiff(context, LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, Paths.get(file).getParent().toString()), img);

    }

    @Override
    public <S, T, I extends IntegerType<I>> void saveWithMetaData(Container<S, T, I> container, String file, Class<T> clazz, Class<S> metadataClazz, Codec<?>... codecs) {
        addCodecs(codecs);
        LabelingMappingCodec<S, T, I> labelingMappingCodec = new LabelingMappingCodec.Builder<S, T, I>()
                .setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, Paths.get(file).getParent().toString()))
                .setClazz(clazz).setMetadataClazz(metadataClazz)
                .setFile(Paths.get(file)).setDatasetIOService(datasetIOService)
                .setCodecRegistry(registry).build();
        labelingMappingCodec.setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, ""));
        BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(outputBuffer);
        labelingMappingCodec.encode(writer, container, EncoderContext.builder().isEncodingCollectibleDocument(false).build());
        LabelingUtil.writeToFile(outputBuffer, new File(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.BSON_ENDING, Paths.get(file).getParent().toString())));
        final Img<I> img = ImgView.wrap(container.getImgLabeling().getIndexImg(), null);
        LabelingUtil.saveAsTiff(context, LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, Paths.get(file).getParent().toString()), img);

    }

    @Override
    public <S, T, I extends IntegerType<I>> void saveWithMetaData(Container<S, T, I> container, String file, ToLongFunction<T> labelToId, Class<S> metadataClazz, Codec<?>... codecs) {
        addCodecs(codecs);
        LabelingMappingCodec<S, T, I> labelingMappingCodec = new LabelingMappingCodec.Builder<S, T, I>()
                .setMetadataClazz(metadataClazz)
                .setFile(Paths.get(file)).setDatasetIOService(datasetIOService)
                .setCodecRegistry(registry).setLabelToId(labelToId).build();
        labelingMappingCodec.setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, ""));
        BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(outputBuffer);
        labelingMappingCodec.encode(writer, container, EncoderContext.builder().isEncodingCollectibleDocument(false).build());
        LabelingUtil.writeToFile(outputBuffer, new File(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.BSON_ENDING, Paths.get(file).getParent().toString())));
        final Img<I> img = ImgView.wrap(container.getImgLabeling().getIndexImg(), null);
        LabelingUtil.saveAsTiff(context, LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, Paths.get(file).getParent().toString()), img);

    }

    private CodecRegistry getRegistry() {
        return registry;
    }

    /**
     * Overwrites the complete CodecRegistry.
     *
     * @param registry the registry to override the existing one
     */
    private void setRegistry(CodecRegistry registry) {
        this.registry = registry;
    }

    /**
     * Adds the codecs contained in one or more {@link CodecRegistry} to the current registry.
     *
     * @param registries a number of registries to merge into the existing one
     */
    private void addCodecRegistries(CodecRegistry... registries) {
        this.registry = CodecRegistries.fromRegistries(getRegistry(), CodecRegistries.fromRegistries(registries));
    }

    /**
     * Adds the codecs contained in one or more {@link CodecProvider} to the current registry.
     *
     * @param providers a number of providers to merge into the existing registry
     */
    private void addCodecProviders(CodecProvider... providers) {
        this.registry = CodecRegistries.fromRegistries(getRegistry(), CodecRegistries.fromProviders(providers));
    }

    /**
     * Adds one or more {@link Codec} to the current registry.
     *
     * @param codecs a number of codecs to merge into the existing registry
     */
    @SafeVarargs
    public final void addCodecs(Codec<?>... codecs) {
        this.registry = CodecRegistries.fromRegistries(getRegistry(), CodecRegistries.fromCodecs(codecs));
    }
}
