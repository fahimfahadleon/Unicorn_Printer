package com.unicornms.unicornprinter;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {
    ArrayList<PaymentModel> models;
    Context context;
    public RVAdapter (Context context, ArrayList<PaymentModel> models){
        this.context = context;
        this.models = models;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_history_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentModel model = models.get(position);
        holder.senderPhone.setText(model.getUSERPHONE());
        holder.receiverPhone.setText(model.getRECEIVERS_PHONE());
        holder.amount.setText(model.getPAYMENNT_BDT());
        holder.type.setText(model.getPAYMENTGATEWAY());
        holder.isPersonal.setText(model.getISPERSONAL());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = "Receiver: "+holder.receiverPhone.getText().toString()+"\n"+
                         "AC/MOB NO: "+model.getPAYMENTPHONENUMBER()+"\n"+
                        "Payment GW: "+holder.type.getText().toString()+"\n"+
                        "BDT: "+holder.amount.getText().toString()+"\n"+
                        "Type: "+holder.isPersonal.getText().toString();

                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("txt", s);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, "Text Copied!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView senderPhone;
        TextView receiverPhone;
        TextView isPersonal;
        TextView type;
        TextView amount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            senderPhone = itemView.findViewById(R.id.senderNumber);
            receiverPhone = itemView.findViewById(R.id.receiverNumber);
            amount = itemView.findViewById(R.id.amount);
            type = itemView.findViewById(R.id.type);
            isPersonal = itemView.findViewById(R.id.isPersonal);
        }
    }
}
