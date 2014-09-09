package com.szepesvari.david.elastaman;

import static org.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

import java.util.Random;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.primitive.DrawMode;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Mesh;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.shape.IAreaShape;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.debugdraw.DebugRenderer;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.DrawType;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;

import android.hardware.SensorManager;

public class GameActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener {

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;
	
	private Scene mScene;
	private IEntity backg;
	private int mFaceCount = 0;
	private PhysicsWorld mPhysicsWorld;
	
	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private static final FixtureDef GROUND_FIXTURE = PhysicsFactory.createFixtureDef(
			0, 0.5f, 0.5f, false, 
			CollisionConstants.CATEGORYBIT_GROUND, 
			CollisionConstants.MASKBITS_GROUND, (short) 0);
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, 
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	protected void onCreateResources() {
		
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0, 0, 0));
		this.mScene.setOnSceneTouchListener(this);
		backg = new Entity();
		IEntity foreg = new Entity();
		this.mScene.attachChild(backg);
		this.mScene.attachChild(foreg);
		
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

//		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
//		final Rectangle ground = new Rectangle(50, CAMERA_HEIGHT - 50, CAMERA_WIDTH-100, 2, vertexBufferObjectManager);
////		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
////		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
////		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
////
//		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
//		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
////		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
////		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
////		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
////
//		backg.attachChild(ground);
////		this.mScene.attachChild(roof);
////		this.mScene.attachChild(left);
////		this.mScene.attachChild(right);

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		
		Random r = new Random(13);
		createTerrain(this.mPhysicsWorld, backg, r);
//		experiments();
		Player pl = new Player(200, 150, foreg, mEngine, mPhysicsWorld, getVertexBufferObjectManager());
		this.mScene.setOnSceneTouchListener(pl);
		pl.registerOnAreaTouchListener(this.mScene);

		
