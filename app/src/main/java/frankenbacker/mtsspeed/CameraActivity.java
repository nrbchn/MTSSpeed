package frankenbacker.mtsspeed;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurgle.camerakit.CameraKit;
import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.github.anastr.speedviewlib.base.Speedometer;
import com.github.anastr.speedviewlib.components.Indicators.ImageIndicator;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity {
    private float speedDownload;
    private int cameraWidth;
    private int cameraHeight;

    @BindView(R.id.relativeLayout)
    ViewGroup layout;

    @BindView(R.id.camera)
    CameraView camera;

    @BindView(R.id.indicator1)
    ImageView indicator1;

    @BindView(R.id.speed)
    TextView textSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        isStoragePermissionGranted();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        cameraWidth = size.x;
        cameraHeight = cameraWidth; //size.y;

        ViewGroup.LayoutParams cameraLayoutParams = camera.getLayoutParams();
        cameraLayoutParams.width = cameraWidth;
        cameraLayoutParams.height = cameraHeight;
        camera.setLayoutParams(cameraLayoutParams);

        Bundle bundle = getIntent().getExtras();
        speedDownload = bundle.getFloat("speedDownload");
        indicator1.setRotation(getAngle(speedDownload));
        textSpeed.setText(String.format("%.1f", speedDownload) + " Mbps");
    }

    private float getAngle(float speed) {
        if(speed < 25)
            return 180/25*speed;
        else
            return 25;
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.start();
    }

    @Override
    protected void onPause() {
        camera.stop();
        super.onPause();
    }

    @OnClick(R.id.capturePhoto)
    void capturePhoto() {
        ImageButton buttonCap = (ImageButton) findViewById(R.id.capturePhoto);
        buttonCap.setVisibility(View.INVISIBLE);
        ImageButton buttonCap2 = (ImageButton) findViewById(R.id.toggleCamera);
        buttonCap2.setVisibility(View.INVISIBLE);
        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(View.VISIBLE);

        Bitmap bitmap = ResultHolder.getImage();
        final long startTime = System.currentTimeMillis();
        camera.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);
                long callbackTime = System.currentTimeMillis();
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                ResultHolder.dispose();
                ResultHolder.setImage(bitmap);
                ResultHolder.setNativeCaptureSize(camera.getPreviewSize());
                ResultHolder.setTimeToCallback(callbackTime - startTime);
                ResultHolder.setImage(Draw());
                Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                intent.putExtra("speedDownload", speedDownload);
                startActivity(intent);
            }
        });
        camera.captureImage();
    }

    @OnClick(R.id.toggleCamera)
    void toggleCamera() {
        switch (camera.toggleFacing()) {
            case CameraKit.Constants.FACING_BACK:
                //Toast.makeText(this, "Switched to back camera!", Toast.LENGTH_SHORT).show();
                break;

            case CameraKit.Constants.FACING_FRONT:
                //Toast.makeText(this, "Switched to front camera!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @OnClick(R.id.toggleFlash)
    void toggleFlash() {
        switch (camera.toggleFlash()) {
            case CameraKit.Constants.FLASH_ON:
                Toast.makeText(this, "Flash on!", Toast.LENGTH_SHORT).show();
                break;

            case CameraKit.Constants.FLASH_OFF:
                Toast.makeText(this, "Flash off!", Toast.LENGTH_SHORT).show();
                break;

            case CameraKit.Constants.FLASH_AUTO:
                Toast.makeText(this, "Flash auto!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private Bitmap barBitmap() {
        ImageView bar_im = (ImageView) findViewById(R.id.frame);
        bar_im.setDrawingCacheEnabled(true);
        bar_im.buildDrawingCache();
        Bitmap bar_bm = Bitmap.createBitmap(bar_im.getDrawingCache());
        return bar_bm;
    }

    private Bitmap speedBitmap() {
        ImageView speed_im = (ImageView) findViewById(R.id.speed1);
        speed_im.setDrawingCacheEnabled(true);
        speed_im.buildDrawingCache();
        Bitmap speed_bm = Bitmap.createBitmap(speed_im.getDrawingCache());
        return speed_bm;
    }

    private Bitmap indicatorBitmap() {
        ImageView indicator_im = (ImageView) findViewById(R.id.indicator1);
        indicator_im.setDrawingCacheEnabled(true);
        indicator_im.buildDrawingCache();
        Bitmap speed_bm = Bitmap.createBitmap(indicator_im.getDrawingCache());

        Matrix matrix = new Matrix();

        matrix.postRotate(getAngle(speedDownload));

        Bitmap rotatedBitmap = Bitmap.createBitmap(speed_bm, 0, 0, speed_bm.getWidth(), speed_bm.getHeight(), matrix, true);

        return rotatedBitmap;
    }

    private Bitmap textBm() {
        TextView indicator_im = (TextView) findViewById(R.id.speed);
        indicator_im.setDrawingCacheEnabled(true);
        indicator_im.buildDrawingCache();
        Bitmap speed_bm = Bitmap.createBitmap(indicator_im.getDrawingCache());
        return speed_bm;
    }

    Paint paint;
    private Bitmap Draw() {
        Bitmap bm = Bitmap.createBitmap(barBitmap().getWidth(), barBitmap().getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);

        paint = new Paint();

        canvas.drawBitmap(scaleDown(ResultHolder.getImage(), barBitmap().getWidth(), true), 0, 0, paint);
        canvas.drawBitmap(barBitmap(), 0, 0, paint);
        canvas.drawBitmap(speedBitmap(), barBitmap().getWidth() - speedBitmap().getWidth() - 8,
                barBitmap().getHeight() - speedBitmap().getHeight(), paint);
        canvas.drawBitmap(indicatorBitmap(), barBitmap().getWidth() - indicatorBitmap().getWidth() + 18,
                barBitmap().getHeight() - indicatorBitmap().getHeight() + 10, paint);
        canvas.drawBitmap(textBm(), barBitmap().getWidth() - speedBitmap().getWidth() - 8,
                barBitmap().getHeight() - textBm().getHeight() - 30, paint);

        return bm;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }
}
