package damasterham.test2;

public interface TouchHandler
{
    boolean isTouchDown(int pointer);
    int getTouchX(int pointer);
    int getTouchY(int pointer);
}
