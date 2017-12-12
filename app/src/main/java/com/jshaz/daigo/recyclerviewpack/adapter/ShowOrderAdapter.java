package com.jshaz.daigo.recyclerviewpack.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jshaz.daigo.R;
import com.jshaz.daigo.client.OrderDetailActivity;
import com.jshaz.daigo.gson.OrderDAO;
import com.jshaz.daigo.util.Order;
import com.jshaz.daigo.util.Setting;
import com.jshaz.daigo.util.Utility;

import java.util.List;

/**
 * Created by jshaz on 2017/12/9.
 */

public class ShowOrderAdapter extends RecyclerView.Adapter<ShowOrderAdapter.ViewHolder> {

    private List<OrderDAO> orderDAOList;

    private Context mContext;

    private Activity parentActivity;

    private String userId;

    public ShowOrderAdapter() {
        super();
    }

    public ShowOrderAdapter(List<OrderDAO> orderDAOList) {
        this.orderDAOList = orderDAOList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_order_item,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final OrderDAO order = new OrderDAO(orderDAOList.get(position));

        switch (order.getOrderState()) {
            case Order.NORMAL:
                holder.orderState.setText("未接单");
                holder.barLayout.setBackgroundResource(R.color.colorPrimary);
                break;
            case Order.RECEIVED:
                if (order.getOrderSender().getUserId().equals(userId)) {
                    holder.orderState.setText("正在配送");
                    holder.barLayout.setBackgroundColor(Color.rgb(255,66,66));
                } else {
                    holder.orderState.setText("已接单，请尽快送达");
                    holder.barLayout.setBackgroundColor(Color.rgb(255,128,64));
                }
                break;
            case Order.COMPLETE:
                if (order.getOrderSender().getUserId().equals(userId)) {
                    holder.orderState.setText("订单已完成");
                    holder.barLayout.setBackgroundColor(Color.rgb(34,181,78));
                } else {
                    holder.orderState.setText("配送完成");
                    holder.barLayout.setBackgroundColor(Color.rgb(236,214,11));
                }

                break;
            case Order.INVALIDATE:
                holder.orderState.setText("订单已取消");
                holder.barLayout.setBackgroundColor(Color.rgb(125,125,125));
                break;
        }
        holder.campus.setText(Setting.getCampusName(Utility.getCampusCode(order.getOrderId())));
        holder.title.setText(order.getTitle());
        holder.publicContent.setText(order.getPublicDetails());
        holder.privateContent.setText(order.getPrivateDetails());
        holder.releaseTime.setText(Utility.getOrderReleaseTime(order.getOrderId()));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(parentActivity, OrderDetailActivity.class);
                intent.putExtra("openMethod", 1);
                intent.putExtra("order_id", order.getOrderId());
                intent.putExtra("user_id", userId);
                parentActivity.startActivityForResult(intent, 1);
            }
        });

        holder.cardView.setLongClickable(true);
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderDAOList.size();
    }

    public List<OrderDAO> getOrderDAOList() {
        return orderDAOList;
    }

    public void setOrderDAOList(List<OrderDAO> orderDAOList) {
        this.orderDAOList = orderDAOList;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public Activity getParentActivity() {
        return parentActivity;
    }

    public void setParentActivity(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout barLayout;

        private CardView cardView;

        private TextView orderState;
        private TextView title;
        private TextView campus;
        private TextView publicContent;
        private TextView privateContent;
        private TextView releaseTime;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.my_order_item_card_view);
            barLayout = (RelativeLayout) itemView.findViewById(R.id.my_order_item_bar);
            orderState = (TextView) itemView.findViewById(R.id.my_order_item_state);
            title = (TextView) itemView.findViewById(R.id.my_order_item_title);
            campus = (TextView) itemView.findViewById(R.id.my_order_item_campus);
            publicContent = (TextView) itemView.findViewById(R.id.my_order_item_public_content);
            privateContent = (TextView) itemView.findViewById(R.id.my_order_item_private_content);
            releaseTime = (TextView) itemView.findViewById(R.id.my_order_item_release_time);
        }
    }
}
