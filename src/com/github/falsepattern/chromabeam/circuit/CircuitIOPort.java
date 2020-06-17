package com.github.falsepattern.chromabeam.circuit;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.falsepattern.chromabeam.mod.BeamCollision;
import com.github.falsepattern.chromabeam.mod.Component;
import com.github.falsepattern.chromabeam.mod.interfaces.MaskedWorld;

public class CircuitIOPort extends NoInteract {
    int linkID = 0;
    public CircuitIOPort() {
        super(1, "circuit.input");
    }


    @Override
    public BeamCollision processIncomingBeam(int beamHeading, int color, MaskedWorld world) {
        if (beamHeading == forward) {
            master.passBeamToChild(color, linkID);
        }
        return BeamCollision.CENTER;
    }

    @Override
    protected void cloneDataFromOriginal(Component original) {
        this.linkID = ((CircuitIOPort)original).linkID;
    }

    @Override
    protected void serializeCustomData(Kryo kryo, Output output) {
        output.writeInt(linkID);
    }

    @Override
    protected void deserializeCustomData(Kryo kryo, Input input) {
        linkID = input.readInt();
    }
}
