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
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.MyGame;

public class GameScreen implements Screen {
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    
    private Model wallModel;
    private Array<ModelInstance> instances;

    public GameScreen(MyGame game) {
        // Настройка 3D камеры (FPS)
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 1.5f, 0f);
        camera.lookAt(0f, 1.5f, -1f);
        camera.near = 0.1f;
        camera.far = 300f;
        camera.update();

        modelBatch = new ModelBatch();

        // Освещение
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // Генерация бесконечного коридора из блоков
        ModelBuilder modelBuilder = new ModelBuilder();
        wallModel = modelBuilder.createBox(2f, 4f, 2f,
                new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(Color.GRAY)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        instances = new Array<ModelInstance>();

        // Строим стены вдоль коридора
        for (int z = 0; z > -200; z -= 2) {
            ModelInstance leftWall = new ModelInstance(wallModel, -3f, 2f, z);
            ModelInstance rightWall = new ModelInstance(wallModel, 3f, 2f, z);
            instances.add(leftWall);
            instances.add(rightWall);
        }
    }

    @Override
    public void render(float delta) {
        // Движение камеры вперед (симуляция ходьбы по коридору)
        camera.position.z -= 5f * delta;
        camera.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    @Override public void resize(int width, int height) { camera.viewportWidth = width; camera.viewportHeight = height; camera.update(); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        modelBatch.dispose();
        wallModel.dispose();
    }
}
