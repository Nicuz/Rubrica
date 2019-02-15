package com.nicuz.rubrica.Adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nicuz.rubrica.Model.PhoneNumber;
import com.nicuz.rubrica.R;

import java.util.ArrayList;

public class PhoneNumbersListAdapter extends RecyclerView.Adapter<PhoneNumbersListAdapter.ContactViewHolder> {

    private ArrayList<PhoneNumber> phoneNumbersList;
    public PhoneNumbersListAdapter(ArrayList<PhoneNumber> list) {
        this.phoneNumbersList = list;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        public TextView phoneNumber;
        public TextView phoneType;
        public ImageView phoneIcon;

        public ContactViewHolder(View itemView) {
            super(itemView);
            phoneNumber = itemView.findViewById(R.id.contact_details_phoneNumberLabel);
            phoneType = itemView.findViewById(R.id.contact_details_phoneTypeLabel);
            phoneIcon = itemView.findViewById(R.id.contact_details_phone_icon);
        }
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_details_phone_numbers_recyclerview_row, parent, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, final int position) {
        holder.phoneNumber.setText(phoneNumbersList.get(position).getNumber());
        holder.phoneType.setText(phoneNumbersList.get(position).getType());
        holder.phoneIcon.setImageResource(R.drawable.ic_phone_accent);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Open dialer when tapping on a phone number
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+ phoneNumbersList.get(position).getNumber()));
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return phoneNumbersList.size();
    }
}