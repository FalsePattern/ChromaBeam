package com.github.falsepattern.chromabeam.mod.interfaces;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Renderable {

    /**
     * Called before the call to the specified {@link SpriteBatch}'s @link begin() method.
     */
    void prepare(SpriteBatch batch, OrthographicCamera camera);

    /**
     * Called after the call to the specified {@link SpriteBatch}'s @link begin() method and before the end() method.
     */
    void render(SpriteBatch batch, OrthographicCamera camera);

    /**
     * Called after the call to the specified {@link SpriteBatch}'s @link end() method.
     */
    void finish(SpriteBatch batch, OrthographicCamera camera);

    /**
     * Called when the window is resized.
     */
    void resize(int width, int height);
}
