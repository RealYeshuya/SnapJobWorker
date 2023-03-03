package com.act.snapjob_worker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.act.snapjob_worker.Global.Transactions;

import java.util.ArrayList;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder>{
    Context context;
    ArrayList<Transactions> list;

    public RequestAdapter(Context context, ArrayList<Transactions> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.userlistviewsingle,parent,false);
        return new RequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder holder, int position) {
        Transactions user = list.get(position);
        String username = user.userName;
        String address = user.address;
        String userId = user.userId;
        String transactionStatus = user.transactionStatus;
        String transId = user.transId;
        String workerName = user.workerName;
        String transactionDesc = user.transactionDescription;
        String userPhoneNum = user.userPhoneNumber;
        String transDate = user.transactionDate;

        holder.username.setText(username);
        holder.address.setText(address);
        holder.singleuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (transactionStatus.equals("Complete")){
                    Intent intent = new Intent(context, Receipt.class);
                    intent.putExtra("clientName",username);
                    intent.putExtra("clientAdd",address);
                    intent.putExtra("transId",transId);
                    intent.putExtra("date",transDate);
                    intent.putExtra("workerName",workerName);
                    intent.putExtra("transDesc",transactionDesc);
                    intent.putExtra("transactionStatus",transactionStatus);
                    context.startActivity(intent);
                }
                else if (transactionStatus.equals("Declined")){
                    Intent intent = new Intent(context, Receipt.class);
                    intent.putExtra("clientName",username);
                    intent.putExtra("clientAdd",address);
                    intent.putExtra("transId",transId);
                    intent.putExtra("date",transDate);
                    intent.putExtra("workerName",workerName);
                    intent.putExtra("transDesc",transactionDesc);
                    intent.putExtra("transactionStatus",transactionStatus);
                    context.startActivity(intent);
                }
                else {
                    Intent intent = new Intent(context, SelectedUser.class);
                    intent.putExtra("name", username);
                    intent.putExtra("address", address);
                    intent.putExtra("userId", userId);
                    intent.putExtra("transactionStatus", transactionStatus);
                    intent.putExtra("transId", transId);
                    intent.putExtra("workerName", workerName);
                    intent.putExtra("transDesc", transactionDesc);
                    intent.putExtra("userPhoneNum", userPhoneNum);
                    context.startActivity(intent);
                }
            }
        });
    }

    public int getItemCount(){
        return list.size();
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        public TextView username, address;
        public ImageView clientAvatar;
        CardView singleuser;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            clientAvatar = itemView.findViewById(R.id.clientAvatar);
            username = itemView.findViewById(R.id.userName);
            address = itemView.findViewById(R.id.userAdd);
            singleuser = itemView.findViewById(R.id.userRequestItem);
        }
    }
    private void dialogRequest(){

    }
}
