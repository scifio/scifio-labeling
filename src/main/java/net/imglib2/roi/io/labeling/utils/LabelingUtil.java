package net.imglib2.roi.io.labeling.utils;

import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgIOException;
import io.scif.img.ImgSaver;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgView;
import net.imglib2.type.numeric.RealType;
import org.bson.io.BasicOutputBuffer;
import org.scijava.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class LabelingUtil {


    public static final String BSON_ENDING = ".bson";
    public static final String TIF_ENDING = ".tif";

    /**
     * @param filename  the filename of the Img to save
     * @param rai   the img
     * @param <T>   the pixel value
     */
    public static <T extends RealType<T>> void saveAsTiff(final Context context,
            final String filename,
            final RandomAccessibleInterval<T> rai) {

        try {
            new ImgSaver(context).saveImg(filename, ImgView.wrap(rai, null), new SCIFIOConfig().writerSetFailIfOverwriting(false));
        } catch (ImgIOException | IncompatibleTypeException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(BasicOutputBuffer outputBuffer, File file) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            outputBuffer.pipe(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFilePathWithExtension(final String filename, final String extension, String path) {
        path = (path == null) ? "" : path;
        String actualFilename = Paths.get(filename).getFileName().toString();
        if (actualFilename.endsWith(extension)) {
            return Paths.get(path, actualFilename).toString();
        }
        final int index = actualFilename.lastIndexOf(".");
        return Paths.get(path, actualFilename.substring(0, index == -1 ? actualFilename.length() : index).concat(extension)).toString();
    }
}
