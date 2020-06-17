package com.github.falsepattern.chromabeam.core;

import com.esotericsoftware.kryo.Kryo;

/**
 * Stuff that should only be accessed by the core mod. If you need to use stuff from here, you're doing it wrong.
 */
class CoreData {
    static Kryo kryo;
}
