package com.ray3k.liftoff.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
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
                returnValue.add(Gdx.files.absolute(path));
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
            return Gdx.files.absolute(result);
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
            return Gdx.files.absolute(result + "/");
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
            return Gdx.files.absolute(result);
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
    
    public static Pool<Vector2> vector2Pool = new Vector2Pool(60);
    private static final Array<Vector2> tmpResult = new Array<>();
    
    public static Array<Vector2> getCurvedLine(float fromX, float fromY, float toX, float toY, float center1X, float center1Y, float center2X, float center2Y, int segments) {
        tmpResult.clear();
        
        float subdiv_step = 1f / segments;
        float subdiv_step2 = subdiv_step * subdiv_step;
        float subdiv_step3 = subdiv_step * subdiv_step * subdiv_step;
        
        float pre1 = 3 * subdiv_step;
        float pre2 = 3 * subdiv_step2;
        float pre4 = 6 * subdiv_step2;
        float pre5 = 6 * subdiv_step3;
        
        float tmp1x = fromX - center1X * 2 + center2X;
        float tmp1y = fromY - center1Y * 2 + center2Y;
        
        float tmp2x = (center1X - center2X) * 3 - fromX + toX;
        float tmp2y = (center1Y - center2Y) * 3 - fromY + toY;
        
        float fx = fromX;
        float fy = fromY;
        
        float dfx = (center1X - fromX) * pre1 + tmp1x * pre2 + tmp2x * subdiv_step3;
        float dfy = (center1Y - fromY) * pre1 + tmp1y * pre2 + tmp2y * subdiv_step3;
        
        float ddfx = tmp1x * pre4 + tmp2x * pre5;
        float ddfy = tmp1y * pre4 + tmp2y * pre5;
        
        float dddfx = tmp2x * pre5;
        float dddfy = tmp2y * pre5;
        
        while (segments-- > 0) {
            tmpResult.add(vector2Pool.obtain().set(fx, fy));
            fx += dfx;
            fy += dfy;
            dfx += ddfx;
            dfy += ddfy;
            ddfx += dddfx;
            ddfy += dddfy;
        }
        tmpResult.add(vector2Pool.obtain().set(fx, fy));
        tmpResult.add(vector2Pool.obtain().set(toX, toY));
        
        return tmpResult;
    }
}
