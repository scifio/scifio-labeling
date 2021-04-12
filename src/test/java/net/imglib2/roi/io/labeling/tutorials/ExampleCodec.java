package net.imglib2.roi.io.labeling.tutorials;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

class ExampleCodec implements Codec<Example> {

    @Override
    public Example decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        String a = reader.readString("a");
        double b = reader.readDouble("b");
        int c = reader.readInt32("c");
        reader.readEndDocument();
        return new Example(a, b, c);
    }

    @Override
    public void encode(BsonWriter writer, Example value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString("a", value.a);
        writer.writeDouble("b", value.b);
        writer.writeInt32("c", value.c);
        writer.writeEndDocument();
    }

    @Override
    public Class<Example> getEncoderClass() {
        return Example.class;
    }
}
