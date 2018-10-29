package xuanli_jxcable.tvjoy.xj.cn.numberprogress;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private NumberProgress mNumberProgress;
    Handler mHandler = new Handler();
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNumberProgress = (NumberProgress) findViewById(R.id.pb);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                count++;
                mNumberProgress.setProgress(count);
                mHandler.postDelayed(this,1000);
            }
        });


    }

}
