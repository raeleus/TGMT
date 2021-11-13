package com.ray3k.liftoff.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.liftoff.Room;
import com.ray3k.liftoff.Room.*;
import com.ray3k.liftoff.editor.ElementWidget.ElementWidgetListener;
import com.ray3k.stripe.FreeTypeSkin;
import com.ray3k.stripe.PopTable;
import com.ray3k.stripe.PopTable.PopTableStyle;
import com.ray3k.stripe.PopTableClickListener;
import com.ray3k.stripe.ScrollFocusListener;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

import static com.ray3k.liftoff.editor.Utils.cl;
import static com.ray3k.liftoff.editor.Utils.openDialog;

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
    
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("tgmt");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        configuration.setWindowedMode(800, 800);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        new Lwjgl3Application(new Editor(), configuration);
    }
    
    @Override
    public void create() {
        skin = new FreeTypeSkin(Gdx.files.internal("skin/skin.json"));
        popTableStyle = new PopTableStyle();
        popTableStyle.background = skin.getDrawable("pop-table-10");
        popTableStyle.stageBackground = skin.getDrawable("pop-table-stage-background");
        
        camera1 = new OrthographicCamera();
        viewport1 = new ExtendViewport(800, 800, camera1);
        stage1 = new Stage(viewport1);
        
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
            var file = openDialog("Open JSON", "", new String[]{"json"}, "JSON Files (*.json)");
            if (file != null) showEditor();
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
        
        popTable.row();
        textButton = new TextButton("New Project", skin, "small");
        popTable.add(textButton);
        
        popTable.row();
        textButton = new TextButton("Open Project", skin, "small");
        popTable.add(textButton);
        
        button = new Button(skin, "save");
        root.add(button);
        
        button = new Button(skin, "home");
        root.add(button);
        cl(button, () -> {
            camera1.position.set(0, 0, 0);
            camera1.zoom = 1f;
        });
        
        button = new Button(skin, "zoom-out");
        root.add(button);
        cl(button, () -> {
            zoomOut();
        });
        
        button = new Button(skin, "folder");
        root.add(button);
        cl(button, () -> {
            resourcesPath = Utils.openFolderDialog("Select resources path", "");
        });
        
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
            
            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                dragTarget = stage1.hit(x, y, true);
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
                if (mouseButton == Buttons.RIGHT && canDrag) {
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
                    
                    var actor = stage1.hit(x, y, true);
                    if (actor == null) {
                        var textButton = new TextButton("Add Room", skin, "small");
                        popTable.add(textButton);
                        cl(textButton, () -> {
                            var roomWidget = new RoomWidget(skin);
                            roomWidget.setPosition(x, y - roomWidget.getHeight() / 2);
                            roomWidgets.add(roomWidget);
                            stage1.addActor(roomWidget);
                            popTable.hide();
                        });
                    } else if (actor instanceof RoomWidget) {
                        var roomWidget = (RoomWidget) actor;
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
                            
                            showTextPop("Enter the text", "", (string) -> {
                                var textElement = new TextElement();
                                textElement.text = string;
                                createElementWidget(textElement, roomWidget, verticalGroup, popTable);
                            }, () -> popTable.setHideOnUnfocus(true));
                        });
                        
                        table.add().growX();
                        
                        button = new Button(skin, "image");
                        table.add(button);
                        cl(button, () -> {
                            popTable.setHideOnUnfocus(false);
                            
                            if (resourcesPath == null) resourcesPath = Utils.openFolderDialog("Select resources path", "");
                            
                            showDetailPop("Select an image", gatherImages(), (string) -> {
                                var imageElement = new ImageElement();
                                imageElement.image = string;
                                createElementWidget(imageElement, roomWidget, verticalGroup, popTable);
                            }, () -> popTable.setHideOnUnfocus(true));
                        });
                        
                        button = new Button(skin, "music");
                        table.add(button);
                        cl(button, () -> {
                            popTable.setHideOnUnfocus(false);
        
                            if (resourcesPath == null) resourcesPath = Utils.openFolderDialog("Select resources path", "");
        
                            showDetailPop("Select a music file", gatherSounds(), (string) -> {
                                var musicElement = new MusicElement();
                                musicElement.music = string;
                                createElementWidget(musicElement, roomWidget, verticalGroup, popTable);
                            }, () -> popTable.setHideOnUnfocus(true));
                        });
                        
                        button = new Button(skin, "sound");
                        table.add(button);
                        cl(button, () -> {
                            popTable.setHideOnUnfocus(false);
        
                            if (resourcesPath == null) resourcesPath = Utils.openFolderDialog("Select resources path", "");
        
                            showDetailPop("Select a sound file", gatherSounds(), (string) -> {
                                var soundElement = new SoundElement();
                                soundElement.sound = string;
                                createElementWidget(soundElement, roomWidget, verticalGroup, popTable);
                            }, () -> popTable.setHideOnUnfocus(true));
                        });
                    }
                    
                    popTable.pack();
                    popTable.setPosition(vector2.x, vector2.y, Align.bottomLeft);
                }
                return super.touchDown(event, x, y, pointer, mouseButton);
            }
        };
        
        stage1.addListener(dragListener);
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
                if (element instanceof SoundElement) {
                    if (resourcesPath == null) resourcesPath = Utils.openFolderDialog("Select resources path", "");
                    popTable.setHideOnUnfocus(false);
                    
                    var soundElement = (SoundElement) element;
                    showDetailPop("Select a sound file", gatherSounds(), soundElement.sound, (string) -> {
                        soundElement.sound = string;
                        elementWidget.update();
                    }, () -> popTable.setHideOnUnfocus(true));
                } else if (element instanceof ImageElement) {
                    if (resourcesPath == null) resourcesPath = Utils.openFolderDialog("Select resources path", "");
                    popTable.setHideOnUnfocus(false);
                    
                    var imageElement = (ImageElement) element;
                    showDetailPop("Select an image", gatherSounds(), imageElement.image, (string) -> {
                        imageElement.image = string;
                        elementWidget.update();
                    }, () -> popTable.setHideOnUnfocus(true));
                } else if (element instanceof MusicElement) {
                    if (resourcesPath == null) resourcesPath = Utils.openFolderDialog("Select resources path", "");
                    popTable.setHideOnUnfocus(false);
                    
                    var musicElement = (MusicElement) element;
                    showDetailPop("Select a music file", gatherSounds(), musicElement.music, (string) -> {
                        musicElement.music = string;
                        elementWidget.update();
                    }, () -> popTable.setHideOnUnfocus(true));
                } else if (element instanceof TextElement) {
                    popTable.setHideOnUnfocus(false);
                    
                    var textElement = (TextElement) element;
                    showTextPop("Enter the text", textElement.text, (string) -> {
                        textElement.text = string;
                        elementWidget.update();
                    }, () -> popTable.setHideOnUnfocus(true));
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
    
    private void showDetailPop(String labelText, Array<String> values, DetailConfirmation onConfirm, Runnable onHide) {
        showDetailPop(labelText, values, null, onConfirm, onHide);
    }
    
    private void showDetailPop(String labelText, Array<String> values, String selection, DetailConfirmation onConfirm, Runnable onHide) {
        var popTable = new PopTable(popTableStyle) {
            @Override
            public void hide(Action action) {
                super.hide(action);
                onHide.run();
            }
        };
        
        popTable.setHideOnUnfocus(true);
        
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
        var textButton = new TextButton("OK", skin, "small");
        textButton.setDisabled(values.size == 0);
        popTable.add(textButton);
        cl(textButton, () -> onConfirm.confirmed(list.getSelected()));
        cl(textButton, popTable::hide);
        
        popTable.show(stage2);
    }
    
    private void showTextPop(String title, String text, DetailConfirmation onConfirm, Runnable onHide) {
        var popTable = new PopTable(popTableStyle) {
            @Override
            public void hide(Action action) {
                super.hide(action);
                onHide.run();
            }
        };
    
        popTable.setHideOnUnfocus(true);
        popTable.setKeepCenteredInWindow(true);
    
        var label = new Label(title, skin);
        popTable.add(label);
    
        popTable.row();
        var textArea = new TextArea(text, skin, "text");
        popTable.add(textArea).prefSize(800);
    
        popTable.row();
        var textButton = new TextButton("OK", skin, "small");
        popTable.add(textButton);
        cl(textButton, () -> onConfirm.confirmed(textArea.getText()));
        cl(textButton, popTable::hide);
    
        popTable.show(stage2);
        textArea.setSelection(0, textArea.getText().length());
        stage2.setKeyboardFocus(textArea);
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
    
    public static boolean doesNameExist(String name) {
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