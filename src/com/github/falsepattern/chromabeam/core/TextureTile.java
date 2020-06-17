package com.github.falsepattern.chromabeam.core;

import com.badlogic.gdx.graphics.Pixmap;

import java.util.Arrays;
import java.util.Objects;

class TextureTile implements Comparable<TextureTile>{

    TextureTile(Pixmap texture, String textureName, int textureFrame, int[] textureGeometry) {
        this.texture = texture;
        this.textureName = textureName;
        this.textureFrame = textureFrame;
        this.textureGeometry = textureGeometry;
    }

    final Pixmap texture;
    final String textureName;
    final int textureFrame;
    final int[] textureGeometry;
    @Override
    public int compareTo(TextureTile o) {
        if (textureGeometry[3] == o.textureGeometry[3]) {
            return textureGeometry[2] - o.textureGeometry[2];
        } else {
            return textureGeometry[3] - o.textureGeometry[3];
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TextureTile)) return false;
        var tex = (TextureTile)obj;
        return Objects.equals(texture, tex.texture) && Objects.equals(textureName, tex.textureName)
                && Objects.equals(textureFrame, tex.textureFrame) && Arrays.equals(textureGeometry, tex.textureGeometry);
    }
}
