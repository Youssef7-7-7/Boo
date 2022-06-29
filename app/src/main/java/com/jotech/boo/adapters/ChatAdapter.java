package com.jotech.boo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jotech.boo.R;
import com.jotech.boo.models.ChatModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ChatModel> chatList;
    FirebaseUser currentUser;

    public ChatAdapter(Context context, List<ChatModel> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @NotNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        if (viewType==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_row_right, parent, false);
            return new MyHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.chat_row_left, parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ChatAdapter.MyHolder holder, final int position) {
        ChatModel chatModel = chatList.get(position);

        holder.msgTv.setText(chatModel.getMessage());
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (chatList.get(position).getSender().equals(currentUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }

    }

    class MyHolder extends RecyclerView.ViewHolder{


        public TextView msgTv;

        public MyHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            msgTv = itemView.findViewById(R.id.msgTv);
        }
    }
}