//		DebugRenderer debug = new DebugRenderer(mPhysicsWorld, getVertexBufferObjectManager());
//		foreg.attachChild(debug);

		return this.mScene;
	}

	
	private void experiments() {
		// we'll use this multiple times so let's save a reference here
		VertexBufferObjectManager vboMan = this.getVertexBufferObjectManager();
		final Rectangle box1 = new Rectangle(300, 200, 32, 32, vboMan);
		final Rectangle box2 = new Rectangle(360, 200, 32, 32, vboMan);
		
		FixtureDef fixture = PhysicsFactory.createFixtureDef(1, 0.1f, 0.8f);
		Body body1 = PhysicsFactory.createBoxBody(mPhysicsWorld, box1, BodyType.DynamicBody, fixture);
		Body body2 = PhysicsFactory.createBoxBody(mPhysicsWorld, box2, BodyType.DynamicBody, fixture);
		
		final DistanceJointDef distJointdef = new DistanceJointDef();
		distJointdef.initialize(body1, body2, 
				body1.getWorldCenter().add(new Vector2(0.8f, 0)), body2.getWorldCenter());
		distJointdef.collideConnected = true;
		distJointdef.frequencyHz = 1;
		distJointdef.dampingRatio = 0.8f;
		mPhysicsWorld.createJoint(distJointdef);

	}

	/** 
	 * Creates a top layer to the cave.. for now simply camera wide and height.
	 * It is built from trapezoids, so there will be no issue with convexity.
	 */
	private void createTerrain(final PhysicsWorld pPhysicsWorld, IEntity attachTo, final Random rand) {
		
		// we'll use this multiple times so let's save a reference here
		VertexBufferObjectManager vboMan = this.getVertexBufferObjectManager();
		
		// some setup
		final float stepSize = 50;
		float dYTop = 0;
		float dYBot = 0;
		
		final int numPoints = (int) Math.ceil(CAMERA_WIDTH / stepSize) + 1;
		
		// generating the sequence of y values that defines the top and bottom terrains
		float[] topTerrain = new float[numPoints];
		float[] botTerrain = new float[numPoints];
		topTerrain[0] = 50;
		botTerrain[0] = CAMERA_HEIGHT - 100;
		for (int i = 1; i < numPoints; ++i) {
			dYTop += (rand.nextFloat()-0.5)*30;
			topTerrain[i] = topTerrain[i-1]+dYTop;
			if (topTerrain[i] < 5) {
				topTerrain[i] = 5.0f;
				dYTop = 0;
			}
			
			dYBot += (rand.nextFloat()-0.5)*30;
			botTerrain[i] = botTerrain[i-1]+dYBot;
			if (botTerrain[i] > CAMERA_HEIGHT - 5) {
				botTerrain[i] = CAMERA_HEIGHT - 5.0f;
				dYBot = 0;
			}
		}
		
		// create the graphics for the top and the bottom
		float colGreen = Color.GREEN_ABGR_PACKED_FLOAT;
		/* we use a trianglestrip with this ordering (the example is for the top)
			1   3   5
				4
			2
					6
		*/
		float[] topPointData = new float[numPoints * 2 * 3]; // each point has x,y,color
		float[] botPointData = new float[numPoints * 2 * 3]; // each point has x,y,color
		for (int i = 0; i < numPoints; ++i) {
			// top point
			topPointData[i*6] = i*stepSize;
			topPointData[i*6+1] = 0;
			topPointData[i*6+2] = colGreen;
			// bottom point
			topPointData[i*6+3] = i*stepSize;
			topPointData[i*6+4] = topTerrain[i];
			topPointData[i*6+5] = colGreen;
			
			// bottom point
			botPointData[i*6] = i*stepSize;
			botPointData[i*6+1] = CAMERA_HEIGHT;
			botPointData[i*6+2] = colGreen;
			// top point
			botPointData[i*6+3] = i*stepSize;
			botPointData[i*6+4] = botTerrain[i];
			botPointData[i*6+5] = colGreen;
			
		}
		final Mesh topGround = new Mesh(0, 0, topPointData, numPoints * 2, 
				DrawMode.TRIANGLE_STRIP, vboMan, DrawType.STATIC);
		final Mesh botGround = new Mesh(0, 0, botPointData, numPoints * 2, 
				DrawMode.TRIANGLE_STRIP, vboMan, DrawType.STATIC);
		attachTo.attachChild(topGround);
		attachTo.attachChild(botGround);
		
		
		// now we create the needed trapezoids (it would be more efficient to do 
		// this in the previous step, but this way it's separated spatially)
		for (int i = 1; i < numPoints; ++i) {
			// a bounding box so that we have something to pass to Box2D
			// the only solution I've seen used this..
			final float topHeight = Math.max(topTerrain[i-1], topTerrain[i]);
			final float botHeight = CAMERA_HEIGHT - Math.min(botTerrain[i-1], botTerrain[i]);
			
			final float halfWidth = stepSize / 2 / PIXEL_TO_METER_RATIO_DEFAULT;
			final float tHalfHeight = topHeight / 2 / PIXEL_TO_METER_RATIO_DEFAULT;
			final float bHalfHeight = botHeight / 2 / PIXEL_TO_METER_RATIO_DEFAULT;
			final Rectangle topPolyBoundingBox = new Rectangle(
					(i-1)*stepSize, 1, stepSize, topHeight, vboMan);
			final Rectangle botPolyBoundingBox = new Rectangle(
					(i-1)*stepSize, CAMERA_HEIGHT-botHeight-1, stepSize, botHeight, vboMan);
			
			// needs to be relative to the center, in meters
			final Vector2[] topVertices = {
				new Vector2(-halfWidth, -tHalfHeight),
				new Vector2(halfWidth, -tHalfHeight),
				new Vector2(halfWidth, (topTerrain[i] / PIXEL_TO_METER_RATIO_DEFAULT) - tHalfHeight),
				new Vector2(-halfWidth, (topTerrain[i-1] /  PIXEL_TO_METER_RATIO_DEFAULT) - tHalfHeight),
			};
			final Vector2[] botVertices = {
				new Vector2(-halfWidth, bHalfHeight),
				new Vector2(-halfWidth, bHalfHeight +
						((-CAMERA_HEIGHT+botTerrain[i-1]) /  PIXEL_TO_METER_RATIO_DEFAULT)),
				new Vector2(halfWidth, bHalfHeight +
						((-CAMERA_HEIGHT+botTerrain[i]) /  PIXEL_TO_METER_RATIO_DEFAULT)),
				new Vector2(halfWidth, bHalfHeight),
			};
			
//			System.out.println("BotTrapezoid:");
//			for (Vector2 vec : botVertices) { System.out.println(vec); }
			
			Body body;
			body = PhysicsFactory.createPolygonBody(pPhysicsWorld, topPolyBoundingBox, topVertices, 
					BodyType.StaticBody, GROUND_FIXTURE);
			body.setUserData("ground");
			body = PhysicsFactory.createPolygonBody(pPhysicsWorld, botPolyBoundingBox, botVertices, 
					BodyType.StaticBody, PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f));
			body.setUserData("ground");
		}
		
	}
	
	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
//		if(this.mPhysicsWorld != null) {
//			if(pSceneTouchEvent.isActionDown()) {
//				this.addFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), backg);
//				return true;
//			}
//		}
		return false;
	}

	private void addFace(final float pX, final float pY, IEntity attachTo) {
		this.mFaceCount++;
		Debug.d("Faces: " + this.mFaceCount);

		final IAreaShape face;
		final Body body;

		if(this.mFaceCount % 2 == 0) {
			face = new Rectangle(pX, pY, 32, 32, this.getVertexBufferObjectManager());
			face.setColor(new Color(0, 0, 1));
			body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
		} else {
			face = new Rectangle(pX, pY, 32, 32, this.getVertexBufferObjectManager());
			face.setColor(new Color(1, 0, 0));
			body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
		}
		
		attachTo.attachChild(face);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
	}

}
