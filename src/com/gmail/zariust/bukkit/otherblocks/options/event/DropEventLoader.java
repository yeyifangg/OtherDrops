package com.gmail.zariust.bukkit.otherblocks.options.event;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;

public class DropEventLoader {
	private static Map<String, DropEventHandler> knownEvents = new HashMap<String, DropEventHandler>();
	
    /*
     * Load all the external classes.
     */
    public static void loadEvents() {
        File dir = new File(OtherBlocks.plugin.getDataFolder(), "events");
        ArrayList<String> loaded = new ArrayList<String>();
        dir.mkdir();
        boolean added = false;
        for (String f : dir.list()) {
            if (f.toLowerCase().contains(".jar")) {
                DropEventHandler event = loadEvent(new File(dir, f));
                if (event != null) {
                    event.onLoad();
                    if (!added) {
                        OtherBlocks.logInfo("Collecting and loading events");
                        added = true;
                    }
                    List<String> known = event.getEvents();
                    for(String e : known) knownEvents.put(e, event);
                    loaded.addAll(known);
                    OtherBlocks.logInfo("Event group " + event.getName() + " loaded");
                }
            }
        }
        if(added) OtherBlocks.logInfo("Events loaded: " + loaded.toString());
    }
    
    private static DropEventHandler loadEvent(File file) {
        try {
            JarFile jarFile = new JarFile(file);
            JarEntry infoEntry = jarFile.getJarEntry("event.info");
            if(infoEntry == null) throw new Exception("No event.info file found.");

            InputStream stream = jarFile.getInputStream(infoEntry);
            Properties info = new Properties();
            info.load(stream);
            String mainClass = info.getProperty("class");

            if (mainClass != null) {
                ClassLoader loader = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()},
                		DropEventHandler.class.getClassLoader());
                Class<?> clazz = Class.forName(mainClass, true, loader);
                for (Class<?> subclazz : clazz.getClasses()) {
                    Class.forName(subclazz.getName(), true, loader);
                }
                Class<? extends DropEventHandler> skillClass = clazz.asSubclass(DropEventHandler.class);
                Constructor<? extends DropEventHandler> ctor = skillClass.getConstructor(OtherBlocks.class);
                DropEventHandler event = ctor.newInstance(OtherBlocks.plugin);
                event.info = info;
                return event;
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            OtherBlocks.logWarning("The events in " + file.getName() + " failed to load");
            e.printStackTrace();
            return null;
        }
    }

	public static DropEventHandler getHandlerFor(String name) {
		return knownEvents.get(name);
	}
}
