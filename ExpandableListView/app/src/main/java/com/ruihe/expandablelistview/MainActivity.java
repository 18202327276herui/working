package com.ruihe.expandablelistview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ExpandableListView expandableListView;
    private List<String> groupArray;
    private List<List<String>> childArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    public void initView() {
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);

        groupArray = new ArrayList<String>();
        childArray = new ArrayList<List<String>>();

        groupArray.add("第一行");
        groupArray.add("第二行");

        List<String> tempArray = new ArrayList<String>();
        tempArray.add("第一条");
        tempArray.add("第二条");
        tempArray.add("第三条");

        for(int i = 0; i<groupArray.size(); i ++) {
            childArray.add(tempArray);
        }

        expandableListView.setAdapter(new ExpandableAdapter(MainActivity.this, groupArray, childArray));
        //在分组列表视图中 展开一组
//        expandableListView.expandGroup(1);
//        expandableListView.isGroupExpanded(1);
//        Log.v("112", expandableListView.isGroupExpanded(1) + "");
    }

}


