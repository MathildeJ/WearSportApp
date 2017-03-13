package com.example.mathilde.wearsportapp;

import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableRecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;


class MenuEntryViewHolder extends WearableRecyclerView.ViewHolder implements View.OnClickListener {

    @Bind(R.id.name)
    TextView nameText;
    @Bind(R.id.icon)
    ImageView iconImage;

    private OnItemClickListener listener;

    public MenuEntryViewHolder(View itemLayout) {
        super(itemLayout);
        ButterKnife.bind(this, itemLayout);
        itemLayout.setTag(itemLayout);
        itemLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        listener.onItemClick(v, getAdapterPosition());
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
