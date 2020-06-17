package com.github.falsepattern.chromabeam.core;
import java.util.HashMap;
import java.util.Map;

public final class LangRegistry {

    private Map<String, String> langMapping;
    private boolean finished;

    public LangRegistry() {
        langMapping = new HashMap<>();
    }

    /**
     * Register a translation mapping from unLocalizedText to localizedText. Usually done for all component registration names.
     */
    public void register(String unLocalizedText, String localizedText) {
        verify(unLocalizedText);
        if (finished) throw new IllegalStateException("Tried to register lang data after the registration phase has ended!");
        langMapping.put(unLocalizedText, localizedText);
    }

    private void verify(String unLocalizedText) {
        for (var ul: langMapping.keySet()) {
            if (ul.equals(unLocalizedText)) throw new IllegalArgumentException("Lang mapping for \"" + unLocalizedText + "\" already registered!");
        }
    }

    /**
     * Registers a translation mapping in the form: "unlocalizedText:Localized Text".
     */
    public void registerPair(String localizationPair) {
        var split = localizationPair.split(":", 2);
        register(split[0].trim(), split[1].trim());
    }

    /**
     * Registers multiple colon-separated mappings from a newline-separated text.
     */
    public void registerMultiple(String localizationBlock) {
        var locs = localizationBlock.split("\n");
        for (var loc: locs) {
            registerPair(loc);
        }
    }

    LangManager createManager() {
        var result = new LangManager(langMapping);
        langMapping = null;
        finished = true;
        return result;
    }
}
