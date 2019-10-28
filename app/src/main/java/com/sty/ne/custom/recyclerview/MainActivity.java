package com.sty.ne.custom.recyclerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private boolean isMultiplyType = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public View onCreateViewHolder(int position, View convertView, ViewGroup parent) {
                if(isMultiplyType) {
                    if (position % 2 == 0) {
                        convertView = MainActivity.this.getLayoutInflater().inflate(R.layout.item_table, parent, false);
//                    TextView textView = convertView.findViewById(R.id.text);
//                    textView.setText("第 " + position + "行");
                    } else {
                        convertView = MainActivity.this.getLayoutInflater().inflate(R.layout.item_table2, parent, false);
//                    TextView textView = convertView.findViewById(R.id.text2);
//                    textView.setText("第 " + position + "行的图标");
                    }
                }else {
                    convertView = MainActivity.this.getLayoutInflater().inflate(R.layout.item_table, parent, false);
                }
//                return convertView;
                return onBinderViewHolder(position, convertView, parent);
            }

            @Override
            public View onBinderViewHolder(int position, View convertView, ViewGroup parent) {
                if(isMultiplyType) {
                    if (position % 2 == 0) {
                        TextView textView = convertView.findViewById(R.id.text);
//                    textView.setText("--->" + position);
                        textView.setText("第 " + position + "行");
                        return convertView;
                    } else {
                        TextView textView = convertView.findViewById(R.id.text2);
                        textView.setText("第 " + position + "行的图标");
                        return convertView;
                    }
                }else {
                    TextView textView = convertView.findViewById(R.id.text);
//                    textView.setText("--->" + position);
                    textView.setText("第 " + position + "行");
                    return convertView;
                }
            }

            @Override
            public int getItemViewType(int row) {
                if(isMultiplyType) {
                    if(row % 2 == 0) {
                        return 0;
                    }else {
                        return 1;
                    }
                }else {
                    return 0;
                }
            }

            @Override
            public int getViewTypeCount() {
                if(isMultiplyType) {
                    return 2;
                }else {
                    return 1;
                }
            }

            @Override
            public int getCount() {
                return 30000;
            }

            @Override
            public int getHeight(int index) {
                return 200;
            }
        });
    }
}
