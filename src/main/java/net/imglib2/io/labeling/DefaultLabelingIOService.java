package net.imglib2.io.labeling;

import io.scif.services.DatasetIOService;
import net.imagej.ImageJService;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
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
    public <T, I extends IntegerType<I>> ImgLabeling<T, I> open(String file, Class clazz) {
        try {
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
    public <T, I extends IntegerType<I>> void save(ImgLabeling<T, I> imgLabeling, String file, Class clazz) {
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
