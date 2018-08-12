package com.github.kilnn.wheelview.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.kilnn.wheelview.WheelView;
import com.github.kilnn.wheelview.adapters.NumericWheelAdapter;

public class MainActivity extends AppCompatActivity {

    private WheelView mWheelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWheelView = findViewById(R.id.wheel_view);
        mWheelView.setViewAdapter(new NumericWheelAdapter(this, 1, 10));
    }
}
