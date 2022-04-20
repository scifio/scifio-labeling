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

package io.scif.labeling.tutorials;

import io.scif.labeling.LabelingIOService;
import io.scif.labeling.data.Container;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

public class E02_SaveLabeling {

	Context context;

	@Before
	public void beforeTests() {
		context = new Context();
	}

	@Test
	public void saveLabelingTest() throws IOException {
		final ImgLabeling<Integer, UnsignedByteType> labeling =
			getSimpleImgLabeling();
		// get the LabelingIO service from the context
		final LabelingIOService labelingIOService = context.getService(
			LabelingIOService.class);
		labelingIOService.save(labeling, new File(
			"src/test/resources/labeling/labelSaveTestSimple.tif").getAbsolutePath());

	}

	@Test
	public void saveLabelingTest2() throws IOException {
		final ImgLabeling<Example, IntType> labeling = getComplexImgLabeling();
		// get the LabelingIO service from the context
		final LabelingIOService labelingIOService = context.getService(
			LabelingIOService.class);
		labelingIOService.save(labeling, new File(
			"src/test/resources/labeling/labelSaveTestComplex.tif")
				.getAbsolutePath());

	}

	@Test
	public void saveLabelingWithMetaDataTest() throws IOException {
		final ImgLabeling<Integer, UnsignedByteType> labeling =
			getSimpleImgLabeling();
		final Container<Map, Integer, UnsignedByteType> container =
			new Container<>();
		container.setImgLabeling(labeling);
		// If you want to store metadata as a map, make sure that their are codecs
		// for every value.
		// Also, the key of the map must be string. If you need anything else,
		// implement your own map codec.
		final Map<String, Integer> sources = new HashMap<>();
		sources.put("one", 1);
		sources.put("two", 2);
		sources.put("three", 3);

		container.setMetadata(sources);

		// get the LabelingIO service from the context
		final LabelingIOService labelingIOService = context.getService(
			LabelingIOService.class);
		labelingIOService.saveWithMetaData(labeling, new File(
			"src/test/resources/labeling/labelSaveTestSimpleMeta.tif")
				.getAbsolutePath(), sources);

	}

	private ImgLabeling<Integer, UnsignedByteType> getSimpleImgLabeling() {
		final Integer[] values1 = new Integer[] { 42, 13 };
		final Integer[] values2 = new Integer[] { 1 };
		final Integer[] values3 = new Integer[] { 1, 13, 42 };
		// setup
		final Img<UnsignedByteType> indexImg = ArrayImgs.unsignedBytes(new byte[] {
			1, 0, 2 }, 3);
		final List<Set<Integer>> labelSets = Arrays.asList(asSet(), asSet(values1),
			asSet(values2), asSet(values3));
		return ImgLabeling.fromImageAndLabelSets(indexImg, labelSets);
	}

	private ImgLabeling<Example, IntType> getComplexImgLabeling() {
		final Example[] values1 = new Example[] { new Example("a", 1.0, 1),
			new Example("b", 2.24121, 2) };
		final Example[] values2 = new Example[] { new Example("a", 1.0, 1) };
		final Example[] values3 = new Example[] { new Example("b", 2.24121, 2),
			new Example("a", 1.0, 1), new Example("a", 1.0, 3) };
		// setup
		final Img<IntType> indexImg = ArrayImgs.ints(new int[] { 1, 0, 2 }, 3);
		final List<Set<Example>> labelSets = Arrays.asList(asSet(), asSet(values1),
			asSet(values2), asSet(values3));
		return ImgLabeling.fromImageAndLabelSets(indexImg, labelSets);
	}

	@SuppressWarnings("unchecked")
	private <T> Set<T> asSet(final T... values) {
		return new TreeSet<>(Arrays.asList(values));
	}

}
