package com.ray3k.liftoff.editor;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.ray3k.liftoff.Room;

public class RoomWidget extends Table {
    public Room room;
    public RoomWidget(Skin skin, Room room) {
        setBackground(skin.getDrawable("button-normal-10"));
        setTouchable(Touchable.enabled);
        
        this.room = room;
        this.pack();
    }
    
    public RoomWidget(Skin skin) {
        this(skin, new Room());
    }
}
