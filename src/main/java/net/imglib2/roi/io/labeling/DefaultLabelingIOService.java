/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2020 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import io.scif.services.DatasetIOService;
import net.imagej.ImageJService;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import org.bson.codecs.Codec;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;

import java.io.IOException;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

@Plugin(type = ImageJService.class)
public class DefaultLabelingIOService extends AbstractService implements LabelingIOService {
    @Parameter
    private Context context;

    @Parameter
    private DatasetIOService datasetIOService;

    private LabelingIO io;

    @Override
    public <T, I extends IntegerType<I>> ImgLabeling<T, I> open(String file) {
        try {
            return io.loadLabeling(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T, I extends IntegerType<I>> ImgLabeling<T, I> open(String file, Class clazz, Codec<T>... codecs) {
        try {
            io.addCodecs(codecs);
            return io.loadLabeling(file, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T, I extends IntegerType<I>> ImgLabeling<T, I> open(String file, LongFunction<T> idToLabel) {
        try {
            return io.loadLabeling(file, idToLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T, I extends IntegerType<I>> void save(ImgLabeling<T, I> imgLabeling, String file) {
        io.saveLabeling(imgLabeling, file);
    }

    @Override
    public <T, I extends IntegerType<I>> void save(ImgLabeling<T, I> imgLabeling, String file, Class clazz, Codec<T>... codecs) {
        io.addCodecs(codecs);
        io.saveLabeling(imgLabeling, file, clazz);
    }

    @Override
    public <T, I extends IntegerType<I>> void save(ImgLabeling<T, I> imgLabeling, String file, ToLongFunction<T> labelToId) {
        io.saveLabeling(imgLabeling, file, labelToId);
    }

    @Override
    public void initialize() {
        io = new LabelingIO(context, datasetIOService);
    }
}
