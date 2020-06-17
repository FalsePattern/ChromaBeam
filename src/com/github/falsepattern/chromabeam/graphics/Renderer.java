package com.github.falsepattern.chromabeam.graphics;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.falsepattern.chromabeam.mod.interfaces.RendererRegistry;
import com.github.falsepattern.chromabeam.mod.interfaces.Renderable;
import com.github.falsepattern.chromabeam.util.storage.UnsafeList;

import java.util.List;

public class Renderer implements RendererRegistry {
    protected SpriteBatch batch;
    protected OrthographicCamera camera;
    private List<Renderable> renderables;

    public Renderer(int size) {
        batch = new SpriteBatch(size);
        camera = new OrthographicCamera();
        renderables = new UnsafeList<>();
    }

    public void addRenderable(Renderable renderable) {
        renderables.add(renderable);
    }

    public void draw() {
        for (var renderable: renderables) renderable.prepare(batch, camera);
        batch.begin();
        for (var renderable: renderables) {
            batch.setColor(1, 1, 1, 1);
            renderable.render(batch, camera);
        }
        batch.end();
        for (var renderable: renderables) renderable.finish(batch, camera);
    }

    public void resize(int width, int height) {
        for (var renderable: renderables) renderable.resize(width, height);
    }

    public void dispose() {
        batch.dispose();
    }
}
