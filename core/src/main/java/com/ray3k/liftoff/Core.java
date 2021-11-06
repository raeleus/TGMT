package com.ray3k.liftoff;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.stripe.FreeTypeSkin;

public class Core extends ApplicationAdapter {
    public static Skin skin;
    public static Stage stage;
    public static ScreenViewport viewport;
    private static final String long_text = "The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.";
    
    @Override
    public void create() {
        skin = new FreeTypeSkin(Gdx.files.internal("skin/skin.json"));
        viewport = new ScreenViewport();
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        
        var root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.getDrawable("bg-10"));
        stage.addActor(root);
        
        var table = new Table();
        var top = new ScrollPane(table, skin, "panel");
        top.setFadeScrollBars(false);
        
        var label = new Label(long_text, skin);
        label.setWrap(true);
        table.add(label).growX();
        
        table = new Table();
        var bottom = new ScrollPane(table, skin, "panel");
        bottom.setFadeScrollBars(false);
        
        var textButton = new TextButton("test", skin);
        table.add(textButton).expandX().left();
        
        table.row();
        label = new Label(long_text, skin);
        label.setWrap(true);
        table.add(label).growX();
    
        var splitPane = new SplitPane(top, bottom, true, skin);
        root.add(splitPane).grow();
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
    @Override
    public void render() {
        ScreenUtils.clear(Color.GREEN);
        
        viewport.apply();
        stage.act();
        stage.draw();
    }
    
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}