package com.github.falsepattern.chromabeam.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

import static com.badlogic.gdx.graphics.GL20.*;

public class BeamDrawHelper {
    private static Texture beamTex;

    public static void init() {
        beamTex = new Texture(Gdx.files.internal("assets/core/textures/beam_new.png"), true);
        beamTex.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
    }

    private int[][] backBuffer;
    private int[][] frontBuffer;
    private int backBufferSize = 0;
    private int frontBufferSize = 0;

    private volatile boolean hasNext = false;

    public BeamDrawHelper() {
        backBuffer = new int[256][6];
        frontBuffer = new int[256][6];
    }

    private void extend() {
        int oldLength = backBuffer.length;
        var oldBuffer = backBuffer;
        backBuffer = new int[oldLength * 2][];
        System.arraycopy(oldBuffer, 0, backBuffer, 0, oldLength);
        for (int i = oldLength; i < backBuffer.length; i++) {
            backBuffer[i] = new int[6];
        }
    }

    public synchronized void clearBackBuffer() {
        hasNext = false;
        backBufferSize = 0;
    }

    public void addToBackBuffer(int x, int y, int rotation, int length, int color, int collision) {
        if (rotation % 2 == 1) {
            if (x < bounds[0] || x > bounds[1]) return;
            //if (y < bounds[2] && length != Integer.MAX_VALUE && y + length < bounds[2]) return;
            //if (y > bounds[3] && length != Integer.MIN_VALUE && y + length > bounds[3]) return;
        } else {
            if (y < bounds[2] || y > bounds[3]) return;
            //if (x < bounds[0] && length != Integer.MAX_VALUE && x + length < bounds[0]) return;
            //if (x > bounds[1] && length != Integer.MIN_VALUE && x + length > bounds[1]) return;
        }
        if (backBufferSize >= backBuffer.length) extend();
        var cell = backBuffer[backBufferSize++];
        cell[0] = x;
        cell[1] = y;
        cell[2] = rotation;
        cell[3] = length;
        cell[4] = color;
        cell[5] = collision;
    }

    public synchronized void finishBackBuffer() {
        hasNext = true;
    }

    public synchronized void swapBuffers() {
        if (hasNext) {
            var tmp = frontBuffer;
            frontBuffer = backBuffer;
            backBuffer = tmp;
            frontBufferSize = backBufferSize;
            hasNext = false;
        }
    }

    private final Vector3 beamCameraProjectionBuffer = new Vector3();
    private final int[] bounds = new int[4];
    public void draw(SpriteBatch batch, OrthographicCamera camera) {
        swapBuffers();
        var dst = batch.getBlendDstFunc();
        var src = batch.getBlendSrcFunc();
        batch.setBlendFunction(GL_SRC_ALPHA, GL_ONE);
        beamCameraProjectionBuffer.set(0, 0, 0);
        camera.unproject(beamCameraProjectionBuffer);
        float left;
        bounds[0] = (int) (left = beamCameraProjectionBuffer.x - 1);
        bounds[3] = (int) (beamCameraProjectionBuffer.y);
        beamCameraProjectionBuffer.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0);
        camera.unproject(beamCameraProjectionBuffer);
        bounds[1] = (int) (beamCameraProjectionBuffer.x);
        bounds[2] = (int) (beamCameraProjectionBuffer.y - 1);
        beamCameraProjectionBuffer.set(1, 0, 0);
        var pixelSize = Math.pow(camera.unproject(beamCameraProjectionBuffer).x - left, 2);
        var thickness = Math.max(1, pixelSize);
        for (int i = 0; i < frontBufferSize; i++) {
            var beam = frontBuffer[i];
            float x = beam[0];
            float y = beam[1];
            float rotation = beam[2];
            float length = beam[3];
            final int color = beam[4];
            /*if (w == 0) {
                w = 1;
                rot = 90;
                if (h < 0)
                    h = Math.max(h + beam[5] * 0.5f, down - y);
                else
                    h = Math.min(h - beam[5] * 0.5f, up - y);
            } else if (h == 0) {
                h = 1;
                if (w > 0)
                    w = Math.min(w - beam[5] * 0.5f, right - x);
                else
                    w = Math.max(w + beam[5] * 0.5f, left - x);
            }*/
            batch.setColor(color & 1, color & 2, color & 4, 1);
            batch.draw(beamTex, x + 0.5f, y, 0f, 0.5f, length - (beam[5] / 2f), 1, 1, (float)thickness, rotation * -90, 0, 0, 32, 32, false, false);
        }
        batch.setBlendFunction(src, dst);
    }
}
