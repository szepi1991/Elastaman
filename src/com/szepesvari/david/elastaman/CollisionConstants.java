package com.szepesvari.david.elastaman;


public class CollisionConstants {

	/* The categories. */
	public static final short CATEGORYBIT_GROUND = 1;
	public static final short CATEGORYBIT_PLAYER = 2;
	public static final short CATEGORYBIT_BULLET = 4;

	/* And what should collide with what. */
	public static final short MASKBITS_GROUND = Short.MAX_VALUE; // collide with everything
	public static final short MASKBITS_BULLET = CATEGORYBIT_GROUND; // just the ground
	public static final short MASKBITS_PLAYER = Short.MAX_VALUE - CATEGORYBIT_BULLET; // everything but bullet 
//	public static final short MASKBITS_BOX = CATEGORYBIT_WALL + CATEGORYBIT_BOX; // Missing: CATEGORYBIT_CIRCLE
//	public static final short MASKBITS_CIRCLE = CATEGORYBIT_WALL + CATEGORYBIT_CIRCLE; // Missing: CATEGORYBIT_BOX

//	public static final FixtureDef WALL_FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f, false, CATEGORYBIT_WALL, MASKBITS_WALL, (short)0);
//	public static final FixtureDef BOX_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, false, CATEGORYBIT_BOX, MASKBITS_BOX, (short)0);
//	public static final FixtureDef CIRCLE_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, false, CATEGORYBIT_CIRCLE, MASKBITS_CIRCLE, (short)0);

	
}
