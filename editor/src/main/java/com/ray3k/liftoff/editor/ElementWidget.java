package com.ray3k.liftoff.editor;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.ray3k.liftoff.Room;
import com.ray3k.liftoff.Room.*;

import static com.ray3k.liftoff.editor.Utils.*;

public class ElementWidget extends Table {
    public TextButton widgetButton;
    public Button deleteButton;
    public Room.Element element;
    
    public ElementWidget(Element element, Skin skin) {
        this.element = element;
        
        if (element instanceof TextElement) {
            widgetButton = new TextButton(element.toString(), skin, "text");
        } else if (element instanceof ImageElement) {
            widgetButton = new TextButton(element.toString(), skin, "image");
        } else if (element instanceof MusicElement) {
            widgetButton = new TextButton(element.toString(), skin, "music");
        } else if (element instanceof SoundElement) {
            widgetButton = new TextButton(element.toString(), skin, "sound");
        }
        add(widgetButton).growX();
        
        widgetButton.getLabelCell().width(150);
        widgetButton.getLabel().setEllipsis("...");
        widgetButton.getLabel().setEllipsis(true);
        cl(widgetButton, () -> fire(new ElementWidgetClickedEvent()));
        
        deleteButton = new Button(skin, "delete-text");
        deleteButton.setColor(1, 1, 1, 0);
        add(deleteButton).space(10);
        cl(deleteButton, () -> fire(new ElementWidgetDeletedEvent()));
        
        setTouchable(Touchable.enabled);
        
        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    deleteButton.addAction(Actions.fadeIn(.3f));
                }
            }
    
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    deleteButton.addAction(Actions.fadeOut(.3f));
                }
            }
        });
    }
    
    public static abstract class ElementWidgetListener implements EventListener {
        public abstract void clicked();
        public abstract void deleted();
    
        @Override
        public boolean handle(Event event) {
            if (event instanceof ElementWidgetClickedEvent) {
                clicked();
                return true;
            } else if (event instanceof ElementWidgetDeletedEvent) {
                deleted();
                return true;
            }
            return false;
        }
    }
    
    public static class ElementWidgetClickedEvent extends Event {
    
    }
    
    public static class ElementWidgetDeletedEvent extends Event {
    
    }
}