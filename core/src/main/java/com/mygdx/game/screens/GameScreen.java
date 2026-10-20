package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen implements Screen {
    private final String playerName;
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private Array<ModelInstance> instances;
    private Model floorModel, wallModel, ceilingModel, lightModel;
    
    private Stage hudStage;
    private Skin skin;
    private Touchpad touchpad;
    
    private Vector3 playerPos = new Vector3(0, 1.5f, 0);

    public GameScreen(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void show() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.1f;
        camera.far = 100f;

        ModelBuilder modelBuilder = new ModelBuilder();
        
        // 1. Пол: тёмно-оранжевый
        floorModel = modelBuilder.createBox(10f, 0.1f, 60f, 
            new com.badlogic.gdx.graphics.g3d.material.Material(ColorAttribute.createDiffuse(new Color(0.75f, 0.35f, 0.05f, 1f))),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        
        // 2. Стены: тёмно-серые
        wallModel = modelBuilder.createBox(0.5f, 5f, 60f,
            new com.badlogic.gdx.graphics.g3d.material.Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // 3. Потолок: светло-серый
        ceilingModel = modelBuilder.createBox(10f, 0.1f, 60f,
            new com.badlogic.gdx.graphics.g3d.material.Material(ColorAttribute.createDiffuse(Color.LIGHT_GRAY)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // Источники света на потолке
        lightModel = modelBuilder.createBox(0.8f, 0.15f, 0.8f,
            new com.badlogic.gdx.graphics.g3d.material.Material(ColorAttribute.createDiffuse(Color.YELLOW), ColorAttribute.createEmissive(Color.YELLOW)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        instances = new Array<>();
        instances.add(new ModelInstance(floorModel, 0, 0, -30f));
        instances.add(new ModelInstance(wallModel, -5f, 2.5f, -30f));
        instances.add(new ModelInstance(wallModel, 5f, 2.5f, -30f));
        instances.add(new ModelInstance(ceilingModel, 0, 5f, -30f));

        // Расставляем периодические источники света по потолку
        for (float z = -5f; z > -55f; z -= 10f) {
            instances.add(new ModelInstance(lightModel, 0, 4.92f, z));
            environment.add(new PointLight().set(1f, 0.95f, 0.8f, 0, 4.3f, z, 12f));
        }

        // --- HUD ИНТЕРФЕЙС ---
        hudStage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Сенсорный стик слева
        touchpad = new Touchpad(10, skin);
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.add(touchpad).size(160, 160).left().bottom().pad(25);
        rootTable.add().expand();
        hudStage.addActor(rootTable);

        // Прицел в центре экрана
        Label crosshair = new Label("+", skin);
        crosshair.setFontScale(2.5f);
        Table centerTable = new Table();
        centerTable.setFillParent(true);
        centerTable.add(crosshair);
        hudStage.addActor(centerTable);

        // Нижняя панель персонажа (Имя, HP - красный, Броня - тёмно-зелёный)
        Table panelTable = new Table();
        panelTable.setBackground(skin.newDrawable("white", new Color(0.1f, 0.1f, 0.1f, 0.75f)));
        
        Label nameLbl = new Label("Игрок: " + playerName, skin);
        Label hpLbl = new Label(" HP: 100 ", skin);
        hpLbl.setColor(Color.RED);
        Label armorLbl = new Label(" Броня: 100 ", skin);
        armorLbl.setColor(new Color(0f, 0.45f, 0.15f, 1f)); // Тёмно-зелёный

        panelTable.add(nameLbl).pad(12);
        panelTable.add(hpLbl).pad(12);
        panelTable.add(armorLbl).pad(12);

        Table bottomContainer = new Table();
        bottomContainer.setFillParent(true);
        bottomContainer.bottom().add(panelTable).padBottom(15);
        hudStage.addActor(bottomContainer);

        Gdx.input.setInputProcessor(hudStage);
    }

    @Override
    public void render(float delta) {
        // Управление ходьбой по стику
        float moveY = touchpad.getKnobPercentY();
        float speed = 6f * delta;
        playerPos.z -= moveY * speed; // движение вперед/назад по коридору

        // Обновление камеры от первого лица
        camera.position.set(playerPos);
        camera.direction.set(0, 0, -1);
        camera.up.set(0, 1, 0);
        camera.update();

        // Отрисовка 3D мира
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.04f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();

        // Отрисовка UI
        hudStage.act(delta);
        hudStage.draw();
    }

    @Override public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        hudStage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        modelBatch.dispose();
        floorModel.dispose();
        wallModel.dispose();
        ceilingModel.dispose();
        lightModel.dispose();
        hudStage.dispose();
        skin.dispose();
    }
}
