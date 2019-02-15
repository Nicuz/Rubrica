package com.nicuz.rubrica.Controller;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nicuz.rubrica.Model.Database;
import com.nicuz.rubrica.Model.Contact;
import com.nicuz.rubrica.Adapters.ContactListAdapter;
import com.nicuz.rubrica.R;

import java.util.ArrayList;

import androidx.navigation.Navigation;

public class ContactListFragment extends Fragment {

    Toolbar toolbar;
    Toolbar searchToolbar;
    private EditText searchbar;
    private TextView emptyLabel;
    private TextView contactNotFound;

    private ArrayList<Contact> contactList;
    ContactListAdapter contactListAdapter;

    public ContactListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_contact_list, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        searchToolbar = view.findViewById(R.id.toolbar_search_bar);

        searchbar = view.findViewById(R.id.searchbar);
        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchFilter(s.toString());

            }
        });

        ImageView searchBtn = view.findViewById(R.id.contact_list_searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.setVisibility(view.GONE);
                showSoftKeyboard(searchbar);
            }
        });

        searchToolbar.setNavigationIcon(R.drawable.ic_arrow_back_accent);
        searchToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(getActivity());
                toolbar.setVisibility(view.VISIBLE);
            }
        });

        Database database = Database.getInstance(getActivity());

        contactList = database.getAllContacts();

        RecyclerView recyclerView = view.findViewById(R.id.contact_list_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        contactListAdapter = new ContactListAdapter(contactList);
        recyclerView.setAdapter(contactListAdapter);

        emptyLabel = view.findViewById(R.id.contact_list_emptyLabel);
        if (contactListAdapter.isEmpty()) {
            emptyLabel.setVisibility(view.VISIBLE);
        } else {
            emptyLabel.setVisibility(view.GONE);
        }

        FloatingActionButton goToaddContactBtn = view.findViewById(R.id.contact_list_goToAddContactBtn);
        goToaddContactBtn.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.toAddContactFragment));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Reset searchbar
        searchbar.setText(null);
    }

    public void searchFilter(String text) {
        ArrayList<Contact> searchResults = new ArrayList<>();

        for (Contact contact: contactList) {
            if (contact.getFullName().toLowerCase().contains(text.toLowerCase())) {
                searchResults.add(contact);
            }
        }

        contactListAdapter.filterList(searchResults);

        contactNotFound = getView().findViewById(R.id.contact_list_contactNotFound);
        if (contactNotFound != null && searchResults.size() > 0) {
            contactNotFound.setVisibility(View.GONE);
        } else {
            if (emptyLabel.getVisibility() == View.GONE) {
                contactNotFound.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            imm.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
