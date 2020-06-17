package com.github.falsepattern.chromabeam.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;


import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.SortedIntList;
import com.github.falsepattern.chromabeam.util.storage.UnsafeList;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class KeyBinds implements InputProcessor {
    private Map<String, Integer> bindings;
    private IntMap<Boolean> pressed;
    private IntMap<Boolean> prevFramePressed;
    public KeyBinds() {
        bindings = loadBindings();
        pressed = new IntMap<>();
        prevFramePressed = new IntMap<>();
    }

    public void setBinding(String name, int keyCode) {
        bindings.put(name, keyCode);
    }

    public void update() {
        for (var key: pressed.entries()) {
            prevFramePressed.put(key.key, key.value);
        }
    }

    public boolean isPressed(String binding) {
        if (!bindings.containsKey(binding)) {
            bindPrompt(binding);
            return false;
        }
        return pressed.get(bindings.get(binding), false);
    }

    public boolean isJustPressed(String binding) {
        if (!bindings.containsKey(binding)) {
            bindPrompt(binding);
            return false;
        }
        var id = bindings.get(binding);
        return !prevFramePressed.get(id, false) && pressed.get(id, false);
    }

    public int getPressedKeyCount() {
        int total = 0;
        for (var key: pressed.values()) {
            if (key) {
                total++;
            }
        }
        return total;
    }

    public int[] getAllPressedKeyIds() {
        var list = new UnsafeList<Integer>();
        for (var key: pressed) {
            if (key.value) {
                list.add(key.key);
            }
        }
        var arr = list.toArray(Integer[]::new);
        var result = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    public boolean isJustReleased(String binding) {
        if (!bindings.containsKey(binding)) {
            bindPrompt(binding);
            return false;
        }
        var id = bindings.get(binding);
        return prevFramePressed.get(id, false) && !pressed.get(id, false);
    }

    public int nameToKeyCode(String binding) {
        if (!bindings.containsKey(binding)) {
            bindPrompt(binding);
            return -1;
        }
        return bindings.get(binding);
    }

    public String nameToGdxName(String binding) {
        if (!bindings.containsKey(binding)) {
            bindPrompt(binding);
            return "UNKNOWN";
        }
        return Input.Keys.toString(bindings.get(binding));
    }

    private String idToGdxFieldName(int id) {
        for (var f: Input.Keys.class.getDeclaredFields()) {
            try {
                if ((int)f.get(null) == id) {
                    return f.getName();
                }
            } catch (IllegalAccessException ignored) {}
        }
        throw new IllegalArgumentException();
    }

    private int gdxFieldNameToId(String name) throws NoSuchFieldException {
        try {
            return (int)Input.Keys.class.getDeclaredField(name).get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void bindPrompt(String binding) {
        if (!waitingBindings.contains(binding))
            waitingBindings.add(binding);
    }

    private void saveBindings() {
        var file = Gdx.files.local("bindings.txt");
        try (var output = new PrintStream(file.write(false))) {
            for (var binding: bindings.entrySet().stream().sorted(Map.Entry.comparingByKey(String::compareTo)).collect(Collectors.toUnmodifiableList())) {
                int bind = binding.getValue();
                output.println(binding.getKey() + ":" + idToGdxFieldName(bind));
            }
            output.println("\n\n\n# Below this are the names and 'binding ids' for all keyboard keys supported by LibGDX.");
            output.println("# Bindings can have overlaps, because specific bindings are only used in specific contexts.");
            for (var f: Input.Keys.class.getDeclaredFields()) {
                try {
                    output.println("# " + Input.Keys.toString((int)f.get(null)) + ": " + f.getName());
                } catch (IllegalAccessException | IllegalArgumentException ignored){}
            }
        }
    }

    private Map<String, Integer> loadBindings() {
        var result = new HashMap<String, Integer>();
        var file = Gdx.files.local("bindings.txt");
        if (file.exists()) {
            try (var input = new Scanner(file.read())) {
                while (input.hasNextLine()) {
                    var line = input.nextLine().trim();
                    if (line.startsWith("#") || !line.contains(":")) continue;
                    var parts = line.split(":");
                    if (parts.length != 2) continue;
                    try {
                        result.put(parts[0].trim(), gdxFieldNameToId(parts[1].trim()));
                    } catch (NoSuchFieldException e) {
                        System.err.println("Unknown key \"" + parts[1].trim() + "\" for action \"" + parts[0].trim() + "\"");
                    }
                }
            }
        }
        return result;
    }

    public final List<String> waitingBindings = new UnsafeList<>();

    @Override
    public boolean keyDown(int keycode) {
        if (waitingBindings.size() > 0) {
            bindings.put(waitingBindings.remove(0), keycode);
            saveBindings();
        } else {
            pressed.put(keycode, true);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        pressed.put(keycode, false);
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
