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

import static io.scif.labeling.utils.LabelingUtil.TIF_ENDING;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.scif.labeling.data.Container;
import io.scif.labeling.utils.LabelingUtil;
import io.scif.services.DatasetIOService;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

import net.imagej.ImageJService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.labeling.data.LabelingData;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.IntegerType;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;

@Plugin(type = ImageJService.class)
public class DefaultLabelingIOService extends AbstractService implements
	LabelingIOService
{

	@Parameter
	private Context context;
	@Parameter
	private DatasetIOService datasetIOService;
	private final Gson gson = new Gson();

	@Override
	public <T, I extends IntegerType<I>> ImgLabeling<T, I> load(final String file,
		final Class<T> labelType, final Class<I> backingType) throws IOException
	{
		return this.getImgLabeling(file, labelType, backingType);
	}

	@Override
	public <S, T, I extends IntegerType<I>> Container<S, T, I> loadWithMetadata(
		final String file, final Class<S> metadataType, final Class<T> labelType,
		final Class<I> backingType) throws IOException
	{
		final LabelingData<T, S> labelingData = this.readLabelingDataFromJson(file,
			labelType, metadataType);
		final Container<S, T, I> container = new Container<>();
		container.setImgLabeling(this.getImgLabeling(file, labelType, backingType));
		final S metadata = this.gson.fromJson(this.gson.toJson(labelingData
			.getMetadata()), metadataType);
		container.setMetadata(metadata);
		return container;
	}

	@Override
	public <S, T, I extends IntegerType<I>> Container<S, T, I> loadWithMetadata(
		final String file, final LongFunction<T> idToLabel,
		final Class<S> metadataClazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T, I extends IntegerType<I>> void save(
		final ImgLabeling<T, I> imgLabeling, final String file) throws IOException
	{
		this.saveWithMetaData(imgLabeling, file, null);
	}

	@Override
	public <S, T, I extends IntegerType<I>> void saveWithMetaData(
		final ImgLabeling<T, I> imgLabeling, final String file, final S metadata)
		throws IOException
	{
		final LabelingMapping<T> labelingMapping = imgLabeling.getMapping();
		final LabelingData<T, S> labelingData = this.createBasicLabelingData(file,
			labelingMapping);
		if (!labelingMapping.getLabels().isEmpty()) {
			this.createLabelsets(labelingMapping, labelingData);

		}
		labelingData.setMetadata(metadata);
		final Img<I> img = ImgView.wrap(imgLabeling.getIndexImg(), null);
		LabelingUtil.saveAsTiff(this.context, LabelingUtil.getFilePathWithExtension(
			file, TIF_ENDING, Paths.get(file).getParent().toString()), img);
		this.writeLabelingFile(file, labelingData);
	}

	@Override
	public <S, T, I extends IntegerType<I>> void saveWithMetaData(
		final ImgLabeling<T, I> imgLabeling, final String file,
		final ToLongFunction<T> labelToId, final S metadata) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	private <T, I extends IntegerType<I>> ImgLabeling<T, I> getImgLabeling(
		final String file, final Class<T> labelType, final Class<I> backingType)
		throws IOException
	{
		return this.buildImgLabelingAndImage(file, this.readLabelingDataFromJson(
			file, labelType, Object.class), backingType);
	}

	private <T, S> LabelingData<T, S> readLabelingDataFromJson(final String file,
		final Class<T> labelType, final Class<S> metadataType) throws IOException
	{
		final String path = LabelingUtil.getFilePathWithExtension(file,
			LabelingUtil.LBL_ENDING, Paths.get(file).getParent().toString());
		final Reader reader = Files.newBufferedReader(Paths.get(path));
		final Type type = TypeToken //
			.getParameterized(LabelingData.class, labelType, metadataType) //
			.getType();
		return this.gson.fromJson(reader, type);
	}

	private <S, T, I extends IntegerType<I>> ImgLabeling<T, I>
		buildImgLabelingAndImage(final String file,
			final LabelingData<T, S> labelingData, final Class<I> backingType)
			throws IOException
	{
		final int numSets = labelingData.getNumSets();
		final String indexImg = labelingData.getIndexImg();
		final List<Set<T>> labelSets = this.readLabelsets(labelingData, numSets);
		final RandomAccessibleInterval<I> img = (Img<I>) this.datasetIOService.open(
			LabelingUtil.getFilePathWithExtension(indexImg, TIF_ENDING, Paths.get(
				file).getParent().toString())).getImgPlus().getImg();
		return ImgLabeling.fromImageAndLabelSets(img, labelSets);
	}

	private <T, S> void createLabelsets(final LabelingMapping<T> labelingMapping,
		final LabelingData<T, S> labelingData)
	{
		final Optional<T> optional = labelingMapping.getLabels().stream()
			.findFirst();
		if (optional.isPresent() && optional.get() instanceof Integer) {
			final Map<String, Set<Integer>> labels = new HashMap<>();
			for (int i = 0; i < labelingMapping.numSets(); i++) {
				labels.put(Integer.toString(i), (Set<Integer>) labelingMapping
					.labelsAtIndex(i));
			}
			labelingData.setLabelSets(labels);
		}
		else {
			final Map<String, Set<Integer>> labels = new HashMap<>();
			final Map<Integer, T> map = new HashMap<>();
			for (int i = 0; i < labelingMapping.numSets(); i++) {
				final Set<Integer> labelset = new HashSet<>();
				for (final T value : labelingMapping.labelsAtIndex(i)) {
					if (!map.containsValue(value)) {
						map.put(map.size() + 1, value);
					}
					final int mappedInteger = map.entrySet().stream().filter(
						entry -> value.equals(entry.getValue())).map(Map.Entry::getKey)
						.findFirst().get();
					labelset.add(mappedInteger);
				}
				labels.put(Integer.toString(i), labelset);
			}
			labelingData.setLabelMapping(map);
			labelingData.setLabelSets(labels);
		}
	}

	private <T, S> List<Set<T>> readLabelsets(
		final LabelingData<T, S> labelingData, final int numSets)
	{
		final List<Set<T>> labelSets = new ArrayList<>();
		if (labelingData.getLabelMapping() == null || labelingData.getLabelMapping()
			.isEmpty())
		{
			for (final Set<Integer> set : labelingData.getLabelSets().values()) {
				final Set<T> labelSet = new HashSet<>();
				set.stream().map(v -> ((Number) v).intValue()).forEach(v -> labelSet
					.add((T) v));
				labelSets.add(labelSet);
			}
		}
		else {
			for (int i = 0; i < numSets; i++) {
				final Set<T> set = new HashSet<>();
				for (final int j : labelingData.getLabelSets().get(Integer.toString(
					i)))
				{
					set.add(labelingData.getLabelMapping().get(j));
				}
				labelSets.add(set);
			}
		}
		return labelSets;
	}

	private <T, S> LabelingData<T, S> createBasicLabelingData(final String file,
		final LabelingMapping<T> labelingMapping)
	{
		final LabelingData<T, S> labelingData = new LabelingData<>();
		labelingData.setVersion(LabelingUtil.VERSION);
		labelingData.setNumSets(labelingMapping.numSets());
		labelingData.setNumSources(1);
		labelingData.setIndexImg(LabelingUtil.getFilePathWithExtension(file,
			TIF_ENDING, null));
		return labelingData;
	}

	private <T, S> void writeLabelingFile(final String file,
		final LabelingData<T, S> labelingData) throws IOException
	{
		final Writer writer = new FileWriter(LabelingUtil.getFilePathWithExtension(
			file, LabelingUtil.LBL_ENDING, Paths.get(file).getParent().toString()));
		final Type labelingDataType = new TypeToken<LabelingData<T, S>>() {}
			.getType();
		this.gson.toJson(labelingData, labelingDataType, writer);
		writer.flush();
		writer.close();
	}

}
