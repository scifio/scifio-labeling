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
package net.imglib2.labeling;

import com.google.gson.Gson;
import io.scif.services.DatasetIOService;
import net.imagej.ImageJService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.labeling.data.Container;
import net.imglib2.labeling.data.LabelingData;
import net.imglib2.labeling.utils.LabelingUtil;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.IntegerType;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

import static net.imglib2.labeling.utils.LabelingUtil.TIF_ENDING;

@Plugin(type = ImageJService.class)
public class DefaultLabelingIOService extends AbstractService implements LabelingIOService {
    @Parameter
    private Context context;
    @Parameter
    private DatasetIOService datasetIOService;
    private Gson gson = new Gson();

    @Override
    public <T, I extends IntegerType<I>> ImgLabeling<T, I> load(String file) throws IOException {
        return getImgLabeling(file);
    }

    @Override
    public <S, T, I extends IntegerType<I>> Container<S, T, I> loadWithMetadata(String file, Class<S> metadataClazz) throws IOException {
        LabelingData<T, S> labelingData = readLabelingDataFromJson(file);
        Container container = new Container<>();
        container.setImgLabeling(getImgLabeling(file));
        S metadata = gson.fromJson(gson.toJson(labelingData.getMetadata()), metadataClazz);
        container.setMetadata(metadata);
        return container;
    }

    @Override
    public <S, T, I extends IntegerType<I>> Container<S, T, I> loadWithMetadata(String file, LongFunction<T> idToLabel, Class<S> metadataClazz) {
        throw new NotImplementedException();
    }

    @Override
    public <T, I extends IntegerType<I>> void save(ImgLabeling<T, I> imgLabeling, String file) throws IOException {
        saveWithMetaData(imgLabeling, file, null);
    }

    @Override
    public <S, T, I extends IntegerType<I>> void saveWithMetaData(ImgLabeling<T, I> imgLabeling, String file, S metadata) throws IOException {
        LabelingMapping<T> labelingMapping = imgLabeling.getMapping();
        LabelingData labelingData = createBasicLabelingData(file, labelingMapping);
        if (!labelingMapping.getLabels().isEmpty()) {
            createLabelsets(labelingMapping, labelingData);

        }
        labelingData.setMetadata(metadata);
        final Img<I> img = ImgView.wrap(imgLabeling.getIndexImg(), null);
        LabelingUtil.saveAsTiff(context, LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, Paths.get(file).getParent().toString()), img);
        writeLabelingFile(file, labelingData);
    }

    @Override
    public <S, T, I extends IntegerType<I>> void saveWithMetaData(ImgLabeling<T, I> imgLabeling, String file, ToLongFunction<T> labelToId, S metadata) throws IOException {
        throw new NotImplementedException();
    }

    private <T, I extends IntegerType<I>> ImgLabeling<T, I> getImgLabeling(String file) throws IOException {
        return buildImgLabelingAndImage(file, readLabelingDataFromJson(file));
    }

    private LabelingData readLabelingDataFromJson(String file) throws IOException {
        String path = LabelingUtil.getFilePathWithExtension(file, LabelingUtil.LBL_ENDING, Paths.get(file).getParent().toString());
        Reader reader = Files.newBufferedReader(Paths.get(path));
        return gson.fromJson(reader, LabelingData.class);
    }

    private <T, I extends IntegerType<I>> ImgLabeling<T, I> buildImgLabelingAndImage(String file, LabelingData labelingData) throws IOException {
        int numSets = labelingData.getNumSets();
        String indexImg = labelingData.getIndexImg();
        List<Set<T>> labelSets = readLabelsets(labelingData, numSets);
        RandomAccessibleInterval<I> img = (Img<I>) datasetIOService.open(LabelingUtil.getFilePathWithExtension(indexImg, TIF_ENDING, Paths.get(file).getParent().toString())).getImgPlus().getImg();
        return ImgLabeling.fromImageAndLabelSets(img, labelSets);
    }

    private <T, S> void createLabelsets(LabelingMapping<T> labelingMapping, LabelingData<T, S> labelingData) {
        if (labelingMapping.getLabels().stream().findFirst().get() instanceof Integer) {
            Map<String, Set<Integer>> labels = new HashMap<>();
            for (int i = 0; i < labelingMapping.numSets(); i++) {
                labels.put(Integer.toString(i), (Set<Integer>) labelingMapping.labelsAtIndex(i));
            }
            labelingData.setLabelSets(labels);
        } else {
            Map<String, Set<Integer>> labels = new HashMap<>();
            Map<Integer, T> map = new HashMap<>();
            for (int i = 0; i < labelingMapping.numSets(); i++) {
                Set<Integer> labelset = new HashSet<>();
                for (T value : labelingMapping.labelsAtIndex(i)) {
                    if (!map.containsValue(value)) {
                        map.put(map.size() + 1, value);
                    }
                    int mappedInteger = map.entrySet().stream().filter(entry -> value.equals(entry.getValue()))
                            .map(Map.Entry::getKey).findFirst().get();
                    labelset.add(mappedInteger);
                }
                labels.put(Integer.toString(i), labelset);
            }
            labelingData.setLabelMapping(map);
            labelingData.setLabelSets(labels);
        }
    }

    private <T, S> List<Set<T>> readLabelsets(LabelingData<T, S> labelingData, int numSets) {
        List<Set<T>> labelSets;
        if (labelingData.getLabelMapping() == null || labelingData.getLabelMapping().isEmpty()) {
            labelSets = new ArrayList<>();
            for (Set<Integer> set : labelingData.getLabelSets().values()) {
                Set<T> labelSet = new HashSet<>();
                set.stream().map(v -> ((Number) v).intValue()).forEach(v -> labelSet.add((T) v));
                labelSets.add(labelSet);
            }
        } else {
            labelSets = new ArrayList<>();
            for (int i = 0; i < numSets; i++) {
                Set<T> set = new HashSet<>();
                for (int j : labelingData.getLabelSets().get(Integer.toString(i))) {
                    set.add((T) labelingData.getLabelMapping().get(j));
                }
                labelSets.add(set);
            }
        }
        return labelSets;
    }

    private <T, S> LabelingData<T, S> createBasicLabelingData(String file, LabelingMapping<T> labelingMapping) {
        LabelingData<T, S> labelingData = new LabelingData();
        labelingData.setVersion(LabelingUtil.VERSION);
        labelingData.setNumSets(labelingMapping.numSets());
        labelingData.setNumSources(1);
        labelingData.setIndexImg(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.TIF_ENDING, null));
        return labelingData;
    }

    private <T, S> void writeLabelingFile(String file, LabelingData<T, S> labelingData) throws IOException {
        Writer writer = new FileWriter(LabelingUtil.getFilePathWithExtension(file, LabelingUtil.LBL_ENDING, Paths.get(file).getParent().toString()));
        gson.toJson(labelingData, writer);
        writer.flush();
        writer.close();
    }

}
