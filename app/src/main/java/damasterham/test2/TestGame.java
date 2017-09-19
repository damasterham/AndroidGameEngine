package damasterham.test2;

public class TestGame extends GameEngine
{
    @Override
    public Screen createStartScreen()
    {
        return new TestScreen(this);
    }
}
