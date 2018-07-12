package frankenbacker.mtsspeed;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.anastr.speedviewlib.Speedometer;
import com.github.anastr.speedviewlib.components.Indicators.ImageIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class MainActivity extends AppCompatActivity {
    private static float speedDownload = 0;

    @BindView(R.id.speedometer)
    Speedometer speedometer;

    @BindView(R.id.textDown)
    TextView textDown;

    @BindView(R.id.saveResult)
    Button saveResult;

    @BindView(R.id.test)
    Button test;

    @OnClick(R.id.saveResult)
    void saveResultClicked() {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        intent.putExtra("speedDownload", speedDownload);
        startActivity(intent);
    }

    @OnClick(R.id.test)
    void testClicked() {
        speedometer.speedTo(0);
        speedDownload = 0;
        textDown.setText(String.format("%.1f", speedDownload));
        test.setVisibility(View.INVISIBLE);
        saveResult.setVisibility(View.INVISIBLE);
        timer.start();
        new SpeedTestDownload().execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ImageIndicator imageIndicator = new ImageIndicator(getApplicationContext(), R.drawable.indicator, 500, 500);
        speedometer.setIndicator(imageIndicator);
        textDown.setText(String.format("%.1f", speedDownload));
    }

    private float getAngle(float speed) {
        speedometer.setMinMaxSpeed(0, 270);
        if (speed >= 0 && speed < 2)
            return (45*speed);
        if (speed >= 2 && speed < 5)
            return (90 + 45/3*(speed - 2));
        if (speed >= 5 && speed < 10)
            return (135 + 45/5*(speed - 5));
        if (speed >= 10 && speed < 25)
            return (180 + 45/15*(speed - 10));
        if (speed >= 25 && speed < 50)
            return(225 + 45/25*(speed - 25));
        return 0;
    }

    final CountDownTimer timer = new CountDownTimer(10000, 2) {

        @Override
        public void onTick(long millisUntilFinished) {
            speedometer.speedTo(getAngle(speedDownload), 1000);
            textDown.setText(String.format("%.1f", speedDownload));
        }

        @Override
        public void onFinish() {
            test.setText(R.string.test_again);
            test.setVisibility(View.VISIBLE);
            saveResult.setVisibility(View.VISIBLE);
        }
    };

    public static class SpeedTestDownload extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            SpeedTestSocket speedTestSocket = new SpeedTestSocket();
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onCompletion(SpeedTestReport report) {
                    speedDownload = report.getTransferRateBit().floatValue()/1000000;
                }

                @Override
                public void onError(SpeedTestError speedTestError, String errorMessage) {}

                @Override
                public void onProgress(float percent, SpeedTestReport report) {
                    speedDownload = report.getTransferRateBit().floatValue()/1000000;
                }
            });
            speedTestSocket.startFixedDownload("http://speedtest.mtsdv.ru/speedtest/random4000x4000.jpg", 10000);
            return null;
        }
    }
}
