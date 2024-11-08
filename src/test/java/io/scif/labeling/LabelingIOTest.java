/*-
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2020 - 2024 SCIFIO developers.
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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labeling.data.LabelingData;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;

public class LabelingIOTest {

	private static Context context;

	@BeforeClass
	public static void setUp() {
		context = new Context();
	}

	@AfterClass
	public static void tearDown() {
		context.dispose();
	}

	@Test
	public void testEquality() throws IOException {
		final LabelingIOService labelingIOService = context.getService(
			LabelingIOService.class);
		final ImgLabeling<Integer, IntType> imgLabeling = labelingIOService.load(
			"src/test/resources/labeling/labelSaveTestSimple", Integer.class,
			IntType.class);
		Path tempFile = mktemp();
		labelingIOService.save(imgLabeling, tempFile.toString());
		final ImgLabeling<Integer, IntType> imgLabeling2 = labelingIOService.load(
				tempFile.toString(), Integer.class, IntType.class);
		Assert.assertEquals(imgLabeling.getMapping().getLabels(), imgLabeling2
			.getMapping().getLabels());
	}

	@Test
	public void testEquality2() throws IOException {
		final LabelingIOService labelingIOService = context.getService(
			LabelingIOService.class);
		final ImgLabeling<Integer, IntType> imgLabeling = labelingIOService.load(
			"src/test/resources/labeling/test", Integer.class, IntType.class);
		Path tempFile = mktemp();
		labelingIOService.save(imgLabeling, tempFile.toString());
		final ImgLabeling<Integer, IntType> imgLabeling2 = labelingIOService.load(
				tempFile.toString(), Integer.class, IntType.class);
		Assert.assertEquals(imgLabeling.getMapping().getLabels(), imgLabeling2
			.getMapping().getLabels());
	}

	@Test
	public void saveLabelingWithMetadataPrimitiveTest() throws IOException {
		final ImgLabeling<Integer, UnsignedByteType> labeling =
			getSimpleImgLabeling();
		context.getService(LabelingIOService.class).saveWithMetaData(labeling,
				mktemp().toString(), new Example("a", 2.0, 1));
	}

	@Test
	public void loadLabelingWithMetadataPrimitiveTest() throws IOException {
		final Container<Example, Integer, IntType> container = context.getService(
			LabelingIOService.class).loadWithMetadata(
				"src/test/resources/labeling/labelSaveTestSimpleMeta.tif",
				Example.class, Integer.class, IntType.class);
		final ImgLabeling<Integer, IntType> mapping = container.getImgLabeling();
		final Example e = container.getMetadata();
		Assert.assertNotNull(e);
		Assert.assertEquals(getSimpleImgLabeling().getMapping().getLabels(), mapping
			.getMapping().getLabels());
	}

	@Test
	public void saveLabelingWithMetadataComplexTest() throws IOException {
		final ImgLabeling<Example, IntType> labeling = getComplexImgLabeling();
		final LabelingIOService labelingIOService = context.getService(
			LabelingIOService.class);
		labelingIOService.saveWithMetaData(labeling, mktemp().toString(),
				new Example("a", 2.0, 1));
	}

	@Test
	public void loadLabelingWithMetadataComplexWithCodecTest()
		throws IOException
	{
		final LabelingIOService labelingIOService = context.getService(
			LabelingIOService.class);
		final Container<Example, Example, IntType> container = labelingIOService
			.loadWithMetadata("src/test/resources/labeling/labelSaveTestComplexMeta",
				Example.class, Example.class, IntType.class);
		final ImgLabeling<Example, IntType> mapping = container.getImgLabeling();
		final Example e = container.getMetadata();
		Assert.assertNotNull(e);
		Assert.assertEquals(getComplexImgLabeling().getMapping().getLabels(),
			mapping.getMapping().getLabels());
	}

	private ImgLabeling<Integer, UnsignedByteType> getSimpleImgLabeling() {
		final Integer[] values1 = new Integer[] { 42, 13 };
		final Integer[] values2 = new Integer[] { 1 };
		final Integer[] values3 = new Integer[] { 1, 13, 42 };
		// setup
		final Img<UnsignedByteType> indexImg = ArrayImgs.unsignedBytes(new byte[] {
			1, 0, 2 }, 1);
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
		final Img<IntType> indexImg = ArrayImgs.ints(new int[] { 1, 0, 2 }, 1);
		final List<Set<Example>> labelSets = Arrays.asList(asSet(), asSet(values1),
			asSet(values2), asSet(values3));
		return ImgLabeling.fromImageAndLabelSets(indexImg, labelSets);
	}

	@SuppressWarnings("unchecked")
	private <T> Set<T> asSet(final T... values) {
		return new TreeSet<>(Arrays.asList(values));
	}

	private static class Example implements Comparable<Example> {

		private final String a;

		private final double b;

		private final int c;

		public Example(final String a, final double b, final int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			final Example example = (Example) o;
			return Double.compare(example.b, b) == 0 && c == example.c && Objects
				.equals(a, example.a);
		}

		@Override
		public int hashCode() {
			return Objects.hash(a, b, c);
		}

		@Override
		public int compareTo(final Example o) {
			return this.equals(o) ? 0 : 1;
		}
	}

	public static Path mktemp() throws IOException {
		Path tempFile = Files.createTempFile(null, null);
		tempFile.toFile().deleteOnExit();
		return tempFile;
	}
}
