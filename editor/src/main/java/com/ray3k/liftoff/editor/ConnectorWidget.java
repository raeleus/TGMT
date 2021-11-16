package com.ray3k.liftoff.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.ray3k.liftoff.Room.Action;
import space.earlygrey.shapedrawer.JoinType;

import static com.ray3k.liftoff.editor.Editor.*;

public class ConnectorWidget extends Table  {
    private Skin skin;
    public RoomWidget roomWidget;
    private static int actionIndex;
    private static Vector2 temp = new Vector2();
    private static Vector2 temp2 = new Vector2();
    private static final Array<Color> colors = new Array<>(new Color[] {Color.RED, Color.GREEN, Color.ORANGE, Color.BLUE,
            Color.YELLOW, Color.CYAN, Color.LIME, Color.PINK, Color.CHARTREUSE, Color.FOREST, Color.VIOLET, Color.SALMON,
            Color.PURPLE, Color.SKY, Color.BROWN});
    public static int colorIndex;
    private Color color;
    private boolean dragging;
    private final Array<ConnectorLabel> connectorLabels = new Array<>();
    
    public ConnectorWidget(Skin skin, RoomWidget roomWidget) {
        color = colors.get(colorIndex++ % colors.size);
        this.skin = skin;
        this.roomWidget = roomWidget;
        setBackground(skin.getDrawable("connector-bg"));
        setTouchable(Touchable.enabled);
        
        var image = new Image(skin, "connector-fg");
        image.setTouchable(Touchable.disabled);
        image.setColor(color);
        add(image);
        
        var dragListener = new DragListener() {
            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                dragging = true;
            }
    
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
            
            }
    
            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                temp.set(x, y);
                dragging = false;
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
                
                update();
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
    
    public void update() {
        System.out.println("connectorLabels = " + connectorLabels.size);
        System.out.println("roomWidget.room.actions.size = " + roomWidget.room.actions.size);
        for (var connectorLabel : connectorLabels) {
            connectorLabel.remove();
        }
        connectorLabels.clear();
        for (var action : roomWidget.room.actions) {
            for (var other : roomWidgets) {
                if (action.targetRoom.equals(other.room.name)) {
                    var connectorLabel = new ConnectorLabel(roomWidget, other, action, skin);
                    stage1.addActor(connectorLabel);
                    connectorLabels.add(connectorLabel);
                    System.out.println("created connector");
                }
            }
        }
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        temp.set(getX() + getWidth() / 2, getY() + getHeight() / 2);
        for (var connectorLabel : connectorLabels) {
            var other = connectorLabel.targetRoom;
            temp2.set(other.getX() + 5, other.getY() + other.getHeight() / 2);
            var curve = Utils.getCurvedLine(temp.x, temp.y, temp2.x, temp2.y, temp.x + 100, temp.y, temp2.x - 100,
                    temp2.y,
                    50);
            shapeDrawer.setColor(color);
            shapeDrawer.path(curve, 1, JoinType.NONE, true);
            
            var pos = curve.get(curve.size / 2);
            connectorLabel.setPosition(pos.x, pos.y, Align.center);
        }
        
        if (dragging) {
            temp2.set(Gdx.input.getX(), Gdx.input.getY());
            stage1.getViewport().unproject(temp2);
            var curve = Utils.getCurvedLine(temp.x, temp.y, temp2.x, temp2.y, temp.x + 100, temp.y, temp2.x - 100, temp2.y,
                    50);
            shapeDrawer.setColor(color);
            shapeDrawer.path(curve, 1, JoinType.NONE, true);
        }
    }
    
    public static class ConnectorLabel extends Container<Label> {
        private Label label;
        public RoomWidget parent;
        private RoomWidget targetRoom;
        public Action action;
        public ConnectorLabel(RoomWidget parent, RoomWidget targetRoom, Action action, Skin skin) {
            setTouchable(Touchable.enabled);
            label = new Label(action.name, skin);
            label.setTouchable(Touchable.disabled);
            setActor(label);
            this.targetRoom = targetRoom;
            this.action = action;
            this.parent = parent;
            pack();
        }
    }
}
