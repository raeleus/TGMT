package com.ray3k.liftoff.editor;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.ray3k.liftoff.Room;

public class RoomWidget extends Table {
    private static int nameIndex;
    public Room room;
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
        } while (Editor.doesNameExist(room.name));
    
        setup();
    }
    
    private void setup() {
        setBackground(skin.getDrawable("button-normal-10"));
        setTouchable(Touchable.enabled);
        
        label = new Label(room.name, skin, "button");
        label.setTouchable(Touchable.disabled);
        add(label);
        
        this.pack();
    }
}
