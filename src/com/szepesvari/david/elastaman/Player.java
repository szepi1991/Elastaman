package com.szepesvari.david.elastaman;

import java.util.concurrent.LinkedBlockingQueue;

import org.andengine.engine.Engine;
import org.andengine.entity.IEntity;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.shape.RectangularShape;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;

public class Player implements IOnSceneTouchListener {

	private static final FixtureDef TENTACLE_FIXTURE = 
			PhysicsFactory.createFixtureDef(0, 0, 0, 
					false, CollisionConstants.CATEGORYBIT_BULLET, 
					CollisionConstants.MASKBITS_BULLET, (short) 0);
	
	private float width = 40;
	private float height = 40;
	private int maxTentacles = 3;
	private float shootSpeed = 100; // in m/s
	private LinkedBlockingQueue<Tentacle> tentacles = 
							new LinkedBlockingQueue<Tentacle>(maxTentacles);
	
	private final Engine mEngine;
	private final VertexBufferObjectManager vboMan;
	private final PhysicsWorld physicsWorld;
	private final IEntity attachTo;
	
	private Color bodyCol = Color.WHITE;

	/** In pixel coordinates!! */
	public static float TENTACLE_MAX_LENGTH = 100.0f;
	
	private RectangularShape entity;
	private Body bodyBody;
	/** Handles all collisions that a Bullet may have. */
	private ContactListener tentacleListener = new ContactListener() {

		@Override
		public void beginContact(final Contact contact) {
			final Fixture x1 = contact.getFixtureA();
			final Fixture x2 = contact.getFixtureB();
			
			final Body other;
			final Bullet b;
			if (x1.getBody().getUserData() instanceof Bullet) {
				b = (Bullet) x1.getBody().getUserData();
				other = x2.getBody();
			} else if (x2.getBody().getUserData() instanceof Bullet) {
				b = (Bullet) x2.getBody().getUserData();
				other = x1.getBody();
			} else {
				return; // not interested
			}
			
			if (other == null || other.getUserData() == null) return; // can't do anything
			
			if (other.getUserData().equals("ground")) {
				
				Vector2[] contactPoints = contact.getWorldManifold().getPoints();
				Log.d("CONTACT", "ground - bullet at ");
				for (Vector2 p : contactPoints) { Log.d("  -c-", p.toString()); }
				Log.d("CONTACT", "bullet location: " + b.body.getWorldCenter().toString());
				
				b.reachedGround(other, contactPoints[0]);
			}
		}

		@Override
		public void endContact(Contact contact) {}
		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {}
		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {}
		
	};
	
	public Player(float x, float y, IEntity attachTo, Engine engine, PhysicsWorld physicsWorld,
						VertexBufferObjectManager pVertexBufferObjectManager) {
		this.physicsWorld = physicsWorld;
		this.attachTo = attachTo;
		this.mEngine = engine;
		this.vboMan = pVertexBufferObjectManager;
		this.entity = new Rectangle(x - width/2, y - height/2, width, height, 
				pVertexBufferObjectManager) {
		    @Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, 
		    				float pTouchAreaLocalX, float pTouchAreaLocalY) {
		    	// remove all tentacles
				mEngine.runOnUpdateThread(new Runnable() 
				{
					@Override
					public void run() {
				    	while (!tentacles.isEmpty()) { tentacles.poll().destroy(); }
					}
				});

		    	// consume touch
		    	return true;
		    }
		};
		this.entity.setColor(bodyCol);
		
