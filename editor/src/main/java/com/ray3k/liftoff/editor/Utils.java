package com.ray3k.liftoff.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;

public class Utils {
    private static String os;
    
    public static void cl(Actor actor, Runnable runnable) {
        actor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                runnable.run();
            }
        });
    }
    
    public static List<FileHandle> openMultipleDialog(String title, String defaultPath,
                                                String[] filterPatterns, String filterDescription) {
        String result = null;
        
        //fix file path characters
        if (isWindows()) {
            defaultPath = defaultPath.replace("/", "\\");
        } else {
            defaultPath = defaultPath.replace("\\", "/");
        }
        if (filterPatterns != null && filterPatterns.length > 0) {
            try (var stack = stackPush()) {
                var pointerBuffer = stack.mallocPointer(filterPatterns.length);
                
                for (var filterPattern : filterPatterns) {
                    pointerBuffer.put(stack.UTF8(filterPattern));
                }
                
                pointerBuffer.flip();
                result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(title, defaultPath, pointerBuffer, filterDescription, true);
            }
        } else {
            result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(title, defaultPath, null, filterDescription, true);
        }
        
        if (result != null) {
            var paths = result.split("\\|");
            var returnValue = new ArrayList<FileHandle>();
            for (var path : paths) {
                returnValue.add(Gdx.files.external(path));
            }
            return returnValue;
        } else {
            return null;
        }
    }
    
    public static FileHandle openDialog(String title, String defaultPath,
String[] filterPatterns, String filterDescription) {
        String result = null;
        
        //fix file path characters
        if (isWindows()) {
            defaultPath = defaultPath.replace("/", "\\");
        } else {
            defaultPath = defaultPath.replace("\\", "/");
        }
        
        if (filterPatterns != null && filterPatterns.length > 0) {
            try (MemoryStack stack = stackPush()) {
                PointerBuffer pointerBuffer = stack.mallocPointer(filterPatterns.length);
                
                for (String filterPattern : filterPatterns) {
                    pointerBuffer.put(stack.UTF8(filterPattern));
                }
                
                pointerBuffer.flip();
                result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(title, defaultPath, pointerBuffer, filterDescription, false);
            }
        } else {
            result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(title, defaultPath, null, filterDescription, false);
        }
        
        if (result != null) {
            return Gdx.files.external(result);
        } else {
            return null;
        }
    }
    
    public static FileHandle openFolderDialog(String title, String defaultPath) {
        String result = null;
    
        //fix file path characters
        if (isWindows()) {
            defaultPath = defaultPath.replace("/", "\\");
        } else {
            defaultPath = defaultPath.replace("\\", "/");
        }
        
        result =  org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_selectFolderDialog(title, defaultPath);
    
        if (result != null) {
            return Gdx.files.external(result);
        } else {
            return null;
        }
    }
    
    public static FileHandle saveDialog(String title, String defaultPath,
String[] filterPatterns, String filterDescription) {
        String result = null;
        
        //fix file path characters
        if (isWindows()) {
            defaultPath = defaultPath.replace("/", "\\");
        } else {
            defaultPath = defaultPath.replace("\\", "/");
        }
        
        if (filterPatterns != null && filterPatterns.length > 0) {
            try (var stack = stackPush()) {
                PointerBuffer pointerBuffer = null;
                pointerBuffer = stack.mallocPointer(filterPatterns.length);
                
                for (String filterPattern : filterPatterns) {
                    pointerBuffer.put(stack.UTF8(filterPattern));
                }
                
                pointerBuffer.flip();
                result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_saveFileDialog(title, defaultPath, pointerBuffer, filterDescription);
            }
        } else {
            result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_saveFileDialog(title, defaultPath, null, filterDescription);
        }
        
        if (result != null) {
            return Gdx.files.external(result);
        } else {
            return null;
        }
    }
    
    public static boolean isWindows() {
        if (os == null) {
            os = System.getProperty("os.name");
        }
        
        return os.startsWith("Windows");
    }
    
    public static boolean isLinux() {
        if (os == null) {
            os = System.getProperty("os.name");
        }
        return os.startsWith("Linux");
    }
    
    public static boolean isMac() {
        if (os == null) {
            os = System.getProperty("os.name");
        }
        return os.startsWith("Mac");
    }
}
