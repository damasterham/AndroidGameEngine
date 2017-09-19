package damasterham.test2;

public class TouchEvent
{
    public enum TouchEventType
    {
        DOWN,
        UP,
        DRAG
    }

    public TouchEventType type;
    public int x;
    public int y;
    public int pointer;
}
