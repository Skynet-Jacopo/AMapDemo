package com.example.liuqun.amapdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkStep;
import com.example.liuqun.amapdemo.R;

import java.util.ArrayList;
import java.util.List;

public class itemWalkDetialAdapter extends BaseAdapter {

    private List<WalkStep> mSteps = new ArrayList<WalkStep>();

    private Context        context;
    private LayoutInflater layoutInflater;
    private WalkPath       mWalkPath;

    public itemWalkDetialAdapter(Context context,WalkPath walkPath) {
        this.context = context;
        mWalkPath = walkPath;
        mSteps =mWalkPath.getSteps();
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mSteps.size();
    }

    @Override
    public WalkStep getItem(int position) {
        return mSteps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.item_drive_detial, null);
            holder.tvStepDetial = (TextView) convertView.findViewById(R.id.tv_step_detial);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        WalkStep step = mSteps.get(position);
        holder.tvStepDetial.setText(step.getInstruction());
        return convertView;
    }


    private class ViewHolder {
        private TextView tvStepDetial;
    }}
