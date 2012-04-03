package net.shortround.rose;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class RoseView extends View {

	public static final int MIN_DECAY = 0;
	public static final int MAX_DECAY = 14;
	
	private static final float ROSE_WIDTH = 575.0f;
	private static final float ROSE_HEIGHT = 525.0f;
	
	private boolean animating;
	
	private int battery;
	private int decay;
	private float scale;
	
	private ArrayList<Bitmap> roses;
	
	public RoseView(Context context) {
		super(context);
		
		// Set some defaults
		animating = false;
		battery = 0;
		decay = 0;
		scale = 1.0f;
		
		// Build the roses
		roses = new ArrayList<Bitmap>();
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_01));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_02));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_03));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_04));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_05));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_06));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_07));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_08));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_09));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_10));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_11));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_12));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_13));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_14));
		roses.add(BitmapFactory.decodeResource(getResources(), R.drawable.rose_15));
		
		// Keep the screen on
		setKeepScreenOn(true);
	}
	
	/*** External Commands ***/
	public void decay() {
		// Fail if we're animating
		if (animating) {
			return;
		}
		
		// Check the decay level
		if (decay >= MAX_DECAY) {
			return;
		}
		
		// Just set the decay
		decay += 1;
		invalidate();
	}
	
	public void revert() {
		// Fail if we're animating
		if (animating) {
			return;
		}
				
		// Check the decay level
		if (decay <= MIN_DECAY) {
			return;
		}
		
		// Just set the decay
		decay -= 1;
		invalidate();
	}
	
	/*** View Callbacks ***/
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// Save state
		canvas.save();

		// Draw the background
		Paint backgroundPaint = new Paint();
		backgroundPaint.setStyle(Paint.Style.FILL);
		backgroundPaint.setColor(Color.BLACK);
		canvas.drawPaint(backgroundPaint);

		// Set the proper offset and scale
		canvas.translate(canvas.getWidth() / 2, canvas.getHeight());
		canvas.scale(scale, scale);
				
		// Draw the appropriate rose
		Bitmap rose = roses.get(decay);
		canvas.drawBitmap(rose, rose.getWidth() / -2, rose.getHeight() * -1, null);
				
		// Restore state
		canvas.restore();
	}
	
	@Override
	protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
		// Set up a new scale
		float scaleX = (float) width / ROSE_WIDTH;
		float scaleY = (float) height / ROSE_HEIGHT;
		
		scale = scaleX > scaleY ? scaleY : scaleX;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// A touch has ended, split the screen and change the decay for the stem
			if (event.getX() < getWidth() / 2.0f) {
				revert();
			} else {
				decay();
			}
			
			return true;
		}

		// The event wasn't handled, so return false
		return false;
	}
	
	/*** Properties ***/
	
	public int getBattery() {
		return battery;
	}
	
	public int getDecay() {
		return decay;
	}
	
	public void setBattery(int value) {
		battery = value;
	}

}
