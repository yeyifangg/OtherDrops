package com.gmail.zariust.otherdrops.event;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.gmail.zariust.otherdrops.OtherBlocks;

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
                    for(String e : known) {
                    	if(knownEvents.containsKey(e))
                    		OtherBlocks.logWarning("Warning: handler " + event.getName() +
                    			" attempted to register event " + e + ", but that was already registered " +
                    			"by handler " + knownEvents.get(e).getName() +
                    			". The event was not re-registered.");
                    	else knownEvents.put(e, event);
                    }
                    loaded.addAll(known);
                    OtherBlocks.logInfo("Event group " + event.getName() + " loaded");
                }
            }
        }
        if(added) OtherBlocks.logInfo("Events loaded: " + loaded.toString());
    }
    
    private static DropEventHandler loadEvent(File file) {
    	String name = file.getName();
        try {
            JarFile jarFile = new JarFile(file);
            JarEntry infoEntry = jarFile.getJarEntry("event.info");
            if(infoEntry == null) throw new DropEventLoadException("No event.info file found.");

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
                DropEventHandler event;
                try { // Try default constructor first
					event = skillClass.newInstance();
				} catch(InstantiationException e) { // If that fails, try OtherBlocks constructor
	                Constructor<? extends DropEventHandler> ctor = skillClass.getConstructor(OtherBlocks.class);
	                event = ctor.newInstance(OtherBlocks.plugin);
				}
                event.info = info;
                event.version = info.getProperty("version");
                return event;
            } else throw new DropEventLoadException("Missing class= property in event.info.");
        } catch(IOException e) { // Failed to load jar or event.info
			OtherBlocks.logWarning("Failed to load event from file " + name + ":");
			e.printStackTrace();
		} catch(ClassNotFoundException e) { // Couldn't find specified class
			OtherBlocks.logWarning("The class specified in event.info for " + name + " could not be found.");
		} catch(IllegalAccessException e) { // Constructor was inaccessible (not public)
			OtherBlocks.logWarning("The constructor for the event in " + name + " was not public.");
		} catch(InvocationTargetException e) { // Constructor threw an exception
			OtherBlocks.logWarning("The event in " + name + " threw an exception while loading:");
			e.getCause().printStackTrace();
		} catch(NoSuchMethodException e) { // Constructor does not exist
			OtherBlocks.logWarning("The event in " + name + " is missing a default or OtherBlocks constructor.");
		} catch(DropEventLoadException e) {
			OtherBlocks.logWarning("Could not load event in " + name + ": " + e.getLocalizedMessage());
		} catch (Exception e) {
            OtherBlocks.logWarning("The events in " + name + " failed to load");
            e.printStackTrace();
        }
        return null;
    }

	public static DropEventHandler getHandlerFor(String name) {
		return knownEvents.get(name);
	}
}
