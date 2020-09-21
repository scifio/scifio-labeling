/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2020 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import io.scif.img.ImgIOException;
import io.scif.img.ImgSaver;
import io.scif.services.DatasetIOService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonReader;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.io.BasicOutputBuffer;
import org.scijava.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

/**
 * A utility class to easily access a load/save functionality for BSON-based labeling data files.
 * Basic support for primitive types and BSON standard types is already included. For non-primitive types,
 * a codec must be set and the class must be given as an argument to the methods.
 * Examples for Codecs can be found at {@link LabelingMappingCodec}.
 *
 * @author Tom Burke
 */
public class LabelingIO {

    private final DatasetIOService datasetIOService;

    private final Context context;

    private static final String BSON_ENDING = ".bson";

    private static final String TIF_ENDING = ".tif";

    CodecRegistry registry = CodecRegistries.fromProviders(new BsonValueCodecProvider(), new DocumentCodecProvider()
            , new ValueCodecProvider());

    LabelingIO(Context context, DatasetIOService datasetIOService){
        this.context = context;
        this.datasetIOService = datasetIOService;
    }

    <T, I extends IntegerType<I>> void saveLabeling(ImgLabeling<T, I> labelingData, String fileName) {
        this.saveLabeling(labelingData, fileName, (Class) null);
    }

    <T, I extends IntegerType<I>> ImgLabeling<T, I> loadLabeling(String fileName) throws IOException {
        return loadLabeling(fileName, (Class) null);
    }

    /**
     * Saves the {@link LabelingMapping} of an {@link ImgLabeling} at the specified path.
     * For the save to work correctly with non-primitive types, a codec must be added to the registry through
     * one of the available methods in this class.
     *
     * @param labelingData  the complete labeling data
     * @param clazz        the class that represents one label
     * @param fileName     the path to the file
     * @param <T> the class that represents a label
     * @param <I> the value type of the image
     */
    <T, I extends IntegerType<I>> void saveLabeling(ImgLabeling<T, I> labelingData, String fileName, Class clazz) {

        LabelingMappingCodec<T> labelingMappingCodec = new LabelingMappingCodec.Builder<T>().setIndexImg(getFilePathWithExtension(fileName, TIF_ENDING)).setClazz(clazz).setCodecRegistry(registry).build();
        BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(outputBuffer);
        labelingMappingCodec.encode(writer, labelingData.getMapping(), EncoderContext.builder().isEncodingCollectibleDocument(false).build());
        writeToFile(outputBuffer, new File(getFilePathWithExtension(fileName, BSON_ENDING)));
        final Img<I> img = ImgView.wrap(labelingData.getIndexImg(), null);
        saveAsTiff(getFilePathWithExtension(fileName, TIF_ENDING), img);
    }

    /**
     * Loads the {@link LabelingMapping} of an {@link ImgLabeling} from the specified path.
     * The Class-argument is only necessary if the labeling type is non-primitive.
     * For the load to work correctly with non-primitive types, a codec must be added to the registry through
     * one of the available methods in this class.
     *
     * @param fileName the path to the file
     * @param clazz    can be null if labeling is a primitive type
     * @param <T> the class that represents a label
     * @param <I> the value type of the image
     * @return the Labeling in the file
     * @throws IOException
     */
    public <T, I extends IntegerType<I>> ImgLabeling<T, I> loadLabeling(String fileName, Class<T> clazz) throws IOException {
        LabelingMappingCodec<T> labelingMappingCodec = new LabelingMappingCodec.Builder<T>().setIndexImg(getFilePathWithExtension(fileName, TIF_ENDING)).setClazz(clazz).setCodecRegistry(registry).build();
        RandomAccessFile aFile = new RandomAccessFile(getFilePathWithExtension(fileName, BSON_ENDING), "r");
        FileChannel inChannel = aFile.getChannel();
        MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        BsonReader bsonReader = new BsonBinaryReader(buffer);
        LabelingMapping<T> labelingMapping =labelingMappingCodec.decode(bsonReader, DecoderContext.builder().build());
        Img<I> indexImg = null;
        if(datasetIOService.canOpen(getFilePathWithExtension(labelingMappingCodec.getIndexImg(), TIF_ENDING))){
            indexImg = (Img<I>) datasetIOService.open(getFilePathWithExtension(labelingMappingCodec.getIndexImg(), TIF_ENDING)).getImgPlus().getImg();
        }else{
            throw new IOException("Image referred to in bson-file could not be opened");
        }
        return ImgLabeling.fromImageAndLabelSets(indexImg, labelingMapping.getLabelSets());
    }

