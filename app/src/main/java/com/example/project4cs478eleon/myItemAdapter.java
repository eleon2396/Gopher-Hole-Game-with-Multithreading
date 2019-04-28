package com.example.project4cs478eleon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class myItemAdapter extends BaseAdapter {

    private int holesBoardFront[];
    private Context mContext;
    private LayoutInflater minflator;
    private static final int PADDING = 20;
    private static final int WIDTH = 175;
    private static final int HEIGHT = 175;



    public myItemAdapter(Context c, int[] i){
        this.holesBoardFront = i;
        //minflator = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = c;
    }

    @Override
    public int getCount() {
        return holesBoardFront.length;
    }

    @Override
    public Object getItem(int position) {
        return holesBoardFront[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void changeItem(int image, int position){
        this.holesBoardFront[position] = image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView image = (ImageView) convertView;
        if(image == null){
            image = new ImageView(mContext);
            image.setLayoutParams(new GridView.LayoutParams(WIDTH, HEIGHT));
            image.setPadding(PADDING, PADDING, PADDING, PADDING);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);

        }


        image.setImageResource(holesBoardFront[position]);
        return image;
    }
}
