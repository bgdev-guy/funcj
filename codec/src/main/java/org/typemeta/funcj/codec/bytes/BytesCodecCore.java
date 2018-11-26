package org.typemeta.funcj.codec.bytes;

import org.typemeta.funcj.codec.*;
import org.typemeta.funcj.codec.bytes.BytesCodec.*;

import java.io.*;

/**
 * Interface for classes which implement an encoding via byte streams.
 */
public class BytesCodecCore
        extends CodecCoreDelegate<Input, Output, Config>
        implements CodecAPI {

    public BytesCodecCore(BytesCodecFormat format) {
        super(new CodecCoreImpl<>(format));
    }

    public BytesCodecCore(Config config) {
        this(new BytesCodecFormat(config));
    }

    public BytesCodecCore() {
        this(new BytesConfigImpl());
    }

    @Override
    public <T> void encode(Class<? super T> clazz, T value, Writer wtr) {
        throw new CodecException("Not supported");
    }

    @Override
    public <T> T decode(Class<? super T> clazz, Reader rdr) {
        throw new CodecException("Not supported");
    }

    /**
     * Encode the given value into byte data and write the results to the {@link OutputStream} object.
     * The static type determines whether type information is written to recover the value's
     * dynamic type.
     * @param type      the static type of the value
     * @param value     the value to be encoded
     * @param os        the output stream to which the byte data is written
     * @param <T>       the static type of the value
     */
    public <T> void encode(Class<? super T> type, T value, OutputStream os) {
        encode(type, value, BytesCodec.outputOf(os));
    }

    /**
     * Decode a value by reading byte data from the given {@link InputStream} object.
     * @param type      the static type of the value to be decoded.
     * @param is        the input stream from which byte data is read
     * @param <T>       the static type of the value
     * @return          the decoded value
     */
    @Override
    public <T> T decode(Class<? super T> type, InputStream is) {
        return decode(type, BytesCodec.inputOf(is));
    }
}
