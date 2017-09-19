package damasterham.test2;

import android.view.MotionEvent;
import android.view.View;

import java.util.List;

/**
 * Created by DaMasterHam on 12-09-2017.
 */

public class MultiTouchHandler implements TouchHandler, View.OnTouchListener
{
    private boolean[] isTouched = new boolean[20];
    private int[] touchX = new int[20];
    private int[] touchY = new int[20];
    private List<TouchEvent> touchEventBuffer;
    private TouchEventPool touchEventPool;

    public MultiTouchHandler(View view, List<TouchEvent> touchEventBuffer, TouchEventPool touchEventPool)
    {
        view.setOnTouchListener(this);
        this.touchEventBuffer = touchEventBuffer;
        this.touchEventPool = touchEventPool;
    }

    @Override
    public boolean isTouchDown(int pointer)
    {
        return isTouched[pointer];
    }

    @Override
    public int getTouchX(int pointer)
    {
        return 0;
    }

    @Override
    public int getTouchY(int pointer)
    {
        return 0;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        TouchEvent touchEvent = null;
        int action = event.getAction() & MotionEvent.ACTION_MASK; // Bitwise, probably to remove unwanted actions;
        int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        int pointerId = event.getPointerId(pointerIndex);

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                setTouchEvent(event, pointerId, TouchEvent.TouchEventType.DOWN,true);
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                setTouchEvent(event, pointerId, TouchEvent.TouchEventType.UP,true);
            break;
            case MotionEvent.ACTION_MOVE:
                int pointerCount = event.getPointerCount();
                synchronized (touchEventBuffer)
                {
                    for (int i = 0; i < pointerCount; i++)
                    {
                        setTouchEvent(event, pointerId, TouchEvent.TouchEventType.DRAG, true);
                    }
                }

            break;
        }

        return false;
    }

    private void setTouchEvent(MotionEvent event, int pointerId, TouchEvent.TouchEventType type,  boolean isTouched)
    {
        TouchEvent touchEvent;
        touchEvent = touchEventPool.obtain();
        touchEvent.type = TouchEvent.TouchEventType.DOWN;
        touchEvent.pointer = pointerId;
        touchEvent.x = (int) event.getX(touchEvent.pointer);
        touchX[pointerId] = touchEvent.x;
        touchEvent.y = (int) event.getY(touchEvent.pointer);
        touchY[pointerId] = touchEvent.y;
        this.isTouched[pointerId] = isTouched;
        synchronized (touchEventBuffer)
        {
            touchEventBuffer.add(touchEvent);
        }
    }
}
