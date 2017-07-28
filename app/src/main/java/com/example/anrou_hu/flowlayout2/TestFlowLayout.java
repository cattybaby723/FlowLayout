package com.example.anrou_hu.flowlayout2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A simple sample Activity for showing how to use FlowLayout
 *
 * @author anrou_hu
 */

public class TestFlowLayout extends AppCompatActivity {
    private final int DEFAULT_INDEX = 0;
    private final int MAX_TEXT_VIEW_COUNT = 10;
    private final int MAX_TEXT_RANDOM_SIZE = 30;


    FlowLayout mVerticalFlowLayout;
    FlowLayout mHorizontalFlowLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_flow_layout);

        initViews();
        generateViewIntoFlowLayout();
        generateViewIntoHorizontalFlowLayout();
    }


    private void initViews() {
        mVerticalFlowLayout = (FlowLayout) findViewById(R.id.verticalFlowLayout);
        mHorizontalFlowLayout = (FlowLayout) findViewById(R.id.horizontalFlowLayout);
    }


    private void generateViewIntoFlowLayout() {
        List<String> textList = generateStringList();

        for (String testText : textList) {
            View view = getLayoutInflater().inflate(R.layout.component_test_item, mVerticalFlowLayout, false);
            TextView textView = (TextView) view.findViewById(R.id.testText);
            textView.setText(testText);
            mVerticalFlowLayout.addView(view);
        }
    }

    private void generateViewIntoHorizontalFlowLayout() {
        List<String> textList = generateStringList();

        for (String testText : textList) {
            View view = getLayoutInflater().inflate(R.layout.component_test_item, mHorizontalFlowLayout, false);
            TextView textView = (TextView) view.findViewById(R.id.testText);
            textView.setText(testText);
            mHorizontalFlowLayout.addView(view);
        }
    }


    // to generate String list from R.string.test_sentences randomly
    private List<String> generateStringList() {
        String res = getString(R.string.test_sentences);
        int resLength = res.length();

        Random random = new Random();
        int listSize = random.nextInt(MAX_TEXT_VIEW_COUNT) + 1;
        int textSize = random.nextInt(MAX_TEXT_RANDOM_SIZE) + 1;
        int startIndex = DEFAULT_INDEX;
        int endIndex = textSize;

        List<String> textList = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            String text = res.substring(startIndex, endIndex);
            textList.add(text);

            textSize = random.nextInt(MAX_TEXT_RANDOM_SIZE) + 1;
            startIndex = (startIndex > resLength) ? DEFAULT_INDEX : startIndex;
            endIndex = (endIndex + textSize > resLength) ? resLength : endIndex;
        }

        return textList;
    }
}
