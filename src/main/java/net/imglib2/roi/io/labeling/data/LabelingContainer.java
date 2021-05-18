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
package net.imglib2.roi.io.labeling.data;

import net.imglib2.roi.labeling.LabelingMapping;

import java.util.*;

public class LabelingContainer<T> {

    List<Set<T>> labelSets;
    Map<String, Set<Integer>> sourceToLabel = new HashMap<>();
    LabelingMapping<T> labelingMapping;


    public LabelingContainer(List<Set<T>> labelSets, Map<String, Set<Integer>> sourceToLabel, LabelingMapping<T> labelingMapping) {
        this.labelSets = labelSets;
        this.sourceToLabel = sourceToLabel;
        this.labelingMapping = labelingMapping;
    }

    public LabelingContainer() {

    }

    public void addLabelToSource(String source, Integer label) {
        sourceToLabel.putIfAbsent(source, new HashSet<>());
        sourceToLabel.get(source).add(label);
    }

    public List<Set<T>> getLabelSets() {
        return labelSets;
    }

    public void setLabelSets(List<Set<T>> labelSets) {
        if (labelingMapping != null)
            labelingMapping.setLabelSets(labelSets);
        this.labelSets = labelSets;
    }

    public Map<String, Set<Integer>> getSourceToLabel() {
        return sourceToLabel;
    }

    public void setSourceToLabel(Map<String, Set<Integer>> sourceToLabel) {
        this.sourceToLabel = sourceToLabel;
    }

    public LabelingMapping<T> getLabelingMapping() {
        return labelingMapping;
    }

    public void setLabelingMapping(LabelingMapping<T> labelingMapping) {
        this.labelingMapping = labelingMapping;
        if (labelSets != null)
            labelingMapping.setLabelSets(labelSets);
    }
}
