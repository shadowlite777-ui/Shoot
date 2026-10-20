package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.MyGame;

public class MenuScreen implements Screen {
    private final MyGame game;
    private Stage stage;
    private Skin skin;
    private TextField nameField;

    public MenuScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public view show() {} // standard libgdx show method

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("Введите имя персонажа:", skin);
        nameField = new TextField("Игрок", skin);
        TextButton startButton = new TextButton("Начать игру", skin);

        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String playerName = nameField.getText();
                if (playerName.isEmpty()) {
                    playerName = "Игрок";
                }
                game.setScreen(new GameScreen(playerName));
            }
        });

        table.add(titleLabel).padBottom(10).row();
        table.add(nameField).width(250).padBottom(20).row();
        table.add(startButton).width(160).height(50);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
