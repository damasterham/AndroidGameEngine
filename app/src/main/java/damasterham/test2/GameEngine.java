package damasterham.test2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.constraint.solver.widgets.Rectangle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
//import android.view.WindowManager;
//import android.view.WindowManager.LayoutParams;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.view.WindowManager.LayoutParams.*;

public abstract class GameEngine extends Activity implements Runnable
{
    private Screen screen;
    private Canvas canvas;
    private Bitmap virtualScreen;
    private Rect virtualScreenSrc;
    private Rect virtualScreenDst;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;


    private Thread mainLoopThread;
    private GameState gameState = GameState.PAUSED;
    private List<GameState> gameStateChanges = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        this.getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON); //Bitwise

        surfaceView = new SurfaceView(this);
        setContentView(surfaceView);
        surfaceHolder = surfaceView.getHolder();

        screen = createStartScreen();

        if (surfaceView.getWidth() > surfaceView.getHeight())
        {
            setVirtualScreen(1920, 1080);
        }
        else
        {
            setVirtualScreen(1080, 1920);
        }

    }

    public void setVirtualScreen(int width, int height)// Bitmap.Config config = Bitmap.Config.ARGB_8888)
    {
        if (virtualScreen != null)
            virtualScreen.recycle();

        virtualScreen = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(virtualScreen);
    }

    public int getFrameBufferWidth()
    {
        return virtualScreen.getWidth();
    }

    public int getFrameBufferHeight()
    {
        return virtualScreen.getHeight();
    }

    //Service Methods
    public abstract Screen createStartScreen();

    public void setScreen(Screen screen)
    {
        if (this.screen != null)
            this.screen.dispose();

        this.screen = screen;
    }

    public Bitmap loadBitmap(String filename)
    {
        InputStream in = null;
        Bitmap bitmap;


        try
        {
            in = getAssets().open(filename);//, R.drawable.bob);
            bitmap = BitmapFactory.decodeStream(in);

            if (bitmap == null)
            {
                throw new RuntimeException("Tried loading bitmap of '"+ filename+"' but file was not image");
            }
            return bitmap;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Tried opening '" + filename +"', but error occurred");
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                }
            }

        }

    }

//    public Music loadMusic(String filename)
//    {
//        return null;
//    }
//
//    public Sound loadSound(String filename)
//    {
//        return null;
//    }

    public void clearFrameBuffer(int color)
    {
        canvas.drawColor(color);
    }


    public void drawBitmap(Bitmap bitmap, int x, int y)
    {
        if (canvas != null)
            canvas.drawBitmap(bitmap, x, y, null);
    }

    public void drawBitmap(Bitmap bitmap, int x, int y, int srcX, int srcY, int srcWidth, int srcHeight)
    {
        if (canvas == null) return;

        Rect src = new Rect();
        Rect dst = new Rect();

        //Image frame start point
        src.left = srcX;
        src.top = srcY;
        //Image frame width and height
        src.right = x + srcWidth;
        src.bottom = y + srcHeight;

        //Screen placement
        dst.left = x;
        dst.top = y;
        //Image resizing
        dst.right = x + srcWidth;
        dst.bottom = y + srcWidth;

        canvas.drawBitmap(bitmap, src, dst, null);
    }

    public boolean isTouchDown(int pointer)
    {
        return false;
    }

    public int getTouchX(int pointer)
    {
        return 0;
    }

    public int getTouchY(int pointer)
    {
        return 0;
    }

//    public List<TouchEvent> getTouchEvents()
//    {
//        return null;
//    }

    public float[] getAccelerometer()
    {
        return null;
    }


    //END . Service Methods


    @Override
    public void onPause()
    {
        super.onPause();

        synchronized (gameStateChanges)
        {
            if (isFinishing())
            {
                gameStateChanges.add(GameState.DISPOSED);
            }
            else
            {
                gameStateChanges.add(GameState.PAUSED);
            }
        }
        try
        {
            mainLoopThread.join();
        }
        catch (Exception e)
        {
            Log.d(this.getLocalClassName(),
                    "Error occured when trying to join Game thread with GameEngine Application thread");
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        synchronized (gameStateChanges)
        {
            gameStateChanges.add(GameState.RESUMED);
        }

        mainLoopThread = new Thread(this);
        mainLoopThread.start();
    }

    // Thread
    public void run()
    {
        while(true)
        {

            // Handle state changes
            synchronized (gameStateChanges)
            {
                int x = gameStateChanges.size();

                for (int i = 0; i < x; i++)
                {
                    gameState = gameStateChanges.get(i);
                    if (gameState == GameState.DISPOSED)
                    {
                        // Exit the game thread
                        Log.d(this.getLocalClassName(),"The game thread is disposed");
                        if (screen != null)
                            screen.dispose();
                        gameStateChanges.clear();
                        return;
                    }
                    else if (gameState == GameState.PAUSED)
                    {
                        Log.d(this.getLocalClassName(),"The game thread is paused");

                        if (screen != null)
                        screen.pause();
                        gameStateChanges.clear();
                        return;
                    }
                    else if (gameState == GameState.RESUMED)
                    {
                        Log.d(this.getLocalClassName(),"The game thread is resumed");
                        gameState = GameState.RUNNING;
                        if (screen != null)
                            screen.resume();

                    }
//                    else if (gameState == GameState.RUNNING)
//                    {
//                        Log.d(this.getLocalClassName(),"The game thread is running");
//                        if (screen != null)
//                            screen.update(0);
//                    }
                }


                gameStateChanges.clear();
            } // End Sync

            if (gameState == GameState.RUNNING)
            {
                if (!surfaceHolder.getSurface().isValid())
                    continue;

                Canvas physicalCanvas = surfaceHolder.lockCanvas();
                //canvas.drawColor(Color.GRAY);
                if (screen != null)
                {
                    screen.update(0);
                    virtualScreenSrc.left = 0;
                    virtualScreenSrc.top = 0;
                    virtualScreenSrc.right = virtualScreen.getWidth();
                    virtualScreenSrc.bottom = virtualScreen.getHeight();

                    virtualScreenDst.left = 0;
                    virtualScreenDst.top = 0;
                    virtualScreenDst.right = surfaceView.getWidth();
                    virtualScreenDst.bottom = surfaceView.getHeight();

                    physicalCanvas.drawBitmap(virtualScreen, virtualScreenSrc, virtualScreenDst, null);
                }
                surfaceHolder.unlockCanvasAndPost(physicalCanvas);
            }
        }
    }

}
