package com.github.falsepattern.chromabeam.world;

public class BeamDataContainer {
    private int[][] rearStorage;
    private int[][] frontStorage;
    private int rearBeamCount = 0;
    private int frontBeamCount = 0;
    boolean newData = false;
    public BeamDataContainer() {
        rearStorage = new int[256][4];
        frontStorage = new int[256][4];
    }

    public void pushBeam(int x, int y, int rotation, int color) {
        newData = true;
        if (rearBeamCount >= rearStorage.length) {
            extend();
        }
        var arr = rearStorage[rearBeamCount++];
        arr[0] = x;
        arr[1] = y;
        arr[2] = rotation;
        arr[3] = color;
    }

    public int[] popBeam() {
        return frontStorage[--frontBeamCount];
    }

    public int getBeamCount() {
        return frontBeamCount;
    }

    public void flip() {
        frontBeamCount = rearBeamCount;
        rearBeamCount = 0;
        var tmp = frontStorage;
        frontStorage = rearStorage;
        rearStorage = tmp;
        newData = false;
    }

    public void clear() {
        rearBeamCount = 0;
    }

    private void extend() {
        int prevLen = rearStorage.length;
        var prevStorage = rearStorage;
        rearStorage = new int[prevLen * 2][];
        System.arraycopy(prevStorage, 0, rearStorage, 0, prevLen);
        for (int i = prevLen; i < rearStorage.length; i++) {
            rearStorage[i] = new int[4];
        }
    }
}
