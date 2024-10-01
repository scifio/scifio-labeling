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

package net.imglib2.labeling.data;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LabelingData<T, S> {

	private int version = 3;
	private int numSets = 0;
	private int numSources = 0;
	private String indexImg;
	private Map<Integer, T> labelMapping = Collections.emptyMap();
	private Map<String, Set<Integer>> labelSets = Collections.emptyMap();
	private S metadata;

	public int getVersion() {
		return this.version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	public int getNumSets() {
		return this.numSets;
	}

	public void setNumSets(final int numSets) {
		this.numSets = numSets;
	}

	public int getNumSources() {
		return this.numSources;
	}

	public void setNumSources(final int numSources) {
		this.numSources = numSources;
	}

	public String getIndexImg() {
		return this.indexImg;
	}

	public void setIndexImg(final String indexImg) {
		this.indexImg = indexImg;
	}

	public Map<Integer, T> getLabelMapping() {
		return this.labelMapping;
	}

	public void setLabelMapping(final Map<Integer, T> labelMapping) {
		this.labelMapping = labelMapping;
	}

	public Map<String, Set<Integer>> getLabelSets() {
		return this.labelSets;
	}

	public void setLabelSets(final Map<String, Set<Integer>> labelSets) {
		this.labelSets = labelSets;
	}

	public S getMetadata() {
		return this.metadata;
	}

	public void setMetadata(final S metadata) {
		this.metadata = metadata;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || this.getClass() != o.getClass()) return false;
		final LabelingData that = (LabelingData) o;
		return this.numSets == that.numSets && this.indexImg.equals(
			that.indexImg) && this.labelSets.equals(that.labelSets);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.numSets, this.indexImg, this.labelSets);
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

	public LabelingData fromJson(final String json) {
		return new Gson().fromJson(json, this.getClass());
	}

	@Override
	public String toString() {
		return this.toJson();
	}
}
