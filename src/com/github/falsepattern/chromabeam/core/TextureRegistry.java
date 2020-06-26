package com.github.falsepattern.chromabeam.core;

import com.badlogic.gdx.graphics.Pixmap;
import com.github.falsepattern.chromabeam.resource.ResourcePack;
import com.github.falsepattern.chromabeam.util.storage.UnsafeList;

public class TextureRegistry {

    private final UnsafeList<TextureTile> textures;
    private boolean finished = false;
    private final ResourcePack resourcePack;

    TextureRegistry(ResourcePack resourcePack) {
        textures = new UnsafeList<>();
        this.resourcePack = resourcePack;
    }

    TextureManager createManager() {
        var result = new TextureManager(textures);
        finished = true;
        resourcePack.dispose();
        return result;
    }

    private void loadTextureUnsafe(String textureName, Pixmap... frames) {
        for (int i = 0; i < frames.length; i++) {
            var tile = new TextureTile(frames[i], textureName, i, new int[]{0, 0, frames[i].getWidth(), frames[i].getHeight()});
            textures.add(tile);
        }
    }

    /**
     * Turns an image containing a single(or multiple) equally sized rectangles into separate textures and stores them
     * in the combined atlas. The <code>textureName</code> must be the same as the name of the component using the
     * texture, otherwise an error will be thrown during component registration.
     * @param textureName The name to register the texture as
     * @param packedTexture The image containing the texture
     * @param horizontalFrames How many frames are in the texture horizontally
     * @param verticalFrames How many frames are in the texture vertically
     */
    private void loadTexture(String textureName, Pixmap packedTexture, int horizontalFrames, int verticalFrames) {
        if (finished) throw new IllegalStateException("Tried to register texture after the registration phase has ended!");
        var chopped = new Pixmap[horizontalFrames * verticalFrames];
        int hSize = packedTexture.getWidth() / horizontalFrames;
        int vSize = packedTexture.getHeight() / verticalFrames;
        for (int y = 0; y < verticalFrames; y++) {
            for (int x = 0; x < horizontalFrames; x++) {

                var cell = new Pixmap(hSize, vSize, Pixmap.Format.RGBA8888);
                cell.drawPixmap(packedTexture, x * hSize, y * vSize, hSize, vSize, 0, 0, hSize, vSize);
                chopped[y * horizontalFrames + x] = cell;
            }
        }
        loadTextureUnsafe(textureName, chopped);
    }

    /**
     * Turns an image containing a single(or multiple) equally sized rectangles into separate textures and stores them
     * in the combined atlas. The <code>textureName</code> must be the same as the name of the component using the
     * texture, otherwise an error will be thrown during component registration.
     * @param textureName The name to register the texture as
     * @param texturePath The texture's locations inside the /assets/ folder.
     * @param horizontalFrames How many frames are in the texture horizontally
     * @param verticalFrames How many frames are in the texture vertically
     */
    public void loadTexture(String textureName, String texturePath, int horizontalFrames, int verticalFrames) {
        verify(textureName);
        var data = resourcePack.getAssetBytes(texturePath);
        var pixmap = new Pixmap(data, 0, data.length);
        loadTexture(textureName, pixmap, horizontalFrames, verticalFrames);
    }

    private void verify(String name) {
        for (var texture: textures) {
            if (texture.textureName.equals(name)) throw new IllegalArgumentException("Texture with name \"" + name + "\" already registered!");
        }
    }
}
