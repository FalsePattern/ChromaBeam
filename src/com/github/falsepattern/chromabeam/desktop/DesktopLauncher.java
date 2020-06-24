package com.github.falsepattern.chromabeam.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.falsepattern.chromabeam.circuit.CircuitMod;
import com.github.falsepattern.chromabeam.core.CoreMod;
import com.github.falsepattern.chromabeam.core.GameLoader;
import com.github.falsepattern.chromabeam.mod.Mod;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class DesktopLauncher {
	private static List<File> modFiles;
	private static FileOutputStream logStream;
	private static boolean instrumented = false;
	private static long startNano = 0;
	public static void premain(String args, Instrumentation instrumentation) throws FileNotFoundException {
		startNano = System.nanoTime();
		logStream = new FileOutputStream("log.txt");
		var sysOutStream = System.out;
		var sysErrStream = System.err;
		var combinedOutputStream = new PrintStream(new MultiOutputStream(sysOutStream, logStream));
		var combinedErrorStream = new PrintStream(new MultiOutputStream(sysErrStream, logStream));
		System.setOut(combinedOutputStream);
		System.setErr(combinedErrorStream);
		System.out.println("[PRELOAD] Initiating early setup: Discovering mod files...");
		var modsDir = new File("./mods");
		DesktopLauncher.modFiles = new ArrayList<>();
		var modFilesArr = modsDir.listFiles();
		if (modFilesArr != null) {
			for (var file: modFilesArr) {
				if (file.isDirectory()) continue;
				if (!file.getName().endsWith(".jar")) continue;
				System.out.println("[PRELOAD] Found jar: " + file.getName());
				modFiles.add(file);
				try {
					instrumentation.appendToSystemClassLoaderSearch(new JarFile(file));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		instrumented = true;
	}
	public static void main (String[] arg) throws IOException {
		if (!instrumented) {
			System.out.println("Either the game was not launched using -javaagent, or an error occurred while discovering mod files!");
		}
		try {
			//Suppressing java illegal reflective access warning because we are doing bad stuff in the game
			try {
				Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
				theUnsafe.setAccessible(true);
				Unsafe u = (Unsafe) theUnsafe.get(null);

				Class<?> cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
				Field logger = cls.getDeclaredField("logger");
				u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
				theUnsafe.setAccessible(false);
			} catch (Exception e) {
				// ignore
			}
			var config = new Lwjgl3ApplicationConfiguration();
			List<Mod> mods = new LinkedList<>();
			mods.add(0, new CoreMod());
			if (modFiles.size() != 0) {
				for (var mod : modFiles) {
					var modClasses = findMainClass(mod);
					for (var modClass: modClasses) {
						try {
							var modInstance = (Mod) modClass.getConstructor().newInstance();
							mods.add(modInstance);
							System.out.println("[INIT] Registered main mod class " + modClass.getName() + " in file " + mod.getName());
						} catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
							throw new RuntimeException("Error while instantiating " + modClass.getName() + " in mod " + mod.getName(), e);
						} catch (ClassCastException e) {
							throw new RuntimeException("Error while instantiating " + modClass.getName() + " in mod" + mod.getName() + ": Does not implement Mod interface");
						}
					}
				}
			}
			mods.add(new CircuitMod());
			config.setTitle("ChromaBeam");
			config.setWindowIcon("icon.png");
			new Lwjgl3Application(new GameLoader(mods.toArray(Mod[]::new), startNano), config);
		} finally {
			logStream.close();
		}
		System.exit(0);
	}

	private static List<Class<? extends Mod>> findMainClass(File modFile) {
		List<Class<? extends Mod>> mainModClasses = new LinkedList<>();
		try (var zipFile = new ZipFile(modFile)) {
			for (var e = zipFile.entries(); e.hasMoreElements();) {
				var zipEntry = e.nextElement();
				if (!zipEntry.isDirectory()) {
					var name = zipEntry.getName().replaceAll("/", ".");
					Class<? extends Mod> clazz = getMainModFile(name);
					if (clazz != null) mainModClasses.add(clazz);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return mainModClasses;
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends Mod> getMainModFile(String name) {
		if (!name.endsWith(".class")) return null;
		try {
			var clazz = Class.forName(name.substring(0, name.length() - 6));
			if (Mod.class.isAssignableFrom(clazz)) {
				return (Class<? extends Mod>)clazz;
			}
		} catch (ClassNotFoundException ignored) {}

		return null;
	}
}