    public <T, I extends IntegerType<I>> void saveLabeling(ImgLabeling<T, I> labelingData, String fileName, ToLongFunction<T> labelToId) {
        LabelingMappingCodec<T> labelingMappingCodec = new LabelingMappingCodec.Builder<T>().setCodecRegistry(registry).setLabelToId(labelToId).build();
        labelingMappingCodec.setIndexImg(getFilePathWithExtension(fileName, TIF_ENDING));
        BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(outputBuffer);
        labelingMappingCodec.encode(writer, labelingData.getMapping(), EncoderContext.builder().isEncodingCollectibleDocument(false).build());
        writeToFile(outputBuffer, new File(getFilePathWithExtension(fileName, BSON_ENDING)));

        final Img<I> img = ImgView.wrap(labelingData.getIndexImg(), null);
        saveAsTiff(getFilePathWithExtension(fileName, TIF_ENDING), img);
    }

    public <T, I extends IntegerType<I>> ImgLabeling<T, I> loadLabeling(String fileName, LongFunction<T> idToLabel) throws IOException {

        LabelingMappingCodec<T> labelingMappingCodec = new LabelingMappingCodec.Builder<T>().setCodecRegistry(registry).setIdToLabel(idToLabel).build();

        RandomAccessFile aFile = new RandomAccessFile(getFilePathWithExtension(fileName, BSON_ENDING), "r");
        FileChannel inChannel = aFile.getChannel();
        MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        BsonReader bsonReader = new BsonBinaryReader(buffer);

        LabelingMapping<T> labelingMapping = labelingMappingCodec.decode(bsonReader, DecoderContext.builder().build());
        Img<I> indexImg = null;
        if(datasetIOService.canOpen(getFilePathWithExtension(labelingMappingCodec.getIndexImg(), TIF_ENDING))){
            indexImg = (Img<I>) datasetIOService.open(getFilePathWithExtension(labelingMappingCodec.getIndexImg(), TIF_ENDING)).getImgPlus().getImg();
        }else{
            throw new IOException("Image referred to in bson-file could not be opened");
        }
        return ImgLabeling.fromImageAndLabelSets(indexImg, labelingMapping.getLabelSets());
    }

    private void writeToFile(BasicOutputBuffer outputBuffer, File file) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            outputBuffer.pipe(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFilePathWithExtension(final String filename, final String extension) {
        if (filename.endsWith(extension)) {
            return filename;
        }
        final int index = filename.lastIndexOf(".");
        return filename.substring(0, index == -1 ? filename.length() : index).concat(extension);
    }

    /**
     *
     *
     * @param filename
     * @param rai
     * @param <T>
     */
    private <T extends RealType<T>> void saveAsTiff(
            final String filename,
            final RandomAccessibleInterval<T> rai) {

		try {
			new ImgSaver( context ).saveImg( filename, ImgView.wrap( rai, null ) );
		} catch ( ImgIOException | IncompatibleTypeException e ) {
			e.printStackTrace();
		}
    }

    public CodecRegistry getRegistry() {
        return registry;
    }

    /**
     * Overwrites the complete CodecRegistry.
     *
     * @param registry the registry to override the existing one
     */
    public void setRegistry(CodecRegistry registry) {
        this.registry = registry;
    }

    /**
     * Adds the codecs contained in one or more {@link CodecRegistry} to the current registry.
     *
     * @param registries a number of registries to merge into the existing one
     */
    public void addCodecRegistries(CodecRegistry... registries) {
        this.registry = CodecRegistries.fromRegistries(getRegistry(), CodecRegistries.fromRegistries(registries));
    }

    /**
     * Adds the codecs contained in one or more {@link CodecProvider} to the current registry.
     *
     * @param providers a number of providers to merge into the existing registry
     */
    public void addCodecProviders(CodecProvider... providers) {
        this.registry = CodecRegistries.fromRegistries(getRegistry(), CodecRegistries.fromProviders(providers));
    }

    /**
     * Adds one or more {@link Codec} to the current registry.
     *
     * @param codecs a number of codecs to merge into the existing registry
     * @param <T> the class the codec encodes
     */
    @SafeVarargs
    public final <T> void addCodecs(Codec<T>... codecs) {
        this.registry = CodecRegistries.fromRegistries(getRegistry(), CodecRegistries.fromCodecs(codecs));
    }
}

