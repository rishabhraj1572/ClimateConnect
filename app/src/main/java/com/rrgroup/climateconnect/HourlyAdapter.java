package com.rrgroup.climateconnect;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.HourViewHolder> {

    private List<String> temperature;
    private List<String> time;
    private List<Integer> images;

    public HourlyAdapter(List<String> temperature,List<String> time, List<Integer> images) {
        this.temperature = temperature;
        this.time = time;
        this.images =images;

    }

    @NonNull
    @Override
    public HourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hourly, parent, false);
        return new HourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {
        String name = temperature.get(position);
        holder.degree.setText(name);
        String Time = time.get(position);
        holder.Time.setText(Time);
        Integer weather_icon_url = images.get(position);
        Picasso.get()
                .load(weather_icon_url)
                .resize(60, 60)
                .centerCrop()
                .into(holder.weather_icon);

        if (position == 0) {
            holder.hourly.setCardBackgroundColor(Color.parseColor("#3c7ad0"));
            holder.degree.setTextColor(Color.WHITE);
            holder.Time.setTextColor(Color.WHITE);
            //holder.Time.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else {
            holder.degree.setTextColor(Color.BLACK);
            holder.Time.setTextColor(Color.BLACK);
            holder.hourly.setCardBackgroundColor(Color.parseColor("#eaeaf1"));
        }
    }

    @Override
    public int getItemCount() {
        return temperature.size();
    }

    public static class HourViewHolder extends RecyclerView.ViewHolder {
        public TextView degree;
        public TextView Time;
        public CardView hourly;
        public ImageView weather_icon;

        public HourViewHolder(View itemView) {
            super(itemView);
            degree = itemView.findViewById(R.id.temperatute);
            Time = itemView.findViewById(R.id.time);
            hourly = itemView.findViewById(R.id.hourly);
            weather_icon = itemView.findViewById(R.id.weather_icon);

        }
    }
}
