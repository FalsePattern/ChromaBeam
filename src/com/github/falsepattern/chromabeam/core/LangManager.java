package com.github.falsepattern.chromabeam.core;

import java.util.Collections;
import java.util.Map;

public final class LangManager {
    private final Map<String, String> langMapping;
    LangManager(Map<String, String> langMapping) {
        this.langMapping = Collections.unmodifiableMap(langMapping);
    }

    /**
     * Returns the localized version of the input text, or the input text if such mapping does not exist.
     */
    public String translate(String untranslated) {
        if (langMapping.containsKey(untranslated)) {
            return langMapping.get(untranslated);
        } else {
            for (var mapping: langMapping.entrySet()) {
                untranslated = untranslated.replace(mapping.getKey(), mapping.getValue());
            }
            return untranslated;
        }
    }
}
