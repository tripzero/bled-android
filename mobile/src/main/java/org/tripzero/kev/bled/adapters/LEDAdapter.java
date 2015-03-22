package org.tripzero.kev.bled.adapters;


import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.support.v7.widget.SwitchCompat;
import android.widget.TextView;

import org.tripzero.kev.bled.Ble;
import org.tripzero.kev.bled.MainActivity;
import org.tripzero.kev.bled.R;
import org.tripzero.kev.bled.utils.Utils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.markushi.ui.CircleButton;

/**
 * Created by ammonrees on 1/25/15.
 */
public class LEDAdapter extends RecyclerView.Adapter<LEDAdapter.MyViewHolder> implements View.OnClickListener {

    private boolean animateItems = false;
    private static final int ANIMATED_ITEMS_COUNT = 2;
    private int lastAnimatedPosition = -1;
    int LEDcolor;
    private final LayoutInflater inflater;
    Context mContext;
    List<Ble.Device> mItems = Collections.emptyList();
    private OnFeedItemClickListener onFeedItemClickListener;



    public LEDAdapter(Context context, List<Ble.Device> devices) {
        inflater = LayoutInflater.from(context);
        this.mItems = devices;
        this.mContext = context;
    }

    @Override

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_item, parent, false);
        ((MainActivity) mContext).backgroundColor = LEDcolor;
        MyViewHolder holder = new MyViewHolder(view);
        Typeface tf = Typeface.createFromAsset(parent.getContext().getAssets(), "fonts/Roboto-Medium.ttf");
        Typeface tf2 = Typeface.createFromAsset(parent.getContext().getAssets(),"fonts/Roboto-Thin.ttf");
        Typeface tf3 = Typeface.createFromAsset(parent.getContext().getAssets(),"fonts/Roboto-Regular.ttf");
        holder.LEDcolorView.setBackgroundColor(LEDcolor);
        holder.colorSelector.setOnClickListener(this);
        holder.settings.setOnClickListener(this);

        holder.lightName.setTypeface(tf);

        return holder;
    }

    private void runEnterAnimation(View view, int position) {
        if (position >= ANIMATED_ITEMS_COUNT - 1) {
            return;
        }

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(Utils.getScreenHeight(mContext));
            view.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .start();
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        runEnterAnimation(holder.itemView, position);
        //InspireItems Inspire = mItems.get(position);
        Ble.Device device = mItems.get(position);
        holder.lightName.setText(device.name());//.getiAuthor());

        holder.colorSelector.setTag(position);
        holder.settings.setTag(holder);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView lightName;
        CircleButton LEDcolorView;
        ImageButton colorSelector,settings;
        SwitchCompat onOff;

        public MyViewHolder(View itemView) {
            super(itemView);
            onOff = (SwitchCompat) itemView.findViewById(R.id.on_off_switch);
            LEDcolorView = (CircleButton) itemView.findViewById(R.id.led_view);
            lightName = (TextView) itemView.findViewById(R.id.light_name);
            settings = (ImageButton) itemView.findViewById(R.id.settings);
            colorSelector = (ImageButton) itemView.findViewById(R.id.color_selector);


        }
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.color_selector) {
            if (onFeedItemClickListener != null) {
                onFeedItemClickListener.onColorClick(view, (Integer) view.getTag());

            }

        } else if (viewId == R.id.settings) {
            MyViewHolder holder = (MyViewHolder) view.getTag();
            if (onFeedItemClickListener != null) {
                onFeedItemClickListener.onSettingsClick(view,holder.getPosition());


            }

        }
    }

    public void updateItems(boolean animated, int position) {

        animateItems = animated;
        notifyDataSetChanged();
    }

    public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
        this.onFeedItemClickListener = onFeedItemClickListener;
    }

    public interface OnFeedItemClickListener {
        void onColorClick(View v, int position);

        void onSettingsClick(View v, int position);

    }
}