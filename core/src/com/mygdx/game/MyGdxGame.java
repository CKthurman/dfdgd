package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Random;

import static com.badlogic.gdx.graphics.Color.BLUE;
import static utils.Constants.PPM;

public class MyGdxGame extends ApplicationAdapter {

	private boolean DEBUG = false;
	private final float SCALE = 2.0f;
	private OrthographicCamera camera;
	private Box2DDebugRenderer b2dr;
	private World world;
	private Body player, platform;
	private float gravity = -9.8f;
	private SpriteBatch batch;
	private Texture tex, rect;
	private ShapeRenderer shapeRenderer;
	private int rectW, rectH;
	private Rectangle rect1;
	private ArrayList<Rectangle> rects;
	private Random random;


	@Override
	public void create () {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, w/SCALE, h/SCALE);

		world = new World(new Vector2(0, gravity), false);
		b2dr = new Box2DDebugRenderer();

		player = createBox(0, 60,17, 32, false);
		platform = createBox(0, 30,100, 20, true);

		random = new Random();

		batch = new SpriteBatch();
		tex = new Texture("wizard.png");
		rect = new Texture("rect.png");
		shapeRenderer = new ShapeRenderer();

		rectW = 300;
		rectH = 20;

		rects = new ArrayList<>();

		rect1 = new Rectangle(30, 30, 100, 10, BLUE);

		generateRects();
		printRects();




		//rects.add(rect1);



	}

	@Override
	public void render () {
		update(Gdx.graphics.getDeltaTime());



		//Render
		ScreenUtils.clear(0, .5f, .67f, 1);

		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
			generateRects();
			printRects();
		}


		/// Drawing froim the batch :)
		batch.begin();

		batch.draw(tex, player.getPosition().x * PPM - (tex.getWidth()/2f), player.getPosition().y * PPM - (tex.getHeight()/2f));
		batch.draw(rect, platform.getPosition().x* PPM - (rect.getWidth()/2f), platform.getPosition().y * PPM - (rect.getHeight()/2f));

		for(Rectangle rect : rects){
			rect.draw(batch, 1);
		}

		batch.end();

		b2dr.render(world, camera.combined.scl(PPM));

		if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
	}

	@Override
	public void resize(int width, int height){
		camera.setToOrtho(false, width / SCALE, height / SCALE);
	}
	
	@Override
	public void dispose () {
		world.dispose();
		b2dr.dispose();
		batch.dispose();
	}

	private void update(float delta) {
		world.step(1/60f, 6, 2);
		
		inputUpdate(delta);
		cameraUpdate(delta);


		batch.setProjectionMatrix(camera.combined);
	}

	private void inputUpdate(float delta) {
		int horizontalForce = 0;

		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
			horizontalForce -= 1;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
			horizontalForce += 1;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.UP) & Math.abs(player.getLinearVelocity().y) <= 0){
			player.applyForceToCenter(0, 150, false);
		}

		player.setLinearVelocity(horizontalForce * 5, player.getLinearVelocity().y);
	}

	private void cameraUpdate(float delta) {

		//Camera centers on player box
		Vector3 position = camera.position;
		position.x = player.getPosition().x * PPM;
		position.y = player.getPosition().y * PPM;
		camera.position.set(position);

		camera.update();
	}
	private Body createBox(int x , int y, int width, int height, boolean isStatic) {
		//Create and initialize the body entity
		Body pBody = null;
		BodyDef def = new BodyDef();

		if(isStatic)
			def.type = BodyDef.BodyType.StaticBody;
		else
			def.type = BodyDef.BodyType.DynamicBody;

		def.position.set(x/PPM,y/PPM);
		def.fixedRotation = true;
		pBody = world.createBody(def);


		// Create the body Shape
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width/ 2 / PPM, height/2/PPM);

		//Apply the shape to the body
		pBody.createFixture(shape, 1.0f);
		shape.dispose();

		return pBody;
	}

	private BodyDef createDef(int x , int y, int width, int height, boolean isStatic){
		//Create and initialize the body entity
		BodyDef bodyDef = new BodyDef();

		if(isStatic)
			bodyDef.type = BodyDef.BodyType.StaticBody;
		else
			bodyDef.type = BodyDef.BodyType.DynamicBody;

		bodyDef.position.set(x/PPM,y/PPM);
		bodyDef.fixedRotation = true;


		// Create the body Shape
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width/ 2 / PPM, height/2/PPM);

		//Apply the shape to the body
		shape.dispose();

		return bodyDef;
	} // come back to this

	private ArrayList<Rectangle> generateRects(){
		Array<Body> bodies = new Array<Body>();
		world.getBodies(bodies);
		for(Body b : bodies){
			if(b != player & b != platform) {
				world.destroyBody(b);
			}
		}
		rects.clear();
		for(int i = 0; i < 10; i++){
			float boxX = (player.getPosition().x * PPM) -30 ;//random.nextInt((int) (player.getPosition().x-200f), (int) (player.getPosition().x+200f));
			float boxY = (player.getPosition().y * PPM) -26;//(int) (player.getPosition().y-100);//random.nextInt((int) (player.getPosition().y), (int) (player.getPosition().y));
			int boxW = 50;
			int boxH = 10;
			System.out.println("boxes: " + boxX + ", " + boxY);
			System.out.println("PLayer: " + player.getPosition().x + ", " + player.getPosition().y);
			rects.add(new Rectangle(boxX, boxY, boxW, boxH, BLUE));
		}
		return rects;
	}

	private void printRects(){
		ArrayList<Body> bodies = new ArrayList<>();
		for (Rectangle rect : rects){
			Body plat = createBox((int) ((int) rect.getX()+rect.getWidth()/2), (int) ((int) rect.getY()+rect.getHeight()/2), (int) rect.getWidth(), (int) rect.getHeight(), true );
			bodies.add(plat);
		}
	}

}
