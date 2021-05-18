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

import net.imagej.ImageJService;
import net.imglib2.roi.io.labeling.data.ImgLabelingContainer;
import net.imglib2.type.numeric.IntegerType;
import org.bson.codecs.Codec;

import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

public interface LabelingIOService extends ImageJService {

    /**
     * Load a labeling container from the given file path as string.
     * The file path must point to the bson file containing the labeling data.
     *
     * @param file The path to the file
     * @param <T>  the label value
     * @param <I>  IntegerType for the pixel value
     * @return a container object holding the ImgLabeling (as well as an optional source mapping)
     */
    <T, I extends IntegerType<I>> ImgLabelingContainer<T, I> open(String file);

    /**
     * Load a labeling container from the given file path as string.
     * The file path must point to the bson file containing the labeling data.
     *
     * @param file   The path to the file
     * @param clazz  The class to convert the contains of the file to
     * @param codecs One or more codecs necessary to convert the data to clazz.
     * @param <T>    the label value
     * @param <I>    IntegerType for the pixel value
     * @return a container object holding the ImgLabeling (as well as an optional source mapping)
     */
    <T, I extends IntegerType<I>> ImgLabelingContainer<T, I> open(String file, Class clazz, Codec<T>... codecs);

    /**
     * Load a labeling container from the given file path as string.
     * The file path must point to the bson file containing the labeling data.
     *
     * @param file      The path to the file
     * @param idToLabel a function transforming the label of type <T> into something else
     * @param <T>       the label value
     * @param <I>       IntegerType for the pixel value
     * @return a container object holding the ImgLabeling (as well as an optional source mapping)
     */
    <T, I extends IntegerType<I>> ImgLabelingContainer<T, I> open(String file, LongFunction<T> idToLabel);

    /**
     * Save an ImgLabelingContainer in the file-path, transforming it into a bson file and an image.
     * The path must contain the filename (ending does not matter).
     *
     * @param imgLabelingContainer the container with the ImgLabeling and an optional source map
     * @param file                 the path pointing to the file, including the filename
     * @param <T>                  the label value
     * @param <I>                  IntegerType for the pixel value
     */
    <T, I extends IntegerType<I>> void save(ImgLabelingContainer<T, I> imgLabelingContainer, String file);

    /**
     * Save an ImgLabelingContainer in the file-path, transforming it into a bson file and an image.
     * The path must contain the filename (ending does not matter).
     *
     * @param clazz                the class of the type T that is contained in the labeling
     * @param codecs               one or more codecs to convert clazz into something bsonifyable
     * @param imgLabelingContainer the container with the ImgLabeling and an optional source map
     * @param file                 the path pointing to the file, including the filename
     * @param <T>                  the label value
     * @param <I>                  IntegerType for the pixel value
     */
    <T, I extends IntegerType<I>> void save(ImgLabelingContainer<T, I> imgLabelingContainer, String file, Class clazz, Codec<T>... codecs);

    /**
     * Save an ImgLabelingContainer in the file-path, transforming it into a bson file and an image.
     * The path must contain the filename (ending does not matter).
     *
     * @param labelToId            a function to convert the type T to a long value.
     * @param imgLabelingContainer the container with the ImgLabeling and an optional source map
     * @param file                 the path pointing to the file, including the filename
     * @param <T>                  the label value
     * @param <I>                  IntegerType for the pixel value
     */
    <T, I extends IntegerType<I>> void save(ImgLabelingContainer<T, I> imgLabelingContainer, String file, ToLongFunction<T> labelToId);

}
