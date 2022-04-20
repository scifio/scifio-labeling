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

import io.scif.labeling.data.Container;

import java.io.IOException;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

import net.imagej.ImageJService;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;

/**
 * A service to easily access a load/save functionality for JSON-based labeling
 * data files. Basic support for primitive types and JSON standard types is
 * already included. For non-primitive types, a codec must be set and the class
 * must be given as an argument to the methods.
 *
 * @author Tom Burke
 */
public interface LabelingIOService extends ImageJService {

	<T, I extends IntegerType<I>> ImgLabeling<T, I> load(String file,
		Class<T> labelType, Class<I> backingType) throws IOException;

	<T, I extends IntegerType<I>> void save(ImgLabeling<T, I> imgLabeling,
		String file) throws IOException;

	/**
	 * Load a labeling container from the given file path as string. The file path
	 * must point to the JSON file containing the labeling data.
	 *
	 * @param file The path to the file
	 * @param metadataType the metadata class
	 * @param <T> the label value
	 * @param <I> IntegerType for the pixel value
	 * @param <S> Class of the meta data
	 * @return a container object holding the ImgLabeling (as well as an optional
	 *         source mapping)
	 * @throws IOException on file read fail
	 */
	<S, T, I extends IntegerType<I>> Container<S, T, I> loadWithMetadata(
		String file, Class<S> metadataType, Class<T> labelType,
		Class<I> backingType) throws IOException;

	/**
	 * Load a labeling container from the given file path as string. The file path
	 * must point to the JSON file containing the labeling data.
	 *
	 * @param file The path to the file
	 * @param idToLabel a function transforming the label of type T into something
	 *          else
	 * @param metadataClazz the metadata class
	 * @param <T> the label value
	 * @param <I> IntegerType for the pixel value
	 * @param <S> Class of the meta data
	 * @return a container object holding the ImgLabeling (as well as an optional
	 *         source mapping)
	 * @throws IOException on file read fail
	 */
	<S, T, I extends IntegerType<I>> Container<S, T, I> loadWithMetadata(
		String file, LongFunction<T> idToLabel, Class<S> metadataClazz)
		throws IOException;

	/**
	 * Save an ImgLabelingContainer in the file-path, transforming it into a JSON
	 * file and an image. The path must contain the filename (ending does not
	 * matter).
	 *
	 * @param imgLabeling the imglabeling object that needs to be serialized
	 * @param file the path pointing to the file, including the filename
	 * @param <T> the label value
	 * @param <I> IntegerType for the pixel value
	 * @param <S> Class of the meta data
	 */
	<S, T, I extends IntegerType<I>> void saveWithMetaData(
		ImgLabeling<T, I> imgLabeling, String file, S metadata) throws IOException;

	/**
	 * Save an ImgLabelingContainer in the file-path, transforming it into a JSON
	 * file and an image. The path must contain the filename (ending does not
	 * matter).
	 *
	 * @param imgLabeling the imglabeling object that needs to be serialized
	 * @param labelToId a function to convert the type T to a long value.
	 * @param file the path pointing to the file, including the filename
	 * @param <T> the label value
	 * @param <I> IntegerType for the pixel value
	 * @param <S> Class of the meta data
	 */
	<S, T, I extends IntegerType<I>> void saveWithMetaData(
		ImgLabeling<T, I> imgLabeling, String file, ToLongFunction<T> labelToId,
		S metadata) throws IOException;

}
