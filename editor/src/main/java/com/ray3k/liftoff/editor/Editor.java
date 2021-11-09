package com.ray3k.liftoff.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.liftoff.Core;
import com.ray3k.stripe.FreeTypeSkin;

public class Editor extends ApplicationAdapter {
	public static Skin skin;
	public static Stage stage;
	public static ScreenViewport viewport;
	public static Table root;
	public static AssetManager assetManager;
	
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
		viewport = new ScreenViewport();
		stage = new Stage(viewport);
		Gdx.input.setInputProcessor(stage);
		
		assetManager = new AssetManager(new InternalFileHandleResolver());
		
		root = new Table();
		root.setFillParent(true);
		root.setBackground(skin.getDrawable("bg-10"));
		stage.addActor(root);
	}
	
	@Override
	public void render() {
		ScreenUtils.clear(Color.BLACK);
	}
	
	@Override
	public void resize(int width, int height) {
	
	}
	
	@Override
	public void dispose() {
	
	}
}