package com.github.falsepattern.chromabeam.resource;

import java.io.InputStream;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

public class ResourcePackStack implements ResourcePack{
    private final Deque<ResourcePack> resourcePacks;
    public ResourcePackStack() {
        resourcePacks = new LinkedBlockingDeque<>();
    }

    public void addResourcePack(ResourcePack resourcePack) {
        if (!containsNested(resourcePack)) {
            resourcePacks.push(resourcePack);
        }
    }

    private boolean containsNested(ResourcePack resourcePack) {
        if (resourcePack.equals(this)) return true;
        return resourcePacks.stream().anyMatch((pack) -> {
            if (pack instanceof ResourcePackStack) {
                return ((ResourcePackStack) pack).containsNested(resourcePack);
            }
            return pack.equals(resourcePack);
        });
    }

    @Override
    public InputStream getAssetStream(String assetPath) {
        InputStream result = null;
        for (var resourcePack: resourcePacks) {
            if ((result = resourcePack.getAssetStream(assetPath)) != null) {
                break;
            }
        }
        return result;
    }

    @Override
    public void dispose() {
        for (var pack: resourcePacks) {
            pack.dispose();
        }
        resourcePacks.clear();
    }
}
