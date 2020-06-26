package com.github.falsepattern.chromabeam.circuit;

import com.github.falsepattern.chromabeam.mod.BeamCollision;
import com.github.falsepattern.chromabeam.mod.Component;
import com.github.falsepattern.chromabeam.mod.interfaces.MaskedWorld;
import com.github.falsepattern.chromabeam.util.serialization.Deserializer;
import com.github.falsepattern.chromabeam.util.serialization.Serializer;

public class CircuitIOPort extends CircuitSlave {
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
    protected void serializeCustomData(Serializer output) {
        output.writeInt(linkID);
    }

    @Override
    protected void deserializeCustomData(Deserializer input) {
        linkID = input.readInt();
    }
}
