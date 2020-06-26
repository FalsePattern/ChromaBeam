package com.github.falsepattern.chromabeam.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.github.falsepattern.chromabeam.graphics.DrawingHelpers;
import com.github.falsepattern.chromabeam.mod.Component;

@SuppressWarnings("MismatchedReadAndWriteOfArray")
public class ComponentDrawHelper {
    private int[][][] intsBackBuffer;
    private boolean[][] flippedBackBuffer;
    private TextureRegion[][] textureBackBuffer;

    private int[][][] intsFrontBuffer;
    private boolean[][] flippedFrontBuffer;
    private TextureRegion[][] textureFrontBuffer;

    private final int[] backBufferSize = new int[8];
    private final int[] frontBufferSize = new int[8];
    private static final int[] EMPTY = new int[8];

    private volatile boolean hasNext = false;

    public ComponentDrawHelper() {
        intsBackBuffer = new int[8][256][3];
        flippedBackBuffer = new boolean[8][256];
        textureBackBuffer = new TextureRegion[8][256];

        intsFrontBuffer = new int[8][256][3];
        flippedFrontBuffer = new boolean[8][256];
        textureFrontBuffer = new TextureRegion[8][256];
    }

    private void extend(int i) {
        int prevLength = intsBackBuffer[i].length;
        int newLength = prevLength * 2;
        var prevInts = intsBackBuffer[i];
        var prevFlip = flippedBackBuffer[i];
        var prevTex = textureBackBuffer[i];
        intsBackBuffer[i] = new int[prevLength * 2][];
        flippedBackBuffer[i] = new boolean[prevLength * 2];
        textureBackBuffer[i] = new TextureRegion[prevLength * 2];
        System.arraycopy(prevInts, 0, intsBackBuffer[i], 0, prevLength);
        System.arraycopy(prevFlip, 0, flippedBackBuffer[i], 0, prevLength);
        System.arraycopy(prevTex, 0, textureBackBuffer[i], 0, prevLength);
        for (int j = prevLength; j < newLength; j++) {
            intsBackBuffer[i][j] = new int[3];
        }
    }

    public synchronized void clearBackBuffer() {
        hasNext = false;
        System.arraycopy(EMPTY, 0, backBufferSize, 0, 8);
    }

    public void addToBackBuffer(Component component) {
        int x, y;
        x = component.getX();
        y = component.getY();
        int color = component.getDrawColor();
        if (backBufferSize[color] >= textureBackBuffer[color].length) extend(color);
        var ints = intsBackBuffer[color][backBufferSize[color]];
        ints[0] = x;
        ints[1] = y;
        ints[2] = component.getRotation();
        flippedBackBuffer[color][backBufferSize[color]] = component.getFlipped();
        textureBackBuffer[color][backBufferSize[color]++] = component.getTexture();
    }

    public synchronized void finishBackBuffer() {
        hasNext = true;
    }

    private final Vector3 compCamProjBuf = new Vector3();
    public void draw(SpriteBatch batch, OrthographicCamera camera) {
        swapBuffers();
        compCamProjBuf.set(0, 0, 0);
        camera.unproject(compCamProjBuf);
        int left = (int) compCamProjBuf.x - 1;
        int up = (int) compCamProjBuf.y;
        compCamProjBuf.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0);
        camera.unproject(compCamProjBuf);
        int right = (int) compCamProjBuf.x;
        int down = (int) compCamProjBuf.y - 1;
        for (int i = 0; i < 8; i++) {
            DrawingHelpers.setColor(batch, i, 1f);
            for (int j = 0; j < frontBufferSize[i]; j++) {
                var ints = intsFrontBuffer[i][j];
                if (ints[0] < left || ints[0] > right || ints[1] < down || ints[1] > up) continue;
                DrawingHelpers.drawComponentTexture(batch, textureFrontBuffer[i][j], ints[0], ints[1], ints[2], flippedFrontBuffer[i][j]);
            }
        }
    }

    private synchronized void swapBuffers() {
        if (hasNext) {
            var tmpInts = intsFrontBuffer;
            var tmpFlipped = flippedFrontBuffer;
            var tmpTexture = textureFrontBuffer;
            intsFrontBuffer = intsBackBuffer;
            flippedFrontBuffer = flippedBackBuffer;
            textureFrontBuffer = textureBackBuffer;
            intsBackBuffer = tmpInts;
            flippedBackBuffer = tmpFlipped;
            textureBackBuffer = tmpTexture;
            System.arraycopy(backBufferSize, 0, frontBufferSize, 0, 8);
            hasNext = false;
        }
    }
}
