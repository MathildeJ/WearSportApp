package com.example.mathilde.wearsportapp;

import android.content.Context;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MenuAdapter extends WearableRecyclerView.Adapter<MenuEntryViewHolder> {

    private final int[] ENTRIES = new int[]{R.string.start_sports, R.string.start_action};
    private final int[] ICONS = new int[]{R.drawable.ic_exercises, R.drawable.ic_steps};

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public MenuEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.view_list_item, parent, false);
        return new MenuEntryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MenuEntryViewHolder holder, final int position) {
        holder.nameText.setText(ENTRIES[position]);
        holder.iconImage.setImageResource(ICONS[position]);
        holder.itemView.setTag(position);
        holder.setListener(new OnItemClickListener(){
            @Override
            public void onItemClick(View view, int position){
                switch (position){
                    case 0:
                        context.startActivity(SportBrowserActivity.createIntent(context));
                        break;
                    case 1:
                        context.startActivity(StepCounterActivity.createIntent(context));
                        break;
                }

            }
        });
    }


    @Override
    public int getItemCount() {
        return ENTRIES.length;
    }
}
