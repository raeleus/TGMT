package com.ray3k.liftoff.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.liftoff.Room;
import com.ray3k.liftoff.Room.*;
import com.ray3k.liftoff.editor.ConnectorWidget.ConnectorLabel;
import com.ray3k.liftoff.editor.ElementWidget.ElementWidgetListener;
import com.ray3k.stripe.FreeTypeSkin;
import com.ray3k.stripe.PopTable;
import com.ray3k.stripe.PopTable.PopTableStyle;
import com.ray3k.stripe.PopTableClickListener;
import com.ray3k.stripe.ScrollFocusListener;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.lang.StringBuilder;
import java.util.Locale;

import static com.ray3k.liftoff.editor.Utils.*;

public class Editor extends ApplicationAdapter {
    public static Skin skin;
    public static Stage stage1;
    public static Stage stage2;
    public static ExtendViewport viewport1;
    public static OrthographicCamera camera1;
    public static ScreenViewport viewport2;
    public static InputMultiplexer inputMultiplexer;
    public static Table root;
    public static AssetManager assetManager;
    public static PopTableStyle popTableStyle;
    private static final Vector2 vector2 = new Vector2();
    public static final Array<RoomWidget> roomWidgets = new Array<>();
    public static FileHandle resourcesPath;
    public static ShapeDrawer shapeDrawer;
    public static Preferences prefs;
    
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("tgmt");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        configuration.setWindowedMode(800, 800);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        configuration.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
        new Lwjgl3Application(new Editor(), configuration);
    }
    
    @Override
    public void create() {
        prefs = Gdx.app.getPreferences("tgmt editor");
        skin = new FreeTypeSkin(Gdx.files.internal("skin/skin.json"));
        popTableStyle = new PopTableStyle();
        popTableStyle.background = skin.getDrawable("pop-table-10");
        popTableStyle.stageBackground = skin.getDrawable("pop-table-stage-background");
        
        
        camera1 = new OrthographicCamera();
        viewport1 = new ExtendViewport(800, 800, camera1);
        stage1 = new Stage(viewport1);
        
        shapeDrawer = new ShapeDrawer(stage1.getBatch(), skin.getRegion("white-pixel"));
        
        viewport2 = new ScreenViewport();
        stage2 = new Stage(viewport2);
        
        inputMultiplexer = new InputMultiplexer(stage2, stage1);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        assetManager = new AssetManager(new InternalFileHandleResolver());
        
        root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.getDrawable("bg-transparent-10"));
        stage2.addActor(root);
        
        showMenu();
    }
    
    private void showMenu() {
        stage1.clear();
        root.clear();
        
        root.defaults().space(25);
        var textButton = new TextButton("New Project", skin);
        root.add(textButton);
        cl(textButton, () -> {
            roomWidgets.clear();
            var roomWidget = new RoomWidget(skin);
            roomWidgets.add(roomWidget);
            showEditor();
        });
        
        textButton = new TextButton("Open Project", skin);
        root.add(textButton);
        cl(textButton, () -> {
            var file = openDialog("Open JSON", "", new String[]{"*.json"}, "JSON Files (*.json)");
            if (file != null) {
                readFromJson(file);
                showEditor();
            }
        });
    }
    
    private void showEditor() {
        stage1.clear();
        root.clear();
        root.defaults().reset();
        
        root.align(Align.bottomLeft);
        root.pad(50);
        
        var button = new Button(skin, "menu1");
        root.add(button);
        var popTableClickListener = new PopTableClickListener(Align.center, Align.topRight, popTableStyle);
        button.addListener(popTableClickListener);
        
        var popTable = popTableClickListener.getPopTable();
        var textButton = new TextButton("Save to JSON", skin, "small");
        popTable.add(textButton);
        cl(textButton, () -> {
            var fileHandle = Utils.saveDialog("Save to JSON", "", new String[]{"*.json"}, "JSON Files[*.json]");
            if (fileHandle != null) saveToJson(fileHandle);
            popTable.hide();
        });
        
        popTable.row();
        textButton = new TextButton("New Project", skin, "small");
        popTable.add(textButton);
        cl(textButton, () -> {
            RoomWidget.nameIndex = 0;
            ConnectorWidget.colorIndex = 0;
            roomWidgets.clear();
            var roomWidget = new RoomWidget(skin);
            roomWidgets.add(roomWidget);
            showEditor();
            popTable.hide();
        });
        
        popTable.row();
        textButton = new TextButton("Open Project", skin, "small");
        popTable.add(textButton);
        cl(textButton, () -> {
            RoomWidget.nameIndex = 0;
            ConnectorWidget.colorIndex = 0;
            roomWidgets.clear();
            var file = openDialog("Open JSON", "", new String[]{"*.json"}, "JSON Files (*.json)");
            if (file != null) {
                readFromJson(file);
                showEditor();
            }
            popTable.hide();
        });
        
        button = new Button(skin, "save");
        root.add(button);
        cl(button, () -> {
            var fileHandle = Utils.saveDialog("Save to JSON", "", new String[]{"*.json"}, "JSON Files[*.json]");
            if (fileHandle != null) saveToJson(fileHandle);
            popTable.hide();
        });
        
        button = new Button(skin, "home");
        root.add(button);
        cl(button, () -> {
            camera1.position.set(0, 0, 0);
            camera1.zoom = 1f;
        });
        
        button = new Button(skin, "zoom-out");
        root.add(button);
        cl(button, this::zoomOut);
        
        button = new Button(skin, "folder");
        root.add(button);
        cl(button, () -> chooseResourcesPath(true));
        
        for (var roomWidget : roomWidgets) {
            stage1.addActor(roomWidget);
        }
        
        zoomOut();
        if (camera1.zoom < 1) camera1.zoom = 1;
        
        var dragListener = new DragListener() {
            float startX;
            float startY;
            boolean canDrag = true;
            Actor dragTarget;
            float dragTargetOffsetX;
            float dragTargetOffsetY;
            boolean uniqueName = true;
            
            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                startX = x;
                startY = y;
                if (dragTarget != null) {
                    dragTargetOffsetX = x - dragTarget.getX();
                    dragTargetOffsetY = y - dragTarget.getY();
                }
            }
            
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                if (canDrag && dragTarget == null) {
                    camera1.position.set(camera1.position.x - x + startX, camera1.position.y - y + startY, 0);
                } else if (dragTarget instanceof RoomWidget) {
                    dragTarget.setPosition(x - dragTargetOffsetX, y - dragTargetOffsetY);
                }
            }
            
            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
            
            }
            
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                if (canDrag) {
                    camera1.zoom += .2f * amountY;
                    camera1.zoom = Math.max(0, camera1.zoom);
                }
                return false;
            }
            
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int mouseButton) {
                dragTarget = stage1.hit(x, y, true);
                
                if (mouseButton == Buttons.RIGHT && canDrag) {
                    if (dragTarget == null) {
                        var popTable = new PopTable(popTableStyle) {
                            @Override
                            public void hide(Action action) {
                                super.hide(action);
                                canDrag = true;
                            }
                        };
                        popTable.setHideOnUnfocus(true);
                        popTable.setKeepSizedWithinStage(true);
                        popTable.show(stage2);
                        vector2.set(x, y);
                        stage1.getViewport().project(vector2);
                        canDrag = false;
                        
                        var textButton = new TextButton("Add Room", skin, "small");
                        popTable.add(textButton);
                        cl(textButton, () -> {
                            var roomWidget = new RoomWidget(skin);
                            roomWidget.setPosition(x, y - roomWidget.getHeight() / 2);
                            roomWidgets.add(roomWidget);
                            stage1.addActor(roomWidget);
                            popTable.hide();
                        });
    
                        popTable.pack();
                        popTable.setPosition(vector2.x, vector2.y, Align.bottomLeft);
                    } else if (dragTarget instanceof RoomWidget) {
                        var popTable = new PopTable(popTableStyle) {
                            @Override
                            public void hide(Action action) {
                                super.hide(action);
                                canDrag = true;
                            }
                        };
                        popTable.setHideOnUnfocus(true);
                        popTable.setKeepSizedWithinStage(true);
                        popTable.show(stage2);
                        vector2.set(x, y);
                        stage1.getViewport().project(vector2);
                        canDrag = false;
                        
                        var roomWidget = (RoomWidget) dragTarget;
                        
                        var linkedActions = new Array<Room.Action>();
                        for (var other : roomWidgets) {
                            for (var action : other.room.actions) {
                                if (action.targetRoom.equals(roomWidget.room.name)) {
                                    linkedActions.add(action);
                                }
                            }
                        }
                        
                        popTable.pad(5);
                        
                        var table = new Table();
                        table.pad(5);
                        var scrollPane = new ScrollPane(table, skin);
                        scrollPane.setFadeScrollBars(false);
                        popTable.add(scrollPane).growX();
                        scrollPane.addListener(new ScrollFocusListener(stage2));
                        
                        table.defaults().space(5);
                        var roomNameTextField = new TextField(roomWidget.room.name, skin, "room-name");
                        roomNameTextField.setSelection(0, roomNameTextField.getText().length());
                        table.add(roomNameTextField).growX();
                        cl(roomNameTextField, () -> {
                            roomWidget.room.name = roomNameTextField.getText();
                            roomWidget.update();
                            
                            for (var action : linkedActions) {
                                action.targetRoom = roomNameTextField.getText();
                            }
                            
                            uniqueName = true;
                            for (var other : roomWidgets) {
                                if (other != roomWidget && other.room.name.equals(roomWidget.room.name)) {
                                    uniqueName = false;
                                    break;
                                }
                            }
                            
                            if (uniqueName) {
                                roomNameTextField.setColor(Color.WHITE);
                                popTable.setHideOnUnfocus(true);
                            } else {
                                roomNameTextField.setColor(Color.RED);
                                popTable.setHideOnUnfocus(false);
                            }
                        });
                        stage2.setKeyboardFocus(roomNameTextField);
                        
                        table.row();
                        var verticalGroup = new VerticalGroup();
                        verticalGroup.grow();
                        verticalGroup.space(5);
                        table.add(verticalGroup).growX();
                        
                        for (var element : roomWidget.room.elements) {
                            createElementWidget(element, roomWidget, verticalGroup, popTable);
                        }
                        
                        popTable.row();
                        table = new Table();
                        popTable.add(table);
                        
                        var button = new Button(skin, "delete");
                        table.add(button).growX();
                        cl(button, () -> {
                            roomWidgets.removeValue(roomWidget, true);
                            roomWidget.remove();
                            popTable.hide();
                        });
                        
                        button = new Button(skin, "text");
                        table.add(button);
                        cl(button, () -> {
                            popTable.setHideOnUnfocus(false);
                            
                            var textElement = new TextElement();
                            showTextPop("Enter the text", textElement, () -> {
                                createElementWidget(textElement, roomWidget, verticalGroup, popTable);
                            }, () -> popTable.setHideOnUnfocus(uniqueName));
                        });
                        
                        table.add().growX();
                        
                        button = new Button(skin, "image");
                        table.add(button);
                        cl(button, () -> {
                            popTable.setHideOnUnfocus(false);
    
                            chooseResourcesPath();
                            
                            var imageElement = new ImageElement();
                            showDetailPop("Select an image", gatherImages(), imageElement, () -> {
                                createElementWidget(imageElement, roomWidget, verticalGroup, popTable);
                            }, () -> popTable.setHideOnUnfocus(uniqueName));
                        });
                        
                        button = new Button(skin, "music");
                        table.add(button);
                        cl(button, () -> {
                            popTable.setHideOnUnfocus(false);
    
                            chooseResourcesPath();
        
                            var musicElement = new MusicElement();
                            showDetailPop("Select a music file", gatherSounds(), musicElement, () -> {
                                createElementWidget(musicElement, roomWidget, verticalGroup, popTable);
                            }, () -> popTable.setHideOnUnfocus(uniqueName));
                        });
                        
                        button = new Button(skin, "sound");
                        table.add(button);
                        cl(button, () -> {
                            popTable.setHideOnUnfocus(false);
    
                            chooseResourcesPath();
        
                            var soundElement = new SoundElement();
                            showDetailPop("Select a sound file", gatherSounds(), soundElement, () -> {
                                createElementWidget(soundElement, roomWidget, verticalGroup, popTable);
                            }, () -> popTable.setHideOnUnfocus(uniqueName));
                        });
    
                        popTable.pack();
                        popTable.setPosition(vector2.x, vector2.y, Align.bottomLeft);
                    } else if (dragTarget instanceof ConnectorWidget) {
                        var connectorWidget = (ConnectorWidget) dragTarget;
                        
                        var popTable = new PopTable(popTableStyle) {
                            @Override
                            public void hide(Action action) {
                                super.hide(action);
                                canDrag = true;
                            }
                        };
                        popTable.setHideOnUnfocus(true);
                        popTable.setKeepSizedWithinStage(true);
                        popTable.pad(10);
                        popTable.show(stage2);
                        vector2.set(x, y);
                        stage1.getViewport().project(vector2);
                        canDrag = false;
    
                        var label = new Label("Modify Action", skin);
                        popTable.add(label);
                        
                        popTable.row();
                        var verticalGroup = new VerticalGroup();
                        var scrollPane = new ScrollPane(verticalGroup, skin);
                        scrollPane.setFadeScrollBars(false);
                        popTable.add(scrollPane).grow();
                        
                        for (var action : connectorWidget.roomWidget.room.actions) {
                            var textButton = new TextButton(action.name, skin, "small");
                            verticalGroup.addActor(textButton);
                            cl(textButton, () -> {
                                popTable.setHideOnUnfocus(false);
        
                                showActionPop(connectorWidget.roomWidget.room, action, () -> {
                                    textButton.setText(action.name);
                                    connectorWidget.update();
                                }, () -> {
                                    verticalGroup.removeActor(textButton);
                                    connectorWidget.update();
                                }, () -> popTable.setHideOnUnfocus(uniqueName));
                            });
                        }
    
                        popTable.pack();
                        popTable.setPosition(vector2.x, vector2.y, Align.bottomLeft);
                    } else if (dragTarget instanceof ConnectorLabel) {
                        var connectorLabel = (ConnectorLabel) dragTarget;
                        showActionPop(connectorLabel.parent.room, connectorLabel.action, () -> {
                            connectorLabel.parent.update();
                        }, () -> {
                            connectorLabel.parent.update();
                        }, () -> {
                        
                        });
                    }
                }
                return super.touchDown(event, x, y, pointer, mouseButton);
            }
        };
        
        stage1.addListener(dragListener);
    
        for (int i = 0; i < roomWidgets.size; i++) {
            var roomWidget = roomWidgets.get(i);
            roomWidget.update();
        }
    }
    
    private void saveToJson(FileHandle fileHandle) {
        var stringWriter = new StringWriter();
        var jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setOutputType(OutputType.json);
        var json = new Json(OutputType.json);
        json.setWriter(jsonWriter);
        json.writeObjectStart();
        for (var roomWidget : roomWidgets) {
            var room = roomWidget.room;
            json.writeObjectStart(roomWidget.room.name);
            json.writeValue("x", roomWidget.getX());
            json.writeValue("y", roomWidget.getY());
            json.writeArrayStart("story");
            for (var element : room.elements) {
                var line = "";
                var requiredKeys = new StringBuilder();
                boolean first = true;
                for (var key : element.requiredKeys) {
                    if (!first) requiredKeys.append("\n");
                    first = false;
                    requiredKeys.append(key.name);
                }
                var bannedKeys = new StringBuilder();
                first = true;
                for (var key : element.bannedKeys) {
                    if (!first) requiredKeys.append("\n");
                    first = false;
                    bannedKeys.append(key.name);
                }
                
                if (element instanceof TextElement) {
                    var textElement = (TextElement) element;
                    line += "text:" + requiredKeys + ":" + bannedKeys + ":" + textElement.text;
                } else if (element instanceof ImageElement) {
                    var imageElement = (ImageElement) element;
                    line += "image:" + requiredKeys + ":" + bannedKeys + ":" + imageElement.image;
                } else if (element instanceof MusicElement) {
                    var musicElement = (MusicElement) element;
                    line += "music:" + requiredKeys + ":" + bannedKeys + ":" + musicElement.music;
                } else if (element instanceof SoundElement) {
                    var soundElement = (SoundElement) element;
                    line += "sound:" + requiredKeys + ":" + bannedKeys + ":" + soundElement.sound;
                }
                json.writeValue(line);
            }
            json.writeArrayEnd();
            json.writeObjectStart("actions");
            for (var action : room.actions) {
                json.writeObjectStart(action.name);
                json.writeValue("targetRoom", action.targetRoom);
                if (action.sound != null) json.writeValue("sound", action.sound);
                json.writeArrayStart("requiredKeys");
                for (var key : action.requiredKeys) {
                    json.writeValue(key.name);
                }
                json.writeArrayEnd();
                json.writeArrayStart("bannedKeys");
                for (var key : action.bannedKeys) {
                    json.writeValue(key.name);
                }
                json.writeArrayEnd();
                json.writeArrayStart("giveKeys");
                for (var key : action.giveKeys) {
                    json.writeValue(key.name);
                }
                json.writeArrayEnd();
                json.writeArrayStart("removeKeys");
                for (var key : action.removeKeys) {
                    json.writeValue(key.name);
                }
                json.writeArrayEnd();
                json.writeObjectEnd();
            }
            json.writeObjectEnd();
            json.writeObjectEnd();
        }
        json.writeObjectEnd();
        fileHandle.writeString(json.prettyPrint(stringWriter.toString()), false, "UTF-8");
    }
    
    private void readFromJson(FileHandle fileHandle) {
        var jsonReader = new JsonReader();
        var root = jsonReader.parse(fileHandle);
        
        for (var child : root.iterator()) {
            var room = new Room();
            room.name = child.name();
            
            var roomWidget = new RoomWidget(skin, room);
            roomWidget.setPosition(child.getFloat("x"), child.getFloat("y"));
            stage1.addActor(roomWidget);
            roomWidgets.add(roomWidget);
            
            if (child.has("story")) for (var line : child.get("story").asStringArray()) {
                int colonIndex = line.indexOf(':');
                String type = line.substring(0, colonIndex);
                line = line.substring(colonIndex + 1);
    
                colonIndex = line.indexOf(':');
                Array<Key> requiredKeys = new Array<>();
                for (var string : line.substring(0, colonIndex).split("\\n")) {
                    if (string.length() > 0) {
                        var key = new Key();
                        key.name = string;
                        requiredKeys.add(key);
                    }
                }
                line = line.substring(colonIndex + 1);
    
                colonIndex = line.indexOf(':');
                Array<Key> bannedKeys = new Array<>();
                for (var string : line.substring(0, colonIndex).split("\\n")) {
                    if (string.length() > 0) {
                        var key = new Key();
                        key.name = string;
                        bannedKeys.add(key);
                    }
                }
                line = line.substring(colonIndex + 1);
    
                switch (type) {
                    case "image":
                        var imageElement = new Room.ImageElement();
                        assetManager.load(line, Texture.class);
                        imageElement.image = line;
                        room.elements.add(imageElement);
                        imageElement.requiredKeys.addAll(requiredKeys);
                        imageElement.bannedKeys.addAll(bannedKeys);
                        break;
                    case "text":
                        var textElement = new Room.TextElement();
                        textElement.text = line;
                        room.elements.add(textElement);
                        textElement.requiredKeys.addAll(requiredKeys);
                        textElement.bannedKeys.addAll(bannedKeys);
                        break;
                    case "music":
                        var musicElement = new Room.MusicElement();
                        assetManager.load(line, Music.class);
                        musicElement.music = line;
                        room.elements.add(musicElement);
                        musicElement.requiredKeys.addAll(requiredKeys);
                        musicElement.bannedKeys.addAll(bannedKeys);
                        break;
                    case "sound":
                        var soundElement = new Room.SoundElement();
                        assetManager.load(line, Sound.class);
                        soundElement.sound = line;
                        room.elements.add(soundElement);
                        soundElement.requiredKeys.addAll(requiredKeys);
                        soundElement.bannedKeys.addAll(bannedKeys);
                        break;
                }
            }
    
            if (child.has("actions")) for (var actionValue : child.get("actions").iterator()) {
                var action = new Room.Action();
                action.name = actionValue.name;
                action.targetRoom = actionValue.getString("targetRoom");
        
                if (actionValue.has("requiredKeys")) for (var keyString : actionValue.get("requiredKeys").asStringArray()) {
                    var key = new Room.Key();
                    key.name = keyString;
                    action.requiredKeys.add(key);
                }
        
                if (actionValue.has("bannedKeys")) for (var keyString : actionValue.get("bannedKeys").asStringArray()) {
                    var key = new Room.Key();
                    key.name = keyString;
                    action.bannedKeys.add(key);
                }
        
                if (actionValue.has("giveKeys")) for (var keyString : actionValue.get("giveKeys").asStringArray()) {
                    var key = new Room.Key();
                    key.name = keyString;
                    action.giveKeys.add(key);
                }
        
                if (actionValue.has("removeKeys")) for (var keyString : actionValue.get("removeKeys").asStringArray()) {
                    var key = new Room.Key();
                    key.name = keyString;
                    action.removeKeys.add(key);
                }
        
                if (actionValue.has("sound")) {
                    action.sound = actionValue.getString("sound");
                    assetManager.load(action.sound, Sound.class);
                }
        
                room.actions.add(action);
            }
        }
    }
    
    private void createElementWidget(Element element, RoomWidget roomWidget, VerticalGroup verticalGroup, PopTable popTable) {
        if (!roomWidget.room.elements.contains(element, true)) {
            roomWidget.room.elements.add(element);
            roomWidget.update();
        }
        
        var elementWidget = new ElementWidget(element, skin);
        verticalGroup.addActor(elementWidget);
        elementWidget.addListener(new ElementWidgetListener() {
            @Override
            public void clicked() {
                boolean temp = true;
                for (var other : roomWidgets) {
                    if (other != roomWidget && other.room.name.equals(roomWidget.room.name)) {
                        temp = false;
                        break;
                    }
                }
                final boolean uniqueName = temp;
                
                if (element instanceof SoundElement) {
                    chooseResourcesPath();
                    popTable.setHideOnUnfocus(false);
                    
                    var soundElement = (SoundElement) element;
                    showDetailPop("Select a sound file", gatherSounds(), soundElement.sound, soundElement,
                            elementWidget::update, () -> popTable.setHideOnUnfocus(uniqueName));
                } else if (element instanceof ImageElement) {
                    chooseResourcesPath();
                    popTable.setHideOnUnfocus(false);
                    
                    var imageElement = (ImageElement) element;
                    showDetailPop("Select an image", gatherImages(), imageElement.image, imageElement,
                            elementWidget::update, () -> popTable.setHideOnUnfocus(uniqueName));
                } else if (element instanceof MusicElement) {
                    chooseResourcesPath();
                    popTable.setHideOnUnfocus(false);
                    
                    var musicElement = (MusicElement) element;
                    showDetailPop("Select a music file", gatherSounds(), musicElement.music, musicElement,
                            elementWidget::update, () -> popTable.setHideOnUnfocus(uniqueName));
                } else if (element instanceof TextElement) {
                    popTable.setHideOnUnfocus(false);
                    
                    var textElement = (TextElement) element;
                    showTextPop("Enter the text", textElement, elementWidget::update,
                            () -> popTable.setHideOnUnfocus(uniqueName));
                }
            }
        
            @Override
            public void deleted() {
                verticalGroup.removeActor(elementWidget);
                roomWidget.room.elements.removeValue(elementWidget.element, true);
            }
    
            @Override
            public void movedUp() {
                int index = roomWidget.room.elements.indexOf(elementWidget.element, true);
                int newIndex = Math.max(index - 1, 0);
                roomWidget.room.elements.removeIndex(index);
                roomWidget.room.elements.insert(newIndex, elementWidget.element);
                
                verticalGroup.removeActorAt(index, false);
                verticalGroup.addActorAt(newIndex, elementWidget);
            }
    
            @Override
            public void movedDown() {
                int index = roomWidget.room.elements.indexOf(elementWidget.element, true);
                int newIndex = Math.min(index + 1, roomWidget.room.elements.size - 1);
                roomWidget.room.elements.removeIndex(index);
                roomWidget.room.elements.insert(newIndex, elementWidget.element);
    
                verticalGroup.removeActorAt(index, false);
                verticalGroup.addActorAt(newIndex, elementWidget);
            }
        });
    }
    
    private void showDetailPop(String labelText, Array<String> values, Element element, Runnable onConfirm, Runnable onHide) {
        showDetailPop(labelText, values, null, element, onConfirm, onHide);
    }
    
    private void showDetailPop(String labelText, Array<String> values, String selection, Element element, Runnable onConfirm, Runnable onHide) {
        var popTable = new PopTable(popTableStyle) {
            @Override
            public void hide(Action action) {
                super.hide(action);
                onHide.run();
            }
        };
        
        popTable.setHideOnUnfocus(true);
        popTable.pad(20);
        
        var label = new Label(labelText, skin);
        popTable.add(label);
        
        popTable.row();
        var list = new List<String>(skin);
        list.setAlignment(Align.center);
        list.setItems(values);
        list.setSelected(selection);
        
        var scrollPane = new ScrollPane(list, skin);
        scrollPane.setFadeScrollBars(false);
        popTable.add(scrollPane).growX();
        scrollPane.addListener(new ScrollFocusListener(stage2));
    
        popTable.row();
        var table = new Table();
        popTable.add(table);
    
        table.defaults().space(5);
        StringBuilder value = new StringBuilder();
        for (var requiredKey : element.requiredKeys) {
            value.append(requiredKey.name + "\n");
        }
        var requiredKeysField = new TextArea(value.toString(), skin, "required-keys");
        requiredKeysField.setPrefRows(5);
        table.add(requiredKeysField);
    
        value = new StringBuilder();
        for (var bannedKey : element.bannedKeys) {
            value.append(bannedKey.name + "\n");
        }
        var bannedKeysField = new TextArea(value.toString(), skin, "banned-keys");
        bannedKeysField.setPrefRows(5);
        table.add(bannedKeysField);
        
        popTable.row();
        var textButton = new TextButton("OK", skin, "small");
        textButton.setDisabled(values.size == 0);
        popTable.add(textButton);
        cl(textButton, () -> {
            if (element instanceof ImageElement) ((ImageElement) element).image = list.getSelected();
            if (element instanceof SoundElement) ((SoundElement) element).sound = list.getSelected();
            if (element instanceof MusicElement) ((MusicElement) element).music = list.getSelected();
            
            element.requiredKeys.clear();
            for (var keyString : requiredKeysField.getText().split("\\n")) {
                if (keyString.length() > 0) {
                    var key = new Key();
                    key.name = keyString;
                    element.requiredKeys.add(key);
                }
            }
    
            element.bannedKeys.clear();
            for (var keyString : bannedKeysField.getText().split("\\n")) {
                if (keyString.length() > 0) {
                    var key = new Key();
                    key.name = keyString;
                    element.bannedKeys.add(key);
                }
            }
            
            onConfirm.run();
        });
        cl(textButton, popTable::hide);
        
        popTable.show(stage2);
    }
    
    private void showTextPop(String title, TextElement textElement, Runnable onConfirm, Runnable onHide) {
        var popTable = new PopTable(popTableStyle) {
            @Override
            public void hide(Action action) {
                super.hide(action);
                onHide.run();
            }
        };
    
        popTable.setHideOnUnfocus(true);
        popTable.setKeepCenteredInWindow(true);
        popTable.pad(20);
    
        var label = new Label(title, skin);
        popTable.add(label);
    
        popTable.row();
        var textTextArea = new TextArea(textElement.text, skin, "text");
        popTable.add(textTextArea).prefSize(800);
        
        popTable.row();
        var table = new Table();
        popTable.add(table);
        
        table.defaults().space(5);
        StringBuilder value = new StringBuilder();
        for (var requiredKey : textElement.requiredKeys) {
            value.append(requiredKey.name + "\n");
        }
        var requiredKeysField = new TextArea(value.toString(), skin, "required-keys");
        requiredKeysField.setPrefRows(5);
        table.add(requiredKeysField);
    
        value = new StringBuilder();
        for (var bannedKey : textElement.bannedKeys) {
            value.append(bannedKey.name + "\n");
        }
        var bannedKeysField = new TextArea(value.toString(), skin, "banned-keys");
        bannedKeysField.setPrefRows(5);
        table.add(bannedKeysField);
    
        popTable.row();
        var textButton = new TextButton("OK", skin, "small");
        popTable.add(textButton);
        cl(textButton, () -> {
            textElement.text = textTextArea.getText();
    
            textElement.requiredKeys.clear();
            for (var keyString : requiredKeysField.getText().split("\\n")) {
                if (keyString.length() > 0) {
                    var key = new Key();
                    key.name = keyString;
                    textElement.requiredKeys.add(key);
                }
            }
    
            textElement.bannedKeys.clear();
            for (var keyString : bannedKeysField.getText().split("\\n")) {
                if (keyString.length() > 0) {
                    var key = new Key();
                    key.name = keyString;
                    textElement.bannedKeys.add(key);
                }
            }
            
            onConfirm.run();
        });
        cl(textButton, popTable::hide);
    
        popTable.show(stage2);
        textTextArea.setSelection(0, textTextArea.getText().length());
        stage2.setKeyboardFocus(textTextArea);
    }
    
    private void chooseResourcesPath() {
        chooseResourcesPath(false);
    }
    
    private void chooseResourcesPath(boolean force) {
        if (force || resourcesPath == null) {
            resourcesPath = openFolderDialog("Select resources path", prefs.getString("resourcesPath", ""));
            prefs.putString("resourcesPath", resourcesPath == null ? "" : resourcesPath.path());
            prefs.flush();
        }
    }
    
    private void showActionPop(Room room, Room.Action action, Runnable onOK, Runnable onDelete, Runnable onHide) {
        chooseResourcesPath();
        
        var popTable = new PopTable(popTableStyle) {
            @Override
            public void hide(Action action) {
                super.hide(action);
                onHide.run();
            }
        };
        
        popTable.setHideOnUnfocus(true);
        popTable.setKeepCenteredInWindow(true);
        popTable.pad(20);
    
        popTable.defaults().space(10);
        var table = new Table();
        popTable.add(table);
        
        table.defaults().space(10).fillX();
        var actionNameField = new TextField(action.name, skin, "action-name");
        table.add(actionNameField);
        
        table.row();
        var targetRoomSelectBox = new SelectBox<String>(skin, "target-room");
        var targetRooms = new Array<String>();
        for (var roomWidget : roomWidgets) {
            if (room != roomWidget.room) targetRooms.add(roomWidget.room.name);
        }
        targetRoomSelectBox.setItems(targetRooms);
        targetRoomSelectBox.setSelected(action.targetRoom);
        table.add(targetRoomSelectBox);
        targetRoomSelectBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popTable.setHideOnUnfocus(false);
            }
        });
        targetRoomSelectBox.getList().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popTable.setHideOnUnfocus(true);
            }
        });
        
        table.row();
        var soundSelectBox = new SelectBox<String>(skin, "sound");
        var sounds = new Array<String>();
        sounds.add("");
        for (var sound : gatherSounds()) {
            sounds.add(sound);
        }
        soundSelectBox.setItems(sounds);
        soundSelectBox.setSelected(action.sound == null ? "" : action.sound);
        table.add(soundSelectBox);
        soundSelectBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popTable.setHideOnUnfocus(false);
            }
        });
        soundSelectBox.getList().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popTable.setHideOnUnfocus(true);
            }
        });
        
        popTable.row();
        table = new Table();
        popTable.add(table).grow();
        
        table.defaults().space(10);
        StringBuilder text = new StringBuilder();
        for (var key : action.requiredKeys) {
            text.append(key.name).append("\n");
        }
        var requiredKeysField = new TextArea(text.toString(), skin, "required-keys");
        requiredKeysField.setPrefRows(10);
        table.add(requiredKeysField);
    
        text = new StringBuilder();
        for (var key : action.bannedKeys) {
            text.append(key.name).append("\n");
        }
        var bannedKeysField = new TextArea(text.toString(), skin, "banned-keys");
        bannedKeysField.setPrefRows(10);
        table.add(bannedKeysField);
    
        text = new StringBuilder();
        for (var key : action.giveKeys) {
            text.append(key.name).append("\n");
        }
        var giveKeysField = new TextArea(text.toString(), skin, "give-keys");
        giveKeysField.setPrefRows(10);
        table.add(giveKeysField);
    
        text = new StringBuilder();
        for (var key : action.removeKeys) {
            text.append(key.name).append("\n");
        }
        var removeKeysField = new TextArea(text.toString(), skin, "remove-keys");
        removeKeysField.setPrefRows(10);
        table.add(removeKeysField);
        
        popTable.row();
        table = new Table();
        popTable.add(table);
        
        var textButton = new TextButton("OK", skin, "small");
        table.add(textButton);
        cl(textButton, () -> {
            action.name = actionNameField.getText();
            action.targetRoom = targetRoomSelectBox.getSelected();
            action.sound = soundSelectBox.getSelected().equals("") ? null : soundSelectBox.getSelected();
            
            action.requiredKeys.clear();
            for (var value : requiredKeysField.getText().split("\\n")) {
                if (value.length() > 0) {
                    var key = new Key();
                    key.name = value;
                    action.requiredKeys.add(key);
                }
            }
    
            action.bannedKeys.clear();
            for (var value : bannedKeysField.getText().split("\\n")) {
                if (value.length() > 0) {
                    var key = new Key();
                    key.name = value;
                    action.bannedKeys.add(key);
                }
            }
    
            action.giveKeys.clear();
            for (var value : giveKeysField.getText().split("\\n")) {
                if (value.length() > 0) {
                    var key = new Key();
                    key.name = value;
                    action.giveKeys.add(key);
                }
            }
    
            action.removeKeys.clear();
            for (var value : removeKeysField.getText().split("\\n")) {
                if (value.length() > 0) {
                    var key = new Key();
                    key.name = value;
                    action.removeKeys.add(key);
                }
            }
            
            popTable.hide();
            onOK.run();
        });
    
        textButton = new TextButton("DELETE", skin, "small");
        table.add(textButton);
        cl(textButton, () -> {
            room.actions.removeValue(action, true);
            popTable.hide();
            onDelete.run();
        });
        
        popTable.show(stage2);
        actionNameField.setSelection(0, actionNameField.getText().length());
        stage2.setKeyboardFocus(actionNameField);
    }
    
    private interface DetailConfirmation {
        void confirmed(String selection);
    }
    
    private Array<String> gatherImages() {
        var fileNameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase(Locale.ROOT).matches(".*\\.((png)|(jpg))$");
            }
        };
        var array = new Array<String>();
        if (resourcesPath != null) for (var fileHandle : resourcesPath.list(fileNameFilter)) {
            var string = fileHandle.nameWithoutExtension();
            if (!array.contains(string, false)) array.add(string);
        }
        return array;
    }
    
    private Array<String> gatherSounds() {
        var fileNameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase(Locale.ROOT).matches(".*\\.((mp3)|(ogg)|(wav))$");
            }
        };
        var array = new Array<String>();
        if (resourcesPath != null) for (var fileHandle : resourcesPath.list(fileNameFilter)) {
            var string = fileHandle.nameWithoutExtension();
            if (!array.contains(string, false)) array.add(string);
        }
        return array;
    }
    
    public void zoomOut() {
        if (roomWidgets.size == 0) {
            camera1.position.set(0, 0, 0);
            camera1.zoom = 1f;
            return;
        }
        
        float cBottom = Float.MAX_VALUE, cTop = -Float.MAX_VALUE, cLeft = Float.MAX_VALUE, cRight = -Float.MAX_VALUE;
        float bottom = Float.MAX_VALUE, top = -Float.MAX_VALUE, left = Float.MAX_VALUE, right = -Float.MAX_VALUE;
        for (var widget : roomWidgets) {
            if (widget.getX() + widget.getWidth() / 2 < cLeft) cLeft = widget.getX() + widget.getWidth() / 2;
            if (widget.getX() + widget.getWidth() / 2 > cRight) cRight = widget.getX() + widget.getWidth() / 2;
            if (widget.getY() + widget.getHeight() / 2 < cBottom) cBottom = widget.getY() + widget.getHeight() / 2;
            if (widget.getY() + widget.getHeight() / 2 > cTop) cTop = widget.getY() + widget.getHeight() / 2;
            
            if (widget.getX() < left) left = widget.getX();
            if (widget.getX() + widget.getWidth() > right) right = widget.getX() + widget.getWidth();
            if (widget.getY() < bottom) bottom = widget.getY();
            if (widget.getY() + widget.getHeight() > top) top = widget.getY() + widget.getHeight();
        }
        
        float cWidth = cRight - cLeft;
        float cHeight = cTop - cBottom;
        
        camera1.position.set(cLeft + cWidth / 2, cBottom + cHeight / 2, 0);
        
        float padding = 50f;
        float width = right - left + padding * 2;
        float height = top - bottom + padding * 2;
        camera1.zoom = Math.max(width / camera1.viewportWidth, height / camera1.viewportHeight);
    }
    
    @Override
    public void render() {
        ScreenUtils.clear(skin.getColor("background"));
        
        stage1.act();
        stage2.act();
        
        stage1.draw();
        stage2.draw();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport1.update(width, height);
        viewport2.update(width, height, true);
    }
    
    @Override
    public void dispose() {
        stage1.dispose();
        stage2.dispose();
        skin.dispose();
    }
    
    public static boolean doesRoomNameExist(String name) {
        boolean exists = false;
        for (var widget : roomWidgets) {
            if (widget.room.name.equals(name)) {
                exists = true;
                break;
            }
        }
        return exists;
    }
}