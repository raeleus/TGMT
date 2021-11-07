package com.ray3k.liftoff;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.liftoff.Room.ImageElement;
import com.ray3k.liftoff.Room.MusicElement;
import com.ray3k.liftoff.Room.SoundElement;
import com.ray3k.liftoff.Room.TextElement;
import com.ray3k.stripe.FreeTypeSkin;
import com.ray3k.stripe.ScrollFocusListener;

public class Core extends ApplicationAdapter {
    public static Skin skin;
    public static Stage stage;
    public static ScreenViewport viewport;
    public static Table root;
    public static final Array<Room> rooms = new Array<>();
    public static Music music;
    public static AssetManager assetManager;
    
    @Override
    public void create() {
        skin = new FreeTypeSkin(Gdx.files.internal("skin/skin.json"));
        viewport = new ScreenViewport();
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        
        assetManager = new AssetManager(new InternalFileHandleResolver());
        
        root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.getDrawable("bg-10"));
        stage.addActor(root);
        
        loadRooms(Gdx.files.internal("template.json"));
        openRoom(0);
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        
        viewport.apply();
        stage.act();
        stage.draw();
    }
    
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
    
    public void loadRooms(FileHandle jsonFile) {
        JsonReader jsonReader = new JsonReader();
        var jsonValue = jsonReader.parse(jsonFile);
        for (var child : jsonValue.iterator()) {
            Room room = new Room();
            room.name = child.name;
            
            for (var storyString : child.get("story").asStringArray()) {
                int colonIndex = storyString.indexOf(':');
                String type = storyString.substring(0, colonIndex);
                String value = storyString.substring(colonIndex + 1);
                switch (type) {
                    case "image":
                        var imageElement = new Room.ImageElement();
                        assetManager.load(value, Texture.class);
                        imageElement.image = value;
                        room.elements.add(imageElement);
                        break;
                    case "text":
                        var textElement = new Room.TextElement();
                        textElement.text = value;
                        room.elements.add(textElement);
                        break;
                    case "music":
                        var musicElement = new Room.MusicElement();
                        assetManager.load(value, Music.class);
                        musicElement.music = value;
                        room.elements.add(musicElement);
                        break;
                    case "sound":
                        var soundElement = new Room.SoundElement();
                        assetManager.load(value, Sound.class);
                        soundElement.sound = value;
                        room.elements.add(soundElement);
                        break;
                }
            }
            
            for (var actionValue : child.get("actions").iterator()) {
                var action = new Room.Action();
                action.name = actionValue.name;
                action.targetRoom = actionValue.getString("targetRoom");
                
                for (var keyString : actionValue.get("requiredKeys").asStringArray()) {
                    var key = new Room.Key();
                    key.name = keyString;
                    action.requiredKeys.add(key);
                }
    
                for (var keyString : actionValue.get("giveKeys").asStringArray()) {
                    var key = new Room.Key();
                    key.name = keyString;
                    action.giveKeys.add(key);
                }
    
                for (var keyString : actionValue.get("removeKeys").asStringArray()) {
                    var key = new Room.Key();
                    key.name = keyString;
                    action.removeKeys.add(key);
                }
                
                room.actions.add(action);
            }
    
            rooms.add(room);
        }
        
        assetManager.finishLoading();
    }
    
    public void openRoom(int index) {
        root.clear();
        var room = rooms.get(index);
    
        var table = new Table();
        var top = new ScrollPane(table, skin, "panel");
        top.setFadeScrollBars(false);
        top.setScrollingDisabled(true, false);
        top.addListener(new ScrollFocusListener(stage));
    
        for (var element : room.elements) {
            if (element instanceof TextElement) {
                var textElement = (TextElement) element;
    
                var label = new Label(textElement.text, skin);
                label.setWrap(true);
                table.add(label).growX();
            } if (element instanceof ImageElement) {
                var imageElement = (ImageElement) element;
                
                var image = new Image(assetManager.get(imageElement.image, Texture.class));
                image.setScaling(Scaling.fit);
                table.add(image);
            } else if (element instanceof MusicElement) {
                var musicElement = (MusicElement) element;
    
                Music music = assetManager.get(musicElement.music);
                music.setLooping(true);
                music.play();
                if (Core.music != null && Core.music != music) {
                    Core.music.stop();
                }
                Core.music = music;
            } else if (element instanceof SoundElement) {
                var soundElement = (SoundElement) element;
    
                Sound sound = assetManager.get(soundElement.sound);
                sound.play();
            }
            table.row();
        }
    
        var horizontalGroup = new HorizontalGroup();
        horizontalGroup.wrap();
        horizontalGroup.rowAlign(Align.center);
        horizontalGroup.align(Align.center);
        var bottom = new ScrollPane(horizontalGroup, skin, "panel");
        bottom.setFadeScrollBars(false);
        bottom.addListener(new ScrollFocusListener(stage));
    
        for (var action : room.actions) {
            var textButton = new TextButton(action.name, skin);
            textButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    openRoom(action.targetRoom);
                }
            });
            horizontalGroup.addActor(textButton);
        }
    
        var splitPane = new SplitPane(top, bottom, true, skin);
        root.add(splitPane).grow();
    }
    
    public void openRoom(String name) {
        for (int i = 0; i < rooms.size; i++) {
            var room = rooms.get(i);
            if (room.name.equals(name)) {
                openRoom(i);
                break;
            }
        }
    }
}