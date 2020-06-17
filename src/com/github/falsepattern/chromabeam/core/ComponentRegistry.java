package com.github.falsepattern.chromabeam.core;

import com.github.falsepattern.chromabeam.mod.Component;
import com.github.falsepattern.chromabeam.mod.Mod;
import com.github.falsepattern.chromabeam.util.storage.UnsafeList;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class ComponentRegistry {
    private final List<Component> prefabs;
    private final Map<Mod, List<Component>> modMapping;
    private final Map<Mod, Map<String, Component>> modAndNameMapping;
    private final Map<String, Component> nameMapping;
    private boolean ended = false;
    ComponentRegistry() {
        prefabs = new UnsafeList<>();
        modMapping = new HashMap<>();
        modAndNameMapping = new HashMap<>();
        nameMapping = new HashMap<>();
    }

    private Mod regMod;

    void setLoadingMod(Mod mod) {
        this.regMod = mod;
    }

    public void registerComponent(Class<? extends Component> componentClass) {
        if (ended) throw new IllegalStateException("Cannot register component after the registration phase has ended!");
        var list = modMapping.computeIfAbsent(regMod, (ignored) -> new UnsafeList<>());
        var nameMap = modAndNameMapping.computeIfAbsent(regMod, (ignored) -> new HashMap<>());
        Component component;
        try {
            component = componentClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Error while loading mod " + regMod.getModid(), e);
        }
        prefabs.add(component);
        list.add(component);
        var regName = component.getRegistryName();
        nameMap.put(regName, component);
        nameMapping.put(regName, component);
    }

    public void registerComponents(Class<? extends Component>... componentClasses) {
        for (var componentClass : componentClasses) {
            registerComponent(componentClass);
        }
    }

    void finish() {
        ended = true;
    }

    public Component getPrefab(Mod mod, String name) {
        return modAndNameMapping.get(mod).get(name);
    }

    public List<Component> getPrefabs(Mod mod) {
        return modMapping.getOrDefault(mod, Collections.emptyList());
    }

    public Component getPrefab(String name) {
        return nameMapping.get(name);
    }

    public List<Component> getAllPrefabs() {
        return prefabs;
    }
}
