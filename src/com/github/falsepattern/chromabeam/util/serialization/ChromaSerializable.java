package com.github.falsepattern.chromabeam.util.serialization;

public interface ChromaSerializable {

    void write(Serializer output);
    void read(Deserializer input);
}
