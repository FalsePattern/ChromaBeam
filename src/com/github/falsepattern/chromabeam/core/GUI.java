package com.github.falsepattern.chromabeam.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.falsepattern.chromabeam.mod.interfaces.InputHandler;
import com.github.falsepattern.chromabeam.mod.interfaces.Renderable;
import com.github.falsepattern.chromabeam.mod.interfaces.World;
import com.github.falsepattern.chromabeam.util.StuckKeyEvent;
import com.github.falsepattern.chromabeam.util.Vector2i;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

class GUI implements Renderable, InputHandler {
    public enum GUIMode {
        PLACER, SELECTOR, COPYPASTE, CUTPASTE, STACKPRIME, STACKEXECUTE
    }


    private final BitmapFont font;
    private final Map<String, String[]> componentCategoryMappings = new HashMap<>();
    private int[] categoryComponentCounts = new int[0];
    private String[] categories = new String[0];
    private int selected;
    int selectedCategory;
    private TextureRegion arrowTexture;
    private GUIMode mode = GUIMode.PLACER;
    private World world;
    private String[] strings = null;
    public boolean warnCollision = false;

    private boolean keyStuck = false;
    private StuckKeyEvent stuckKey = null;

    public GUI() {
        font = new BitmapFont(Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.fnt"), GlobalData.textureManager.getTexture("arial-15"), true);
        //font = new BitmapFont(true);
    }

    public void setWorld(World world) {
        this.world = world;
    }

    private int mx = 0;
    private int my = 0;
    public void setMousePos(int x, int y) {
        this.mx = x;
        this.my = y;
    }

    public void setGUIMode(GUIMode mode) {
        this.mode = mode;
    }

    public void setSelectionPoints(Vector2i a, Vector2i b) {
        selectionA.set(a.x, a.y);
        selectionB.set(b.x, b.y);
    }

    @Override
    public void prepare(SpriteBatch batch, OrthographicCamera camera) {
        if (strings == null) {
            setupStrings();
        }
    }


    private void setupStrings() {
        var up = getBindName("up");
        var down = getBindName("down");
        var left = getBindName("left");
        var right = getBindName("right");
        var apply = getBindName("apply");
        var cancel = getBindName("cancel");
        var rot = getBindName("rotate");
        var flip = getBindName("flip");
        var comp = getBindName("scroll_components");
        var alt = getBindName("scroll_alternatives");
        var label = getBindName("label");
        var cat = getBindName("scroll_categories");
        strings = new String[] {
                "\n\nIncrease by 1: " + up + "\nIncrease by 10: Shift + " + up +
                        "\n\nDecrease by 1: " + down + "\nDecrease by 10: Shift + " + down +
                        "\n\nConfirm Stacking:" + apply +
                        "\n\nReturn to selection: " + cancel,
                "\n\nIncrease by 1: " + up + "\nIncrease by 10: Shift + " + up +
                        "\n\nDecrease by 1: " + down + "\nDecrease by 10: Shift + " + down +
                        "\n\nConfirm Stacking: [BLOCKED: Stacking would overwrite existing components]" +
                        "\n\nReturn to selection: " + cancel,
                "\n\nStack left: " + left + ", right: " + right + ", up: " + up + ", down:" + down + "\nReturn to selection: " + cancel,
                "Paste: Left click\n\n"
                        + "Rotate Clockwise: " + rot + "      Rotate Counter-Clockwise: SHIFT + " + rot +
                        "\n\nFlip: " + flip+
                        "\n\nMove left: " + left + ", right: " + right + ", up: " + up + ", down:" + down +
                        "\n\nReturn to selection: " + cancel,
                "Paste: [BLOCKED: Target is colliding with existing components]\n\n"
                        + "Rotate Clockwise: " + rot + "      Rotate Counter-Clockwise: SHIFT + " + rot +
                        "\n\nFlip: " + flip +
                        "\n\nMove left: " + left + ", right: " + right + ", up: " + up + ", down:" + down +
                        "\n\nReturn to selection: " + cancel,
                "Press " + getBindName("keybind_hints") + " to toggle keybinding hints",
                "Place: Left click       Delete: Right click       Pick component: Shift + Middle click" +
                        "\n\nNext Component: " + comp + "       Previous Component: Shift + " + comp +
                        "\n\nNext Category: " + cat + "       Previous Category: Shift + " + cat +
                        "\n\nNext Alternative: " + alt + "       Previous Alternative: Shift + " + alt +
                        "\n\nRotate Clockwise: " + rot + "       Rotate Counter-Clockwise: Shift + " + rot +
                        "\n\nFlip: " + flip +
                        "\n\nOpen Duplicator: " + getBindName("duplicator") +
                        "\n\nPlace label: " + label + "       Remove label: SHIFT + " + label +
                        "\n\nSave/Load menu: " + getBindName("saveload") +
                        "\n\nSet target TPS limit: " + getBindName("tpslimit"),
                "Set Point A: Left click\nSet Point B:Right click\n\nCopy: " + getBindName("copy") +
                        "\nCut and paste: " + getBindName("cut") +
                        "\nStack: " + getBindName("stack") +
                        "\nCreate circuit: " + getBindName("circuit") +
                        "\nExport to file: " + getBindName("export") +
                        "\nImport from file: " + getBindName("import") +
                        "\nDelete: " + getBindName("delete") +
                        "\n\nExit duplicator: " + cancel

        };
    }

    @Override
    public void render(SpriteBatch batch, OrthographicCamera camera) {
        if (GlobalData.keyBinds.waitingBindings.size() > 0) {
            var waiting = GlobalData.keyBinds.waitingBindings.get(0);
            uberWarning(batch, "Unbound action \"" + waiting + "\".\nPlease press the button you want to bind it to!");
        } else if (keyStuck && GlobalData.keyBinds.getPressedKeyCount() > 0) {
            var warnBuilder = new StringBuilder();
            int[] keys = GlobalData.keyBinds.getAllPressedKeyIds();
            if (keys.length > 0) {
                for (int i = 0; i < keys.length - 1; i++) {
                    warnBuilder.append(Input.Keys.toString(keys[i])).append(", ");
                }
                warnBuilder.append(Input.Keys.toString(keys[keys.length - 1]));
            }
            uberWarning(batch, "Please release " + warnBuilder.toString() + " to continue!");
        } else {
            world.drawLabels(batch, camera, font);
            font.draw(batch, "X: " + mx + "  Y: " + (GlobalData.hoveredComponentName.equals("") ? my : my + "\n" + GlobalData.hoveredComponentName), Gdx.graphics.getWidth() - 200, 16);
            switch (mode) {
                case PLACER -> drawPlacerGUI(batch);
                case SELECTOR -> drawSelectorGUI(batch);
                case COPYPASTE, CUTPASTE -> drawCopyPasteGUI(batch);
                case STACKPRIME -> drawStackPrimeGUI(batch);
                case STACKEXECUTE -> drawStackExecuteGUI(batch);
            }
        }
    }

    private void uberWarning(SpriteBatch batch, String text) {
        //batch.setColor(0, 0, 0, 1);
        //batch.draw(GlobalData.textureManager.getTexture("beam"), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        font.setColor(Color.RED);
        font.draw(batch, text, 8, 8);
        font.setColor(Color.WHITE);
    }

    public int stackDirection = 0;
    public int stackValue = 0;
    private void drawStackExecuteGUI(SpriteBatch batch) {
        batch.draw(arrowTexture, Gdx.graphics.getWidth() / 2f - 16, Gdx.graphics.getHeight() / 2f - 16, 16, 16, 32, 32, 1, 1, stackDirection * 90);

        font.draw(batch, "Stack amount: " + stackValue + strings[warnCollision ? 1 : 0], 16, 16);
    }

    private void drawStackPrimeGUI(SpriteBatch batch) {
        font.draw(batch, strings[2], 16, 16);
    }

    private void drawCopyPasteGUI(SpriteBatch batch) {
        font.draw(batch, strings[warnCollision ? 4 : 3], 16, 48);
    }

    private boolean showKeyBinds = false;
    private boolean hinthint = true;
    private void drawPlacerGUI(SpriteBatch batch) {
        if (hinthint) {
            font.draw(batch, strings[5], 0, Gdx.graphics.getHeight() - 16);
        }
        if (GlobalData.tpsLimitOn) {
            font.draw(batch, "TPS Limit: " + GlobalData.tpsLimit, 0, Gdx.graphics.getHeight() - 32);
        }
        if (showKeyBinds) {
            font.draw(batch, strings[6], 16, 48);
        } else {
            var h = font.getLineHeight();
            if(prevGroup != selectedCategory || menuText.trim().equals("")) {
                var newText = new StringBuilder();
                for (int i = 0; i < categories.length; i++) {
                    newText.append(GlobalData.langManager.translate("category." + categories[i])).append('\n');
                }
                if (categories.length > selectedCategory && selectedCategory != 0) {
                    newText.append("-------------\n");
                    String[] componentNames = componentCategoryMappings.get(categories[selectedCategory]);
                    for (int l = 0; l < componentNames.length; l++) {
                        newText.append("  ").append(componentNames[l]).append('\n');
                    }
                    newText.append("-------------");
                }
                prevGroup = selectedCategory;
                menuText = newText.toString();
            }
            font.draw(batch, menuText, 30, 16);
            batch.draw(arrowTexture, 10, selectedCategory * h + 13, 16, 16);
            if (selectedCategory != 0) {
                batch.draw(arrowTexture, 10, (categories.length + selected) * h + 13, 16, 16);
            }
        }
    }

    private int prevGroup = -1;
    private String menuText = "";

    private final Vector2i selectionA = new Vector2i();
    private final Vector2i selectionB = new Vector2i();

    private void drawSelectorGUI(SpriteBatch batch) {
        font.draw(batch, strings[7], 10, 48);
    }

    private String getBindName(String binding) {
        return GlobalData.keyBinds.nameToGdxName(binding);
    }

    @Override
    public void finish(SpriteBatch batch, OrthographicCamera camera) {

    }

    @Override
    public void resize(int width, int height) {

    }

    public void initArrowTexture(TextureManager atlas) {
        arrowTexture = atlas.getTexture("arrow");
    }

    public boolean postStuckKeyEvent(String binding, Runnable callback) {
        if (keyStuck) {
            return false;
        } else {
            stuckKey = new StuckKeyEvent(binding, callback);
            keyStuck = true;
            return true;
        }
    }

    private Runnable callback = null;
    @Override
    public void handleInput(boolean shiftHeld) {
        if (keyStuck) {
            if (GlobalData.keyBinds.getPressedKeyCount() == 0) {
                callback = stuckKey.callback;
                keyStuck = false;
                stuckKey = null;
            }
        } else if (callback != null) {
            callback.run();
            callback = null;
        }
        if (mode == GUIMode.PLACER) {
            if (GlobalData.keyBinds.isJustPressed("keybind_hints")) {
                showKeyBinds = !showKeyBinds;
                hinthint = false;
            }
        }
        if (GlobalData.keyBinds.isJustPressed("tpslimit")) {
            postStuckKeyEvent("tpslimit", () -> {

                var limit = acquireTpsLimit();
                if (limit == 0 && GlobalData.tpsLimitOn) {
                    GlobalData.tpsLimitToggle = true;
                } else if (limit != 0) {
                    GlobalData.tpsLimit = limit;
                    if (!GlobalData.tpsLimitOn) {
                        GlobalData.tpsLimitToggle = true;
                    }
                }
            });
        }
    }

    private int acquireTpsLimit() {
        while (true) {
            try {
                var result = Integer.parseInt(JOptionPane.showInputDialog("Enter TPS limit (1-1000000, 0 to disable):"));
                if (result >= 0 && result <= 1000000) {
                    return result;
                }
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }


    void dispose() {
        font.dispose();
    }

    public void readNamesFromRegistry(ComponentRegistry registry, LangManager langManager) {
        categories = registry.getAllCategories().toArray(String[]::new);
        categoryComponentCounts = new int[categories.length];
        for (int i = 0; i < categories.length; i++) {
            var cat = categories[i];
            var prefabs = registry.getPrefabs(cat);
            var componentNames = prefabs.stream().map((component -> langManager.translate(component.getRegistryName()))).toArray(String[]::new);
            componentCategoryMappings.put(cat, componentNames);
            categoryComponentCounts[i] = componentNames.length;
        }
    }

    public void setSelectedComponent(int selectedComponent) {
        selectedCategory = 0;
        while (selectedComponent > categoryComponentCounts[selectedCategory]) {
            selectedComponent -= categoryComponentCounts[selectedCategory++];
        }
        selected = selectedComponent;
    }
}
