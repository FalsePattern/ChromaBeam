package com.github.falsepattern.chromabeam.util.storage;

import com.github.falsepattern.chromabeam.util.ImmutablePair;
import com.github.falsepattern.chromabeam.util.MutablePair;
import com.github.falsepattern.chromabeam.util.Pair;
import com.github.falsepattern.chromabeam.util.storage.nongeneric.NativeContainer2DNode;
import com.github.falsepattern.chromabeam.util.storage.quadrant.UnsafeNativeContainer2D;

import java.util.List;

public class NodeGraphContainer2D<T> extends UnsafeNativeContainer2D<T> {
    private final NativeContainer2DNode nodeGraph = new NativeContainer2DNode();
    private final List<int[]> massAddPositions = new UnsafeList<>();
    private final boolean unsafeOptimizations;

    public NodeGraphContainer2D() {
        this(false);
    }

    public NodeGraphContainer2D(boolean unsafeOptimizations) {
        this.unsafeOptimizations = unsafeOptimizations;
    }

    @Override
    public T set(int x, int y, T data) {
        var original = super.set(x, y, data);
        if (original == null && data != null) {
            addNode(x, y);
        } else if (original != null && data == null) {
            removeNode(x, y);
        }
        return original;
    }

    private void removeNode(int x, int y) {
        var node = nodeGraph.remove(x, y);
        var nodes = new int[4][4][3];
        for (int i = 0; i < 4; i++) {
            var nb = node[i];
            if (nb[2] == 1) {
                nodes[i] = nodeGraph.get(nb[0], nb[1]);
            }
        }
        for (int i = 0; i < 2; i++) {
            int l = i + 2;
            if (nodes[i] != null && nodes[l] != null) {
                nodes[i][l] = node[l];
                nodes[l][i] = node[i];
            } else if (nodes[i] == null && nodes[l] != null) {
                nodes[l][i][2] = 0;
            } else if (nodes[i] != null) {
                nodes[i][l][2] = 0;
            }
        }
    }
    private boolean massAddMode = false;
    public boolean toggleMassAddMode() {
        massAddMode = !massAddMode;
        if (!massAddMode) {
            for (var position: massAddPositions) {
                addNode(position[0], position[1]);
            }
        }
        return massAddMode;
    }

    private void addNode(int x, int y) {
        if (massAddMode) {
            massAddPositions.add(new int[]{x, y});
        }
        var node = new int[4][3];
        nodeGraph.set(x, y, node);
        for (int i = 0; i < 4; i++) {
            var exist = super.getNextExisting(x, y, i);
            if (exist != null) {
                var pos = exist.getA();
                var neighbor = nodeGraph.get(pos[0], pos[1]);
                node[i][2] = 1;
                System.arraycopy(pos, 0, node[i], 0, 2);
                var arr = neighbor[(i + 2) % 4];
                arr[0] = x;
                arr[1] = y;
                arr[2] = 1;
            }
        }
    }

    @Override
    public T remove(int x, int y) {
        var result = super.remove(x, y);
        if (result != null) {
            removeNode(x, y);
        }
        return result;
    }

    private final MutablePair<int[], T> nextAxisGetBuffer = new MutablePair<>(null, null);
    @Override
    public Pair<int[], T> getNextExisting(int x, int y, int rotation) {
        var node = nodeGraph.get(x, y);
        if (node == null) {
            return super.getNextExisting(x, y, rotation);
        } else {
            var nb = node[rotation];
            if (nb[2] == 1) {
                if (unsafeOptimizations) {
                    nextAxisGetBuffer.a = nb;
                    nextAxisGetBuffer.b = super.get(nb[0], nb[1]);
                    return nextAxisGetBuffer;
                } else {
                    int[] output = new int[2];
                    System.arraycopy(nb, 0, output, 0, 2);
                    return new ImmutablePair<>(output, super.get(nb[0], nb[1]));
                }
            } else {
                return null;
            }
        }
    }

    @Override
    public void clear() {
        nodeGraph.clear();
        super.clear();
        massAddPositions.clear();
        massAddMode = false;
    }
}
