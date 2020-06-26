package com.github.falsepattern.chromabeam.core;

import com.github.falsepattern.chromabeam.circuit.CircuitSlave;
import com.github.falsepattern.chromabeam.util.ImmutablePair;
import com.github.falsepattern.chromabeam.mod.Component;
import com.github.falsepattern.chromabeam.util.serialization.Deserializer;
import com.github.falsepattern.chromabeam.util.serialization.Serializer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SaveEngine {
    public static ImmutablePair<Component[], Map<int[], String>> loadComponentsFromFile() {
        var parentFrame = new JFrame();
        var chooser = createChooser();
        int userSelection = chooser.showDialog(parentFrame, "Load");
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try (var input = new Deserializer(new GZIPInputStream(new GZIPInputStream(new FileInputStream(chooser.getSelectedFile()))))) {
                var errorBuilder = new StringBuilder();
                var mismatch = false;
                var fatalMismatch = false;
                var modsInSave = new ArrayList<String>();
                var modVersionsInSave = new HashMap<String, String>();
                int modCountInSave = input.readInt();
                for (int i = 0; i < modCountInSave; i++) {
                    var modid = input.readString();
                    var version = input.readString();
                    modsInSave.add(modid);
                    modVersionsInSave.put(modid, version);
                }
                var modsInGame = new ArrayList<String>();
                var modVersionsInGame = new HashMap<String, String>();
                for (int i = 0; i < GlobalData.mods.length; i++) {
                    var modid = GlobalData.mods[i].getModid();
                    var version = GlobalData.mods[i].getVersion();
                    modsInGame.add(modid);
                    modVersionsInGame.put(modid, version);
                }
                var matchingMods = new ArrayList<String>();
                GlobalData.modsInLoadedSave.clear();
                for (var modInSave: modsInSave) {
                    if (!modsInGame.contains(modInSave)) {
                        fatalMismatch = mismatch = true;
                        errorBuilder.append("[FATAL] Save has extra mod: ").append(modInSave).append(", version: ").append(modVersionsInSave.get(modInSave)).append(" missing from game!\n");
                    } else {
                        var saveVer = modVersionsInSave.get(modInSave);
                        var gameVer = modVersionsInGame.get(modInSave);
                        GlobalData.modsInLoadedSave.put(modInSave, saveVer);
                        if (!saveVer.equals(gameVer)) {
                            if (modInSave.equals("core")) {
                                //special behaviour for compatible versions
                                if (saveVer.equals("0.4.0") || saveVer.equals("0.5.0")) matchingMods.add(modInSave);
                                continue;
                            }
                            mismatch = true;
                            errorBuilder.append("[CRITICAL] Game has different version of mod: ").append(modInSave).append(". In Game: ").append(gameVer).append("; In Save: ").append(saveVer).append('\n');
                            matchingMods.add(modInSave);
                        } else {
                            matchingMods.add(modInSave);
                        }
                    }
                }
                for (var modInGame: modsInGame) {
                    if (!matchingMods.contains(modInGame)) {
                        mismatch = true;
                        errorBuilder.append("[WARNING] Game has extra mod: ").append(modInGame).append(", version: ").append(modVersionsInGame.get(modInGame)).append(" missing from save!\n");
                    }
                }
                if (fatalMismatch || mismatch) {
                    try {
                        GlobalData.soundManager.play("error");
                    } catch (Throwable ignored){}
                    System.err.println(errorBuilder.toString());
                    if (!fatalMismatch) {
                        System.err.println("[!!!DANGER!!!] Loading the save might crash the game! Please back up your current world before attempting to load!\n");
                    }
                }
                if (fatalMismatch) {
                    JOptionPane.showMessageDialog(parentFrame, "Fatal error while loading save. Please check the console for more information.", "Load error", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (mismatch) {
                        int choice = JOptionPane.showConfirmDialog(parentFrame, "Error while loading save. Please check the console, then press OK if you want to continue loading.", "Load warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (choice != JOptionPane.OK_OPTION) {
                            return null;
                        }
                    }
                    var resultComp = deserializeComponents(input);
                    var coreVerSave = GlobalData.modsInLoadedSave.get("core");
                    var resultLabel = new HashMap<int[], String>();
                    if (!coreVerSave.equals("0.3.0") && !coreVerSave.equals("0.3.1")) {
                        int labelCount = input.readInt();
                        for (int i = 0; i < labelCount; i++) {
                            var pos = input.readInts(2);
                            var text = input.readString();
                            resultLabel.put(pos, text);
                        }
                    }
                    return new ImmutablePair<>(resultComp, resultLabel);
                }
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(parentFrame, "Target file could not be found", "Load error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parentFrame, "IO Exception while trying to load data from file", "Load error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    public static boolean saveComponentsToFile(Component[] components, Map<int[], String> labels) {
        var parentFrame = new JFrame();
        var chooser = createChooser();
        int userSelection = chooser.showDialog(parentFrame, "Save");
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            if (!chooser.getSelectedFile().getName().endsWith(".chroma")) {
                chooser.setSelectedFile(new File(chooser.getSelectedFile().getAbsolutePath() + ".chroma"));
            }
            try (var output = new Serializer(new GZIPOutputStream(new GZIPOutputStream(new FileOutputStream(chooser.getSelectedFile()))))) {
                output.writeInt(GlobalData.mods.length);
                for (var mod: GlobalData.mods) {
                    output.writeAsciiString(mod.getModid());
                    output.writeAsciiString(mod.getVersion());
                }
                serializeComponents(output, components);
                output.writeInt(labels.size());
                for (var entry: labels.entrySet()) {
                    output.writeInts(entry.getKey(), 0, 2);
                    output.writeAsciiString(entry.getValue());
                }
                return true;
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(parentFrame, "Target file could not be created", "Save error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parentFrame, "IO Exception while trying to save selection", "Save error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    public static void serializeComponents(Serializer output, Component[] components) {
        int savedCount = 0;
        for (int i = 0; i < components.length; i++) {
            if (!(components[i] instanceof CircuitSlave)) savedCount++;
        }
        output.writeInt(savedCount);
        for (int i = 0; i < components.length; i++) {
            if (!(components[i] instanceof CircuitSlave)) output.writeObject(components[i]);
        }
    }

    public static Component[] deserializeComponents(Deserializer input) {
        int loadCount = input.readInt();
        var result = new Component[loadCount];
        for (int i = 0; i < loadCount; i++) {
            result[i] = input.readObject();
        }
        return result;
    }

    private static JFileChooser createChooser() {
        var chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("ChromaBeam save (.chroma)", "chroma"));
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        return chooser;
    }
}
