package com.hatch.h5browse.listener;

import android.view.View;

public interface OnItemClickListener {
    void onItemClick(View view, int position);

    void removeItem(View view, int position);
}
