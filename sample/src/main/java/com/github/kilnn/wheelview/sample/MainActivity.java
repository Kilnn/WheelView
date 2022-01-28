package com.github.kilnn.wheelview.sample;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

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
        findViewById(R.id.btn_show_one_wheel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OneWheelDialogFragment().show(getSupportFragmentManager(), null);
            }
        });
        findViewById(R.id.btn_show_two_wheel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TwoWheelDialogFragment().show(getSupportFragmentManager(), null);
            }
        });
        findViewById(R.id.btn_show_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DateWheelDialogFragment().show(getSupportFragmentManager(), null);
            }
        });
    }
}
