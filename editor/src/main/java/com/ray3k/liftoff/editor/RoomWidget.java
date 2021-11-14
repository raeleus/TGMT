package com.ray3k.liftoff.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.ray3k.liftoff.Room;

public class RoomWidget extends Stack {
    private static int nameIndex;
    public Room room;
    private Table table;
    private Label label;
    private Skin skin;
    
    public RoomWidget(Skin skin, Room room) {
        this.skin = skin;
        this.room = room;
        setup();
    }
    
    public RoomWidget(Skin skin) {
        this.skin = skin;
        room = new Room();
        do {
            room.name = "room " + nameIndex++;
        } while (Editor.doesRoomNameExist(room.name));
    
        setup();
    }
    
    private void setup() {
        setTouchable(Touchable.enabled);
        
        table = new Table();
        table.setTouchable(Touchable.disabled);
        table.setBackground(skin.getDrawable("button-normal-10"));
        add(table);
        
        label = new Label(room.name, skin, "button");
        label.setTouchable(Touchable.disabled);
        table.add(label);
        
        var container = new Container<ConnectorWidget>();
        container.setTouchable(Touchable.childrenOnly);
        container.right();
        add(container);
        
        var connector = new ConnectorWidget(skin, this);
        connector.setPosition(Gdx.input.getX(), Gdx.input.getY());
        container.setActor(connector);
        
        this.pack();
    }
    
    public void update() {
        label.setText(room.name);
    }
}
