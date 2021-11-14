package com.ray3k.liftoff.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.ray3k.liftoff.Room.Action;
import space.earlygrey.shapedrawer.JoinType;

import javax.print.attribute.standard.MediaSize.Other;

import static com.ray3k.liftoff.editor.Editor.*;

public class ConnectorWidget extends Table  {
    private Skin skin;
    private RoomWidget roomWidget;
    private static int actionIndex;
    private static Vector2 temp = new Vector2();
    private static Vector2 temp2 = new Vector2();
    
    public ConnectorWidget(Skin skin, RoomWidget roomWidget) {
        this.skin = skin;
        this.roomWidget = roomWidget;
        setBackground(skin.getDrawable("connector-bg"));
        setTouchable(Touchable.enabled);
        
        var image = new Image(skin, "connector-fg");
        image.setColor(Color.RED);
        add(image);
        
        var dragListener = new DragListener() {
            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
            
            }
    
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
            
            }
    
            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                temp.set(x, y);
                localToStageCoordinates(temp);
                
                var actor = stage1.hit(temp.x, temp.y, true);
                if (actor instanceof RoomWidget) {
                    var otherWidget = (RoomWidget) actor;
                    var action = new Action();
                    do {
                        action.name = "action" + actionIndex++;
                    } while (actionNameExists(action.name));
                    action.targetRoom = otherWidget.room.name;
                    roomWidget.room.actions.add(action);
                }
            }
        };
        addListener(dragListener);
    }
    
    private boolean actionNameExists(String name) {
        boolean exists = false;
        for (var action : roomWidget.room.actions) {
            if (action.name.equals(name)) {
                exists = true;
                break;
            }
        }
        return exists;
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        temp.set(getX() + getWidth() / 2, getY() + getHeight() / 2);
        for (var action : roomWidget.room.actions) {
            for (var other : roomWidgets) {
                if (action.targetRoom.equals(other.room.name)) {
                    temp2.set(other.getX() + 5, other.getY() + other.getHeight() / 2);
                    var curve = Utils.getCurvedLine(temp.x, temp.y, temp2.x, temp2.y, temp.x + 100, temp.y, temp2.x - 100, temp2.y,
                            50);
                    shapeDrawer.path(curve, 1, JoinType.NONE, true);
                }
            }
        }
    }
}
