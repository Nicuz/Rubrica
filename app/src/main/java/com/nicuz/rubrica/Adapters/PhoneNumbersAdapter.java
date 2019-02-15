package com.nicuz.rubrica.Adapters;

import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import com.nicuz.rubrica.Model.PhoneNumber;
import com.nicuz.rubrica.R;

import java.util.ArrayList;

public class PhoneNumbersAdapter extends RecyclerView.Adapter<PhoneNumbersAdapter.ContactViewHolder> {

    private ArrayList<PhoneNumber> phoneNumbers;

    public PhoneNumbersAdapter(ArrayList<PhoneNumber> list) {
        this.phoneNumbers = list;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        public TextInputEditText phoneNumber;
        public Spinner phoneType;
        public ImageView phoneIcon;

        public ContactViewHolder(final View itemView) {
            super(itemView);
            phoneNumber = itemView.findViewById(R.id.add_contact_recyclerview_phoneNumber);
            phoneType = itemView.findViewById(R.id.add_contact_recyclerview_phoneType);
            phoneIcon = itemView.findViewById(R.id.add_contact_recyclerview_phoneIcon);
        }
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.phone_numbers_recyclerview_row, parent, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ContactViewHolder holder, final int position) {
        holder.phoneIcon.setImageResource(R.drawable.ic_phone_accent);
        holder.phoneType.setSelection(getIndex(holder.phoneType, phoneNumbers.get(position).getType()));
        holder.phoneNumber.setText(phoneNumbers.get(position).getNumber());

        //Update array if phoneType changes
        holder.phoneType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int index, long id) {
                phoneNumbers.get(position).setType(holder.phoneType.getItemAtPosition(index).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //Update array if phoneNumber changes
        holder.phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                phoneNumbers.get(position).setNumber(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return phoneNumbers.size();
    }

    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equals(myString)){
                return i;
            }
        }
        return -1;
    }

    public ArrayList<PhoneNumber> getNumbers(){
        return phoneNumbers;
    }
}