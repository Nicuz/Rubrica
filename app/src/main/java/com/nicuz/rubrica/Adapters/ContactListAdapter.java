package com.nicuz.rubrica.Adapters;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nicuz.rubrica.Model.Contact;
import com.nicuz.rubrica.Model.Database;
import com.nicuz.rubrica.R;

import java.util.ArrayList;

import androidx.navigation.Navigation;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactViewHolder> {

    private ArrayList<Contact> contactList;
    private Database database;

    public ContactListAdapter(ArrayList<Contact> list) {
        this.contactList = list;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        public TextView contactFullNameLabel;
        public ImageView contactImage;

        public ContactViewHolder(View itemView) {
            super(itemView);
            contactFullNameLabel = itemView.findViewById(R.id.contact_list_fullNameLabel);
            contactImage = itemView.findViewById(R.id.contact_list_ImageView);
        }
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_list_recyclerview_row, parent, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, final int position) {
        if (contactList.get(position).getPhoto() != null) {
            holder.contactImage.setImageBitmap(contactList.get(position).getDecodedPhoto(contactList.get(position).getPhoto()));
        } else {
            holder.contactImage.setImageResource(R.drawable.ic_account_circle_accent);
        }

        holder.contactFullNameLabel.setText(contactList.get(position).getFullName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                database = Database.getInstance(v.getContext());

                //Send data to the contact details fragment
                Bundle bundle = new Bundle();

                Contact contact = new Contact(
                        contactList.get(position).getFirstName(),
                        contactList.get(position).getLastName()
                );

                bundle.putInt("rowid", database.getRowid(contact));

                Navigation.findNavController(v).navigate(R.id.toContactDetails, bundle);
            }
        });
    }

    @Override
    public int getItemCount() { return contactList.size(); }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void filterList(ArrayList<Contact> filteredList) {
        contactList = filteredList;
        notifyDataSetChanged();
    }
}