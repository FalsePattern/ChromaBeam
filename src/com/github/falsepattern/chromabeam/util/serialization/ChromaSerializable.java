package com.github.falsepattern.chromabeam.util.serialization;

import java.io.OutputStream;

public interface ChromaSerializable {

    void write(Serializer output);
    void read(Deserializer input);
}