		bodyBody = PhysicsFactory.createBoxBody(physicsWorld, entity, 
				BodyType.DynamicBody, PhysicsFactory.createFixtureDef(
						5.1f, 0.05f, 0.8f, false,
						CollisionConstants.CATEGORYBIT_PLAYER, 
						CollisionConstants.MASKBITS_PLAYER, (short) 0));
		bodyBody.setAngularDamping(10.5f);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(entity, bodyBody, true, true));
		
		attachTo.attachChild(entity);
		shootTentacle(new Vector2(0, 1));
		shootTentacle(new Vector2(0, -1));
		
		physicsWorld.setContactListener(tentacleListener);
	}
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(pSceneTouchEvent.isActionDown()) {
			// direction will be from current position to touch position
			shootTentacle(new Vector2(
					pSceneTouchEvent.getX() - (entity.getX() + entity.getWidth()/2), 
					pSceneTouchEvent.getY() - (entity.getY() + entity.getHeight()/2)));
			// TODO not sure if adding height and width here is appropriate due to rotations??
			return true;
		}
		return false;
	}
	
	public void registerOnAreaTouchListener(Scene onto) {
		onto.registerTouchArea(entity);
	}

	/**
	 * Shoot a new tentacle. If we reached the max number of tentacles and the
	 * new one hangs on to something, remove the oldest tentacle.
	 * @param dir		Direction in radians.
	 */
	private void shootTentacle(Vector2 dir) {
		new Bullet(entity.getX()+entity.getWidth()/2, entity.getY()+entity.getHeight()/2, dir);
	}
	
	// TODO use a pool later + kill bullets that leave the screen
	public class Bullet {
		Rectangle shape;
		Body body;
		boolean exists = true;
		
		/** (dx, dy) doesn't need to be normalized. */
		public Bullet(float x, float y, Vector2 dir) {
			dir.nor();
			
			shape = new Rectangle(x, y, 2, 2, vboMan);
			shape.setColor(bodyCol);
			body = PhysicsFactory.createBoxBody(physicsWorld, shape, BodyType.DynamicBody, TENTACLE_FIXTURE);
			body.setUserData(this);
			// shoot something in the given direction at a high speed:
			body.setLinearVelocity(shootSpeed* (float) dir.x, 
									shootSpeed * (float) dir.y);
			physicsWorld.registerPhysicsConnector(new PhysicsConnector(shape, body, true, true));
			
			attachTo.attachChild(shape);
		}

		/** Must be called on the updateThread!! */
		void die() {
			final PhysicsConnector physicsConnector =
					physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(shape);
			if (physicsConnector == null) return;
			
			Debug.d("bullet", "Getting killed now");
			physicsWorld.unregisterPhysicsConnector(physicsConnector);
			body.setActive(false);
			physicsWorld.destroyBody(body);
			attachTo.detachChild(shape);
			
			exists = false;
		}
		
		void reachedGround(final Body other, final Vector2 collidePoint) {

			Debug.d("bullet", "Registering tentacle to " + collidePoint.toString());
			
			// add spring
			mEngine.runOnUpdateThread(new Runnable() 
			{
			    @Override
			    public void run() 
			    {
			    	if (exists == false) return;
			    	
					// remove last spring
					if (tentacles.size() >= maxTentacles) {
			    		tentacles.poll().destroy();
					}
					
					// create elastic rope to this position from entity
					final DistanceJointDef distJointdef = new DistanceJointDef();
					distJointdef.initialize(bodyBody, other, 
//							bodyBody.getWorldCenter(), b.body.getWorldCenter());
							bodyBody.getWorldCenter(), collidePoint);
					if (distJointdef.length < 0.2f) { return; }
					distJointdef.length = 0.8f; // make it short
					distJointdef.collideConnected = true; 
					distJointdef.frequencyHz = 1;
					distJointdef.dampingRatio = 0.8f;
					try {
						Debug.d("bullet", "Creating new tentacle to " + collidePoint.toString());
						tentacles.put(new Tentacle(
								physicsWorld.createJoint(distJointdef), 
								collidePoint) );
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			    	
					// now kill the bullet
					die();
			    }
			});
		}
	}
	
	public static boolean isTentacleTooLong(float x1, float y1, float x2, float y2) {
		return (TENTACLE_MAX_LENGTH > ((x1-x2)*(x1-x2) - (y1-y2)*(y1-y2)));
	}
	
	/** Keeps track of (Joint, PhysicsConnector, visible line) corresponding to
	 * one tentacle. */
	private class Tentacle {
		private Joint joint;
		private PhysicsConnector connector;
		private Line connectionLine;
		
		public Tentacle(Joint j, Vector2 collidePoint) {
			joint = j;
			
			// add graphics for the line (ground --> body)
			final Vector2 movingWorldCent = bodyBody.getWorldCenter();
			connectionLine = new Line(
					collidePoint.x * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 
					collidePoint.y * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 
					movingWorldCent.x * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 
					movingWorldCent.y * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
					vboMan);
			attachTo.attachChild(connectionLine);
			
			connector = new PhysicsConnector(entity, bodyBody, true, true) {
				@Override
				public void onUpdate(final float pSecondsElapsed) {
					super.onUpdate(pSecondsElapsed);
					final Vector2 playerWorldCent = bodyBody.getWorldCenter();
					// or just use entity.getX(), entity.getY() + centerize??
					float px = playerWorldCent.x * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
					float py = playerWorldCent.y * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
//					if (isTentacleTooLong(
//							connectionLine.getX1(), connectionLine.getY1(), 
//							px, py)) {
//						destroy();
//					}
					connectionLine.setPosition(
							connectionLine.getX1(), connectionLine.getY1(), px, py);
				}
			};
					
			physicsWorld.registerPhysicsConnector(connector);
		}
		
		public void destroy() {
			physicsWorld.unregisterPhysicsConnector(connector);
			physicsWorld.destroyJoint(joint);
			attachTo.detachChild(connectionLine);
		}
	}

	
}
