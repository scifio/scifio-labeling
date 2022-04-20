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

import java.io.IOException;

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

public class E01_LoadLabeling {

	Context context;

	@Before
	public void beforeTests() {
		context = new Context();
	}

	@Test
	public void loadBasicLabeling() throws IOException {
		// get the LabelingIO service from the context
		final LabelingIOService labelingIOService = context.getService(
			LabelingIOService.class);
		// load a JSON file with IntType labels
		// the container contains an ImgLabeling of that type as well as an optional
		// sourcemap
		// the sourcemap is a mapping of a source img to a list of labels that where
		// contained in it and added to
		// the ImgLabeling

		final ImgLabeling<Integer, IntType> imgLabeling = labelingIOService.load(
			"src/test/resources/labeling/labelSaveTestSimple.lbl.json", Integer.class,
			IntType.class);
		Assert.assertNotNull(imgLabeling);
		Assert.assertNotNull(imgLabeling.getIndexImg());
		Assert.assertFalse(imgLabeling.getMapping().getLabels().isEmpty());

	}

	@Test
	public void loadClassBasedLabeling() throws IOException {
		// get the LabelingIO service from the context
		final LabelingIOService labelingIOService = context.getService(
			LabelingIOService.class);
		final Container<Example, Example, IntType> container = labelingIOService
			.loadWithMetadata("src/test/resources/labeling/labelSaveTestComplex",
				Example.class, Example.class, IntType.class);
		final ImgLabeling<Example, IntType> mapping = container.getImgLabeling();
		Assert.assertNotNull(mapping);
	}

}
