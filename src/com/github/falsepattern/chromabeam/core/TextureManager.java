package com.github.falsepattern.chromabeam.core;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.github.falsepattern.chromabeam.mod.interfaces.ReadableSpriteAtlas;
import com.github.falsepattern.chromabeam.util.storage.UnsafeList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class TextureManager implements ReadableSpriteAtlas, Disposable {
    public final com.badlogic.gdx.graphics.Texture texture;
    private final List<String> textureNames;
    private final IntMap<TextureRegion[]> textures;

    public TextureManager(List<TextureTile> textures) {
        this.textureNames = new UnsafeList<>();
        textures.sort(Comparator.reverseOrder());
        var finalSize = pack(textures);
        var result = new Pixmap(finalSize[0], finalSize[1], Pixmap.Format.RGBA8888);
        result.setBlending(Pixmap.Blending.None);
        textures.forEach((textureTile -> {
            var geom = textureTile.textureGeometry;
            result.drawPixmap(textureTile.texture, geom[0], geom[1]);
        }));
        texture = new Texture(result, true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        this.textures = new IntMap<>();
        var allFrames = new HashMap<String, IntMap<TextureRegion>>();
        for (var texture: textures) {
            var frameMap = allFrames.computeIfAbsent(texture.textureName, (ignored) -> new IntMap<>());
            var geom = texture.textureGeometry;
            frameMap.put(texture.textureFrame, new TextureRegion(this.texture, geom[0], geom[1], geom[2], geom[3]));
        }
        int i = 0;
        for (var frame: allFrames.entrySet()) {
            textureNames.add(frame.getKey());
            var optionMax = Arrays.stream(frame.getValue().keys().toArray().items).max();
            int arrSize = 0;
            if (optionMax.isPresent()) {
                arrSize = optionMax.getAsInt() + 1;
            }
            var frames = new TextureRegion[arrSize];
            for (int j = 0; j < arrSize; j++) {
                frames[j] = frame.getValue().get(j);
            }
            this.textures.put(i++, frames);
        }
    }

    private int[] pack(List<TextureTile> tiles) {
        boolean notFound = true;
        boolean widthDouble = true;
        int gap = GlobalData.TEXTURE_GAP;
        int width = 64;
        int height = 64;
        while (notFound) {
            if (widthDouble) width *= 2; else height *= 2; widthDouble = !widthDouble;
            var partitions = new LinkedBlockingDeque<int[]>();
            partitions.push(new int[]{0, 0, width, height});
            outer:
            for (var tile: tiles) {
                while (partitions.size() > 0) {
                    var partition = partitions.pop();
                    var geom = tile.textureGeometry;
                    if (geom[2] + gap <= partition[2] && geom[3] + gap <= partition[3]) {
                        geom[0] = partition[0];
                        geom[1] = partition[1];
                        int right = geom[0] + geom[2] + gap;
                        int bottom = geom[1] + geom[3] + gap;
                        partitions.push(new int[]{geom[0], bottom, partition[2], partition[3] - geom[3] - gap});
                        partitions.push(new int[]{right, geom[1], partition[2] - geom[2] - gap, geom[3] + gap});
                        continue outer;
                    }
                }
                break;
            }
            notFound = partitions.size() == 0;
        }
        return new int[]{width, height};
    }

    public TextureRegion getTexture(String name) {

        return getTexture(name, 0);
    }

    public TextureRegion[] getMultiTexture(String name) {
        return textures.get(getTextureID(name));
    }

    public TextureRegion getTexture(String name, int index) {
        return getMultiTexture(name)[index];
    }

    private int getTextureID(String name) {
        int id = textureNames.indexOf(name);
        if (id == -1) {
            throw new IllegalArgumentException("Texture \"" + name + "\" is not registered!");
        }
        return id;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
