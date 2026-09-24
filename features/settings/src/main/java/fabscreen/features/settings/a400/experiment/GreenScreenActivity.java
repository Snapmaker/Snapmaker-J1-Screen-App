package fabscreen.features.settings.a400.experiment;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.settings.R;
import fabscreen.platform.base.RoutePath;

@Route(path = RoutePath.A400_GREEN_SCREEN)
public class GreenScreenActivity extends AppCompatActivity {

    private long mTime = 0;
    private int mTouchCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_green_screen);
        findViewById(R.id.btn_green_screnn_bg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentTime = SystemClock.elapsedRealtime();
                if (currentTime - mTime < 500) {
                    mTouchCount += 1;
                } else {
                    mTouchCount = 1;
                }
                mTime = currentTime;
                if (mTouchCount >= 3) {
                    finish();
                }
            }
        });
    }

}
