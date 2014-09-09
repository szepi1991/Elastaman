package com.szepesvari.david.elastaman;

import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.shape.RectangularShape;
import org.andengine.input.touch.TouchEvent;

public abstract class Carryable implements ITouchArea {
	
	private boolean held = false;
	RectangularShape shape;
	
	public void grab() { held = true; }
	public void letGo() { held = false; }
	public boolean isHeld() { return held; }
	
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, 
			final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		letGo();
		return true;
	}

}
