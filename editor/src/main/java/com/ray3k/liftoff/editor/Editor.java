package com.ray3k.liftoff.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.liftoff.Core;
import com.ray3k.stripe.FreeTypeSkin;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;

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
		
		showMenu();
	}
	
	private void showMenu() {
		root.clear();
		
		root.defaults().space(25);
		var textButton = new TextButton("New Project", skin);
		root.add(textButton);
		textButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				showEditor();
			}
		});
		
		textButton = new TextButton("Open Project", skin);
		root.add(textButton);
		textButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				var file = openDialog("Open JSON", "", new String[]{"json"}, "JSON Files (*.json)");
				if (file != null) showEditor();
			}
		});
	}
	
	private void showEditor() {
		root.clear();
		
		
	}
	
	@Override
	public void render() {
		ScreenUtils.clear(Color.BLACK);
		
		stage.act();
		stage.draw();
	}
	
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
	}
	
	@Override
	public void dispose() {
		stage.dispose();
		skin.dispose();
	}
	
	public static List<File> openMultipleDialog(String title, String defaultPath,
										 String[] filterPatterns, String filterDescription) {
		String result = null;
		
		//fix file path characters
		if (isWindows()) {
			defaultPath = defaultPath.replace("/", "\\");
		} else {
			defaultPath = defaultPath.replace("\\", "/");
		}
		if (filterPatterns != null && filterPatterns.length > 0) {
			try (var stack = stackPush()) {
				var pointerBuffer = stack.mallocPointer(filterPatterns.length);
				
				for (var filterPattern : filterPatterns) {
					pointerBuffer.put(stack.UTF8(filterPattern));
				}
				
				pointerBuffer.flip();
				result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(title, defaultPath, pointerBuffer, filterDescription, true);
			}
		} else {
			result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(title, defaultPath, null, filterDescription, true);
		}
		
		if (result != null) {
			var paths = result.split("\\|");
			var returnValue = new ArrayList<File>();
			for (var path : paths) {
				returnValue.add(new File(path));
			}
			return returnValue;
		} else {
			return null;
		}
	}
	
	public static File openDialog(String title, String defaultPath,
						   String[] filterPatterns, String filterDescription) {
		String result = null;
		
		//fix file path characters
		if (isWindows()) {
			defaultPath = defaultPath.replace("/", "\\");
		} else {
			defaultPath = defaultPath.replace("\\", "/");
		}
		
		if (filterPatterns != null && filterPatterns.length > 0) {
			try (MemoryStack stack = stackPush()) {
				PointerBuffer pointerBuffer = stack.mallocPointer(filterPatterns.length);
				
				for (String filterPattern : filterPatterns) {
					pointerBuffer.put(stack.UTF8(filterPattern));
				}
				
				pointerBuffer.flip();
				result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(title, defaultPath, pointerBuffer, filterDescription, false);
			}
		} else {
			result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(title, defaultPath, null, filterDescription, false);
		}
		
		if (result != null) {
			return new File(result);
		} else {
			return null;
		}
	}
	
	public static File saveDialog(String title, String defaultPath,
						   String[] filterPatterns, String filterDescription) {
		String result = null;
		
		//fix file path characters
		if (isWindows()) {
			defaultPath = defaultPath.replace("/", "\\");
		} else {
			defaultPath = defaultPath.replace("\\", "/");
		}
		
		if (filterPatterns != null && filterPatterns.length > 0) {
			try (var stack = stackPush()) {
				PointerBuffer pointerBuffer = null;
				pointerBuffer = stack.mallocPointer(filterPatterns.length);
				
				for (String filterPattern : filterPatterns) {
					pointerBuffer.put(stack.UTF8(filterPattern));
				}
				
				pointerBuffer.flip();
				result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_saveFileDialog(title, defaultPath, pointerBuffer, filterDescription);
			}
		} else {
			result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_saveFileDialog(title, defaultPath, null, filterDescription);
		}
		
		if (result != null) {
			return new File(result);
		} else {
			return null;
		}
	}
	
	private static String os;
	
	public static boolean isWindows() {
		if (os == null) {
			os = System.getProperty("os.name");
		}
		
		return os.startsWith("Windows");
	}
	
	public static boolean isLinux() {
		if (os == null) {
			os = System.getProperty("os.name");
		}
		return os.startsWith("Linux");
	}
	
	public static boolean isMac() {
		if (os == null) {
			os = System.getProperty("os.name");
		}
		return os.startsWith("Mac");
	}
}