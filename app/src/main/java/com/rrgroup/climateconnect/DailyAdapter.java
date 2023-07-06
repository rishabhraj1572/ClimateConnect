package com.rrgroup.climateconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.DailyViewHolder> {

    private List<String> temperature;
    private List<String> day;
    private List<Integer> images;

    public DailyAdapter(List<String> temperature,List<String> day,List<Integer> images) {
        this.temperature = temperature;
        this.day = day;
        this.images =images;
    }

    @NonNull
    @Override
    public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily, parent, false);
        return new DailyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyViewHolder holder, int position) {
        String Temp = temperature.get(position);
        holder.degree.setText(Temp);
        String Day = day.get(position);
        holder.day.setText(Day);
        Integer image = images.get(position);
        //holder.image.setImageResource(image);
        Picasso.get()
                .load(image)
                .resize(35, 35)
                .centerCrop()
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return temperature.size();
    }

    public static class DailyViewHolder extends RecyclerView.ViewHolder {
        public TextView degree;
        public TextView day;
        public ImageView image;
        public DailyViewHolder(View itemView) {
            super(itemView);
            degree = itemView.findViewById(R.id.temp);
            day = itemView.findViewById(R.id.day);
            image = itemView.findViewById(R.id.image);
        }
    }
}
