package com.github.falsepattern.chromabeam.mod.interfaces;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A wrapper interface for the sprite atlas, only allowing reading of textures.
 */
public interface ReadableSpriteAtlas {
    TextureRegion getTexture(String name);
    TextureRegion[] getMultiTexture(String name);
    TextureRegion getTexture(String name, int index);
}
