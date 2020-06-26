package com.github.falsepattern.chromabeam.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.falsepattern.chromabeam.mod.interfaces.Renderable;
import com.github.falsepattern.chromabeam.util.Vector2i;

class CameraMover implements Renderable {
    private final Vector2i mousePrev = new Vector2i();
    @Override
    public void prepare(SpriteBatch batch, OrthographicCamera camera) {
        int x = Gdx.input.getX();
        int y = Gdx.input.getY();
        if (!(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) && Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            camera.position.x -= (x - mousePrev.x) / (float)Gdx.graphics.getWidth() * camera.viewportWidth;
            camera.position.y += (y - mousePrev.y) / (float)Gdx.graphics.getHeight() * camera.viewportHeight;
            camera.update();
            batch.setProjectionMatrix(camera.combined);
        }
        mousePrev.x = x;
        mousePrev.y = y;

    }

    @Override
    public void render(SpriteBatch batch, OrthographicCamera camera) {
    }

    @Override
    public void finish(SpriteBatch batch, OrthographicCamera camera) {
    }

    @Override
    public void resize(int width, int height) {

    }
}
