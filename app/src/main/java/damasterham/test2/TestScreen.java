package damasterham.test2;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.Random;

public class TestScreen extends Screen
{
    private String name = this.getClass().getName();
    private Random random;
    private Bitmap bob;


    public TestScreen(GameEngine gameEngine)
    {
        super(gameEngine);
        random = new Random();
        bob = gameEngine.loadBitmap("bob.png");
    }

    @Override
    public void update(float deltaTime)
    {
        gameEngine.clearFrameBuffer(Color.GRAY);
        gameEngine.drawBitmap(bob, 500, 300);
        //Log.d(name, ": UPDATE");
    }

    @Override
    public void pause()
    {
        Log.d(name, ": PAUSE");

    }

    @Override
    public void resume()
    {
        Log.d(name, ": RESUME");
    }

    @Override
    public void dispose()
    {
        Log.d(name, ": DISPOSE");

    }
}
