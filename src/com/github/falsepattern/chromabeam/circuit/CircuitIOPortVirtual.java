package com.github.falsepattern.chromabeam.circuit;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.falsepattern.chromabeam.mod.BasicComponent;
import com.github.falsepattern.chromabeam.mod.BeamCollision;
import com.github.falsepattern.chromabeam.mod.Component;
import com.github.falsepattern.chromabeam.mod.interfaces.MaskedWorld;

public class CircuitIOPortVirtual extends BasicComponent {
    int linkID = 0;
    CircuitMaster master;
    public CircuitIOPortVirtual() {
        super(1, "circuit.input");
    }

    @Override
    public BeamCollision processIncomingBeam(int beamHeading, int color, MaskedWorld world) {
        if (master != null && beamHeading == forward) {
            master.passBeamToParent(color, linkID);
        }
        return BeamCollision.CENTER;
    }

    @Override
    protected void cloneDataFromOriginal(Component original) {
        this.linkID = ((CircuitIOPortVirtual)original).linkID;
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
