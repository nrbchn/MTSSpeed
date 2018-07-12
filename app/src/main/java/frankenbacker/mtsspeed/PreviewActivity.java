package frankenbacker.mtsspeed;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.wonderkiln.camerakit.Size;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PreviewActivity extends AppCompatActivity {
    private Bitmap glob;

    @BindView(R.id.image)
    ImageView imageView;

    @BindView(R.id.button)
    Button test;

    float speedDownload;

    @OnClick(R.id.button)
    void GoTest() {
        Intent intent = new Intent(PreviewActivity.this, MainActivity.class);
        startActivity(intent);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            speedDownload = bundle.getFloat("speedDownload");
        }
    }

    @OnClick(R.id.button2)
    void Save() {
        saveImage(glob);
    }

    @OnClick(R.id.button3)
    void GoPho() {
        Intent intent = new Intent(PreviewActivity.this, CameraActivity.class);
        intent.putExtra("speedDownload", speedDownload);
        startActivity(intent);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            return false; //I have tried here true also
        }
        return super.onKeyDown(keyCode, e);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        speedDownload = bundle.getFloat("speedDownload");
        Bitmap bitmap = ResultHolder.getImage();
        if (bitmap == null) {
            finish();
            return;
        }

        glob = bitmap;

        imageView.setImageBitmap(bitmap);

        Size captureSize = ResultHolder.getNativeCaptureSize();

    }

    private void galleryAddPic(String filename) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filename);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void saveImage(Bitmap finalBitmap) {
        String filename;
        String fname = Environment.getExternalStorageDirectory().toString()
                + "/RES"
                + System.currentTimeMillis()
                + ".jpg";

        File file = new File(fname);
        filename = fname;
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            galleryAddPic(filename);
            Toast.makeText(this, "Сохранено!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
