package net.imglib2.roi.io.labeling;

import net.imagej.ImageJService;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import org.bson.codecs.Codec;

import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

public interface LabelingIOService extends ImageJService {

    <T, I extends IntegerType<I>> ImgLabeling<T, I> open(String file);

    <T, I extends IntegerType<I>> ImgLabeling<T, I> open(String file, Class clazz, Codec<T>... codecs);

    <T, I extends IntegerType<I>> ImgLabeling<T, I> open(String file, LongFunction<T> idToLabel);

    <T, I extends IntegerType<I>> void save(ImgLabeling<T, I> imgLabeling, String file);

    <T, I extends IntegerType<I>> void save(ImgLabeling<T, I> imgLabeling, String file, Class clazz, Codec<T>... codecs);

    <T, I extends IntegerType<I>> void save(ImgLabeling<T, I> imgLabeling, String file, ToLongFunction<T> labelToId);

}