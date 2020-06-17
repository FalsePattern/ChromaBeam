package com.github.falsepattern.chromabeam.graphics;

public class ScreenSpaceRenderer extends Renderer{
    public ScreenSpaceRenderer() {
        super(1000);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(true, width, height);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        super.resize(width, height);
    }
}
