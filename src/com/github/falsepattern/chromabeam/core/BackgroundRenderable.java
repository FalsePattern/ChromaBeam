package com.github.falsepattern.chromabeam.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.github.falsepattern.chromabeam.mod.interfaces.Renderable;

class BackgroundRenderable implements Renderable, Disposable {
    private final Texture bg;
    BackgroundRenderable() {
        bg = new Texture(Gdx.files.internal("assets/core/textures/bg.png"), true);
        bg.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        bg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    }
    @Override
    public void prepare(SpriteBatch batch, OrthographicCamera camera) {

    }

    @Override
    public void render(SpriteBatch batch, OrthographicCamera camera) {
        var pos = camera.position;
        float w = camera.viewportWidth;
        float h = camera.viewportHeight;
        int left = (int)Math.floor(pos.x - w / 2);
        int right = (int)Math.ceil(pos.x + w / 2);
        int bottom = (int)Math.floor(pos.y - h / 2);
        int top = (int)Math.ceil(pos.y + h / 2);
        batch.draw(bg, left, bottom, right - left, top - bottom, 0, 0, right - left, top - bottom);
    }

    @Override
    public void finish(SpriteBatch batch, OrthographicCamera camera) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void dispose() {
        bg.dispose();
    }
}
