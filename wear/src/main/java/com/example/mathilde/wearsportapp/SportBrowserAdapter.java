package com.example.mathilde.wearsportapp;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class SportBrowserAdapter extends WearableRecyclerView.Adapter<SportBrowserAdapter.SportViewHolder> {

    List<Sport> sports;
    //private Context context;
    private SportInterface sportInterface;

    SportBrowserAdapter(List<Sport> sports, /*Context context,*/ SportInterface si){
        this.sports = sports;
        //this.context = context;
        this.sportInterface = si;
    }

    @Override
    public SportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        SportViewHolder svh = new SportViewHolder(itemView);
        return svh;
    }

    @Override
    public void onBindViewHolder(SportViewHolder holder, int position) {
        holder.sportName.setText(sports.get(position).getName());
        holder.sportImage.setImageResource(sports.get(position).getImage());
        holder.sportDescription.setText(sports.get(position).getDescription());
        holder.itemView.setTag(position);
        holder.setListener(new OnItemClickListener(){
            @Override
            public void onItemClick(View view, int position){
                sportInterface.sendMessage(sports.get(position).getLink());

            }
        });
    }

    @Override
    public int getItemCount() {
        return sports.size();
    }


    public static class SportViewHolder extends WearableRecyclerView.ViewHolder implements WearableRecyclerView.OnClickListener{
        @Bind(R.id.cv)CardView cv;
        @Bind(R.id.sport_name)TextView sportName;
        @Bind(R.id.sport_description)TextView sportDescription;
        @Bind(R.id.sport_image)ImageView sportImage;

        private OnItemClickListener listener;

        @Override
        public void onClick(View v) {
            listener.onItemClick(v, getAdapterPosition());
        }

        public void setListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        public SportViewHolder(final View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setTag(itemView);
            itemView.setOnClickListener(this);
        }

    }
}

