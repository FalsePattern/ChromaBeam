package com.github.falsepattern.chromabeam.graphics;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.falsepattern.chromabeam.mod.interfaces.Renderable;
import com.github.falsepattern.chromabeam.mod.interfaces.World;

public class WorldRenderable implements Renderable {

    private World world;

    @Override
    public void prepare(SpriteBatch batch, OrthographicCamera camera) {}

    @Override
    public void render(SpriteBatch batch, OrthographicCamera camera) {
        world.drawBeams(batch, camera);
        world.drawComponents(batch, camera);
    }

    @Override
    public void finish(SpriteBatch batch, OrthographicCamera camera) {}
    @Override
    public void resize(int width, int height) {}

    public void setWorld(World world) {
        this.world = world;
    }
}