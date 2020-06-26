package com.github.falsepattern.chromabeam.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.github.falsepattern.chromabeam.core.GlobalData;

public class WorldSpaceRenderer extends Renderer implements InputProcessor {

    private float zoom = 10;
    private float deltaZoom = 0;
    private final float deltaTransferSpeed = 0.1f;

    public WorldSpaceRenderer() {
        super(8191);
    }

    public void resize(int width, int height) {
        camera.viewportWidth = (float)(width / GlobalData.SPRITE_SIZE * Math.pow(zoom / 10d, 2));
        camera.viewportHeight = (float)(height / GlobalData.SPRITE_SIZE * Math.pow(zoom / 10d, 2));
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        super.resize(width, height);
    }

    private void transferDelta() {
        float transferred = deltaZoom / 5f;
        zoom += transferred;
        deltaZoom -= transferred;
        if (zoom < 4) zoom = 4;
    }

    @Override
    public void draw() {
        transferDelta();
        //if (zoom > 1000) zoom = 1000;
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        super.draw();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
    @Override
    public boolean scrolled(int amount) {
        deltaZoom += amount;
        return true;
    }
}
