package com.ray3k.liftoff;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class Room {
    public String name;
    public final Array<Element> elements = new Array<>();
    public final Array<Action> actions = new Array<>();
    
    public interface Element {
    
    }
    
    public static class TextElement implements Element {
        public String text;
    }
    
    public static class ImageElement implements Element {
        public String image;
    }
    
    public static class MusicElement implements Element {
        public String music;
    }
    
    public static class SoundElement implements Element {
        public String sound;
    }
    
    public static class Action {
        public String name;
        public String targetRoom;
        public final Array<Key> requiredKeys = new Array<>();
        public final Array<Key> bannedKeys = new Array<>();
        public final Array<Key> giveKeys = new Array<>();
        public final Array<Key> removeKeys = new Array<>();
        public String sound;
    }
    
    public static class Key {
        public String name;
    
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Key) return ((Key)obj).name.equals(name);
            return false;
        }
    }
}
