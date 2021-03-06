package br.com.event.custom.view;

import android.view.MotionEvent;

public class EclairMotionEvent extends WrapMotionEvent {

	protected EclairMotionEvent(MotionEvent event) {
		super(event);
	}

	@Override
	public float getX(int pointerIndex) {
		return event.getX(pointerIndex);
	}

	@Override
	public float getY(int pointerIndex) {
		return event.getY(pointerIndex);
	}

	@Override
	public int getPointerCount() {
		return event.getPointerCount();
	}

	@Override
	public int getPointerId(int pointerIndex) {
		return event.getPointerId(pointerIndex);
	}
}
