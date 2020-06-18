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
    private final Map<String, Map<String, Component>> categoryAndNameMapping;
    private final Map<String, List<Component>> categoryMapping;
    private final List<String> categories;
    private boolean ended = false;
    ComponentRegistry() {
        prefabs = new UnsafeList<>();
        modMapping = new HashMap<>();
        modAndNameMapping = new HashMap<>();
        nameMapping = new HashMap<>();
        categoryAndNameMapping = new HashMap<>();
        categories = new UnsafeList<>();
        categories.add("interact");
        categoryMapping = new HashMap<>();
    }

    private Mod regMod;

    void setLoadingMod(Mod mod) {
        this.regMod = mod;
    }

    public void registerComponent(Class<? extends Component> componentClass) {
        if (ended) throw new IllegalStateException("Cannot register component after the registration phase has ended!");
        var modMap = modMapping.computeIfAbsent(regMod, (ignored) -> new UnsafeList<>());
        var modNameMap = modAndNameMapping.computeIfAbsent(regMod, (ignored) -> new HashMap<>());
        Component component;
        try {
            component = componentClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Error while loading mod " + regMod.getModid(), e);
        }
        var category = component.getCategory();
        var categoryNameMap = categoryAndNameMapping.computeIfAbsent(category, (ignored) -> new HashMap<>());
        var categoryMap = categoryMapping.computeIfAbsent(category, (ignored) -> new UnsafeList<>());
        if (!categories.contains(category)) {
            categories.add(category);
        }
        prefabs.add(component);
        modMap.add(component);
        categoryMap.add(component);
        var regName = component.getRegistryName();
        modNameMap.put(regName, component);
        nameMapping.put(regName, component);
        categoryNameMap.put(regName, component);
    }

    public void registerComponents(Class<? extends Component>... componentClasses) {
        for (var componentClass : componentClasses) {
            registerComponent(componentClass);
        }
    }

    void finish() {
        ended = true;
        prefabs.sort((left, right) -> {
            var leftCategory = left.getCategory();
            var rightCategory = right.getCategory();
            if (leftCategory.equals(rightCategory)) {
                var catComps = categoryMapping.get(leftCategory);
                return catComps.indexOf(left) - catComps.indexOf(right);
            } else {
                return categories.indexOf(leftCategory) - categories.indexOf(rightCategory);
            }
        });
    }

    public Component getPrefab(Mod mod, String name) {
        return modAndNameMapping.get(mod).get(name);
    }

    public List<Component> getPrefabs(Mod mod) {
        return modMapping.getOrDefault(mod, Collections.emptyList());
    }

    public List<Component> getPrefabs(String category) { return categoryMapping.getOrDefault(category, Collections.emptyList()); }

    public Component getPrefab(String name) {
        return nameMapping.get(name);
    }

    public List<Component> getAllPrefabs() {
        return prefabs;
    }

    public List<String> getAllCategories() {return categories;}
}
