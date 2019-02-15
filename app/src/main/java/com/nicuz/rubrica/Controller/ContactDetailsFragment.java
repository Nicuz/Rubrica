package com.nicuz.rubrica.Controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nicuz.rubrica.Model.Contact;
import com.nicuz.rubrica.Adapters.PhoneNumbersListAdapter;
import com.nicuz.rubrica.Model.Database;
import com.nicuz.rubrica.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import androidx.navigation.Navigation;

public class ContactDetailsFragment extends Fragment implements OnMapReadyCallback {

    private Database database;

    private Contact contact;
    private PhoneNumbersListAdapter phoneNumbersListAdapter;
    private RecyclerView phoneNumbersrecyclerView;

    private MapView mapView;

    private ImageView shareBtn;
    private ImageView removeBtn;
    private ImageView editBtn;

    public ContactDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_details, container, false);

        //Toolbar settings
        Toolbar toolbar = view.findViewById(R.id.toolbar_contact_details);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        database = Database.getInstance(getActivity());

        //get rowid from bundle
        final int rowid = getArguments().getInt("rowid");
        contact = database.getContact(rowid);

        ImageView imageView = view.findViewById(R.id.contact_details_contact_photo);
        if (contact.getPhoto() != null) {
            imageView.setImageBitmap(contact.getDecodedPhoto(contact.getPhoto()));
        } else {
            imageView.setImageResource(R.drawable.ic_account_circle_accent);
        }

        TextView textView = view.findViewById(R.id.contact_details_fullNameLabel);
        textView.setText(contact.getFullName());

        phoneNumbersrecyclerView = view.findViewById(R.id.contact_details_phone_numbers_recyclerview);
        phoneNumbersrecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        phoneNumbersListAdapter = new PhoneNumbersListAdapter(contact.getPhoneNumbers());
        phoneNumbersrecyclerView.setAdapter(phoneNumbersListAdapter);

        if (contact.getEmail() != null) {
            LinearLayout email_linearlayout = view.findViewById(R.id.contact_details_email_linearlayout);
            email_linearlayout.setVisibility(View.VISIBLE);
            TextView email = view.findViewById(R.id.contact_details_email_textview);
            ImageView email_icon = view.findViewById(R.id.contact_details_email_icon);
            email_icon.setImageResource(R.drawable.ic_email_accent);
            email.setText(contact.getEmail());
        }

        if (contact.getCompany() != null) {
            LinearLayout company_linearlayout = view.findViewById(R.id.contact_details_company_linearlayout);
            company_linearlayout.setVisibility(View.VISIBLE);
            TextView company = view.findViewById(R.id.contact_details_company_textview);
            ImageView company_icon = view.findViewById(R.id.contact_details_company_icon);
            company_icon.setImageResource(R.drawable.ic_work_accent);
            company.setText(contact.getCompany());
        }

        if (contact.getAddress() != null) {
            LinearLayout address_linearlayout = view.findViewById(R.id.contact_details_address_linearlayout);
            address_linearlayout.setVisibility(View.VISIBLE);
            TextView address = view.findViewById(R.id.contact_details_address_textview);
            ImageView address_icon = view.findViewById(R.id.contact_details_address_icon);
            address_icon.setImageResource(R.drawable.ic_location_accent);
            address.setText(contact.getAddress());
        }

        ImageView businessCard = view.findViewById(R.id.contact_details_businessCard);
        if (contact.getBusinessCardPhoto() != null) {
            businessCard.setImageBitmap(contact.getDecodedPhoto(contact.getBusinessCardPhoto()));
        } else {
            TextView headerBusinessCard = view.findViewById(R.id.contact_details_header_businessCard);
            headerBusinessCard.setVisibility(view.GONE);
            businessCard.setVisibility(view.GONE);
        }

        shareBtn = view.findViewById(R.id.contact_details_shareBtn);
        shareBtn.setOnClickListener(shareContact);

        removeBtn = view.findViewById(R.id.contact_details_deleteBtn);
        removeBtn.setOnClickListener(removeContact);

        editBtn = view.findViewById(R.id.contact_details_editBtn);
        editBtn.setOnClickListener(editContact);

        return view;
    }

    private View.OnClickListener shareContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //Create vCard file
            File vCardFile = null;
            try {
                vCardFile = File.createTempFile(
                        contact.getFullName().replaceAll(" ", ""),
                        ".vcf",
                        getActivity().getCacheDir()
                );

                //Write vCard content on file
                BufferedWriter writer = new BufferedWriter(new FileWriter(vCardFile));
                writer.write(contact.getvCard(contact));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/x-vcard");
            Uri contentUri = FileProvider.getUriForFile(getActivity(), "com.nicuz.rubrica.fileprovider", vCardFile);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            startActivity(Intent.createChooser(shareIntent, getContext().getString(R.string.contact_details_shareTitle)));

            vCardFile.deleteOnExit();
        }
    };

    private View.OnClickListener removeContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
            builder.setMessage(R.string.contact_details_dialog_deleteContact);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    database.deleteContact(contact);
                    Toast.makeText(getActivity(), R.string.contact_details_toast_contactDeleted,
                            Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            android.support.v7.app.AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    private View.OnClickListener editContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int rowid = getArguments().getInt("rowid");

            //Send rowid to EditContactFragment
            Bundle bundle = new Bundle();
            bundle.putInt("rowid", rowid);

            Navigation.findNavController(v).navigate(R.id.toEditContact, bundle);
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (contact.getAddress() != null) {
            mapView = view.findViewById(R.id.contact_details_mapview);
            mapView.setVisibility(view.VISIBLE);
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        MapsInitializer.initialize(getContext());
        Geocoder geocoder = new Geocoder(getContext());

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(contact.getAddress(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (addresses != null) {
            googleMap.addMarker(new MarkerOptions().position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude())));
            CameraPosition location = CameraPosition.builder().target(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude())).zoom(18).bearing(0).build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(location));
        }
    }
}
