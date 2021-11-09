package com.ray3k.liftoff;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

public class CollapsibleContainer extends WidgetGroup {
    @Override
    public void invalidate() {
        super.invalidate();
    }
    
    @Override
    public void layout() {
        System.out.println("layout");
        Actor selected = getChildren().size > 0 ? getChildren().first() : null;
        
        for (Actor child : getChildren()) {
            child.setVisible(false);
            if (!(child instanceof Layout)) {
                selected = child;
                break;
            }
    
            Layout layout = (Layout) child;
            float cWidth = layout.getPrefWidth();
            float cHeight = layout.getPrefHeight();
            if (cWidth < getWidth() && cHeight < getHeight()) {
                if (selected == null) selected = child;
                else if (cWidth > selected.getWidth()){
                    selected = child;
                }
            }
        }
        
        if (selected != null) {
            selected.setBounds(0, 0, getWidth(), getHeight());
            if (selected instanceof Layout) ((Layout) selected).validate();
            selected.setVisible(true);
        }
    }
    
    @Override
    public float getPrefWidth() {
        if (getChildren().size == 0) return 0;
        Actor child = getChildren().first();
        if (child instanceof Layout) return ((Layout)child).getPrefWidth();
        return 0;
    }
    
    @Override
    public float getPrefHeight() {
        if (getChildren().size == 0) return 0;
        Actor child = getChildren().first();
        if (child instanceof Layout) return ((Layout)child).getPrefHeight();
        return 0;
    }
}
