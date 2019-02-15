package com.nicuz.rubrica.Controller;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.nicuz.rubrica.Adapters.PhoneNumbersAdapter;
import com.nicuz.rubrica.Model.Contact;
import com.nicuz.rubrica.Model.Database;
import com.nicuz.rubrica.Model.PhoneNumber;
import com.nicuz.rubrica.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static com.nicuz.rubrica.Controller.AddContactFragment.hasPermissions;

public class EditContactFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_CODE = 1;
    String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int TAKE_CONTACT_PHOTO = 2;
    private static final int CHOOSE_CONTACT_PHOTO = 3;
    private static final int TAKE_BUSINESS_CARD_PHOTO = 4;
    private static final int CHOOSE_BUSINESS_CARD_PHOTO = 5;

    private Uri contactImageUri;
    private String mCurrentPhotoPath;
    private Bitmap contactBitmap;
    private Uri businessCardImageUri;
    private Bitmap businessCardBitmap;

    private Database database;
    private Contact contact;
    private ArrayList<PhoneNumber> getPhoneNumbersCopy;
    private RecyclerView additionalPhoneNumbers;

    private ImageView contactPhoto;
    private TextInputEditText firstNameField;
    private TextInputEditText lastNameField;
    private ImageView phoneIcon;
    private Spinner phonetype;
    private TextInputEditText phoneNumber;
    private MaterialButton addAnotherPhoneNumberBtn;
    private TextInputEditText emailField;
    private TextInputEditText companyField;
    private TextInputEditText addressField;
    private ImageView businessCard;

    private TextInputLayout firstNameLayout;
    private TextInputLayout lastNameLayout;
    private TextInputLayout phoneNumberLayout;
    private TextInputLayout emailLayout;

    private PhoneNumbersAdapter phoneNumbersAdapter;

    public EditContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_contact, container, false);

        database = Database.getInstance(getActivity());
        int rowid = getArguments().getInt("rowid");

        //Toolbar settings
        Toolbar toolbar = view.findViewById(R.id.toolbar_edit_contact);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        //get contact object from database
        contact = database.getContact(rowid);

        contactPhoto = view.findViewById(R.id.edit_contact_ImageView);
        firstNameField = view.findViewById(R.id.edit_contact_firstNameLabel);
        lastNameField = view.findViewById(R.id.edit_contact_lastNameLabel);
        phoneIcon = view.findViewById(R.id.add_contact_recyclerview_phoneIcon);
        phonetype = view.findViewById(R.id.add_contact_recyclerview_phoneType);
        phoneNumber = view.findViewById(R.id.add_contact_recyclerview_phoneNumber);
        emailField = view.findViewById(R.id.edit_contact_emailLabel);
        companyField = view.findViewById(R.id.edit_contact_companyLabel);
        addressField = view.findViewById(R.id.edit_contact_addressLabel);
        businessCard = view.findViewById(R.id.edit_contact_businessCard);

        if (contact.getPhoto() != null) {
            contactPhoto.setImageBitmap(contact.getDecodedPhoto(contact.getPhoto()));
            contactImageUri = Uri.parse(contact.getPhoto());
            contactBitmap = contact.getDecodedPhoto(contact.getPhoto());
        } else {
            contactPhoto.setImageResource(R.drawable.ic_account_circle_accent);
        }

        MaterialButton choosePhotoBtn = view.findViewById(R.id.edit_contact_choosePhotoBtn);
        choosePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hasPermissions(getContext(), PERMISSIONS)){
                    new android.support.v7.app.AlertDialog.Builder(getContext())
                            .setTitle(R.string.permissions_dialog_title)
                            .setMessage(R.string.permissions_dialog_message)
                            .setPositiveButton(R.string.permissions_dialog_positiveButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                } else {
                    if (contact.getPhoto() == null || contactImageUri == null) {
                        alertSetContactPhoto("contact");
                    } else {
                        alertChangeContactPhoto("contact");
                    }
                }
            }});

        firstNameField.setText(contact.getFirstName());
        lastNameField.setText(contact.getLastName());
        phoneIcon.setImageResource(R.drawable.ic_phone_accent);
        phonetype.setSelection(getSpinnerSelectionIndex(phonetype, database.getPhoneNumbers(contact).get(0).getType()));
        phoneNumber.setText(contact.getPhoneNumbers().get(0).getNumber());
        emailField.setText(contact.getEmail());
        companyField.setText(contact.getCompany());
        addressField.setText(contact.getAddress());

        //TextInputLayouts, used in FormValidator()
        firstNameLayout = view.findViewById(R.id.edit_contact_firstNameLayout);
        lastNameLayout = view.findViewById(R.id.edit_contact_lastNameLayout);
        phoneNumberLayout = view.findViewById(R.id.add_contact_phoneNumberLayout);
        emailLayout = view.findViewById(R.id.edit_contact_emailLayout);

        additionalPhoneNumbers = view.findViewById(R.id.edit_contact_recyclerview_phoneNumbersList);
        additionalPhoneNumbers.setLayoutManager(new LinearLayoutManager(getActivity()));
        getPhoneNumbersCopy = contact.getPhoneNumbers();
        //Remove first number since it's in a static TextView
        getPhoneNumbersCopy.remove(0);
        phoneNumbersAdapter = new PhoneNumbersAdapter(getPhoneNumbersCopy);
        additionalPhoneNumbers.setAdapter(phoneNumbersAdapter);

        //Swipe left to remove items in RecyclerView
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                getPhoneNumbersCopy.remove(viewHolder.getAdapterPosition());
                phoneNumbersAdapter.notifyDataSetChanged();
            }

        }).attachToRecyclerView(additionalPhoneNumbers);

        addAnotherPhoneNumberBtn = view.findViewById(R.id.edit_contact_addPhoneBtn);
        addAnotherPhoneNumberBtn.setOnClickListener(addAnotherPhoneNumber);

        if (contact.getBusinessCardPhoto() != null) {
            businessCard.setImageBitmap(contact.getDecodedPhoto(contact.getBusinessCardPhoto()));
            businessCardImageUri = Uri.parse(contact.getBusinessCardPhoto());
            businessCardBitmap = contact.getDecodedPhoto(contact.getBusinessCardPhoto());
        } else {
            businessCard.setImageResource(R.drawable.ic_work_accent);
        }

        MaterialButton chooseBusinessCardBtn = view.findViewById(R.id.edit_contact_chooseBusinessCardBtn);
        chooseBusinessCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hasPermissions(getContext(), PERMISSIONS)){
                    new android.support.v7.app.AlertDialog.Builder(getContext())
                            .setTitle(R.string.permissions_dialog_title)
                            .setMessage(R.string.permissions_dialog_message)
                            .setPositiveButton(R.string.permissions_dialog_positiveButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                } else {
                    if (contact.getBusinessCardPhoto() == null || businessCardImageUri == null) {
                        alertSetContactPhoto("businesscard");
                    } else {
                        alertChangeContactPhoto("businesscard");
                    }
                }
            }});

        ImageView saveBtn = view.findViewById(R.id.edit_contact_saveBtn);
        saveBtn.setOnClickListener(updateContact);

        return view;
    }

    private View.OnClickListener addAnotherPhoneNumber = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PhoneNumber emptyPhoneNumberObject = new PhoneNumber("", "Mobile");
            getPhoneNumbersCopy.add(emptyPhoneNumberObject);
            phoneNumbersAdapter.notifyDataSetChanged();
        }
    };

    private View.OnClickListener updateContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard(getActivity());
            if (FormValidator()) {

                ArrayList<PhoneNumber> AllPhoneNumbers = phoneNumbersAdapter.getNumbers();
                PhoneNumber primaryPhone = new PhoneNumber(phoneNumber.getText().toString().replaceAll("[^0-9]", ""), phonetype.getSelectedItem().toString());
                AllPhoneNumbers.add(0, primaryPhone);

                Contact updatedContact = new Contact(
                        capitalize(firstNameField.getText().toString().trim()),
                        capitalize(lastNameField.getText().toString().trim()),
                        AllPhoneNumbers,
                        emailField.getText().toString().trim().length() > 0 ? emailField.getText().toString().trim() : null,
                        companyField.getText().toString().trim().length() > 0 ? companyField.getText().toString().trim() : null,
                        addressField.getText().toString().trim().length() > 0 ? addressField.getText().toString().trim() : null,
                        encodeBitmap(contactBitmap),
                        encodeBitmap(businessCardBitmap)
                );

                int rowid = getArguments().getInt("rowid");
                database.updateContact(updatedContact, rowid);

                Toast.makeText(getActivity(), R.string.edit_contact_toast_contactUpdated,
                        Toast.LENGTH_LONG).show();

                getActivity().onBackPressed();
            }
        }
    };

    private int getSpinnerSelectionIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equals(myString)){
                return i;
            }
        }

        return 0;
    }

    public boolean FormValidator() {
        firstNameLayout.setErrorEnabled(false);
        lastNameLayout.setErrorEnabled(false);
        phoneNumberLayout.setErrorEnabled(false);
        emailLayout.setErrorEnabled(false);

        if (firstNameField.getText().toString().length() == 0) {
            firstNameLayout.setError("Required");
            return false;
        }

        if (lastNameField.getText().toString().length() == 0) {
            lastNameLayout.setError("Required");
            return false;
        }

        if (phoneNumber.getText().toString().length() == 0) {
            phoneNumberLayout.setError("Required");
            return false;
        }

        Matcher isMailValid = Pattern.compile("^(.+)@(.+)$").matcher(emailField.getText().toString());
        if (emailField.getText().toString().length() !=0 && !isMailValid.matches()) {
            emailLayout.setError("Invalid email");
            return false;
        }

        return true;
    }

    public String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void alertSetContactPhoto(final String type) {
        final String[] listItems = {getContext().getString(R.string.alert_set_photo_item_takePhoto), getContext().getString(R.string.alert_set_photo_item_chooseFromGallery)};
        new android.support.v7.app.AlertDialog.Builder(getContext())
                .setTitle(R.string.alert_set_photo_title)
                .setItems(listItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {

                        if (listItems[index].equals(getString(R.string.alert_set_photo_item_takePhoto))) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                // Create the File where the photo should go
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile();
                                } catch (IOException e) {
                                    // Error occurred while creating the File
                                }
                                // Continue only if the File was successfully created
                                if (photoFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(getContext(),
                                            "com.nicuz.rubrica.fileprovider",
                                            photoFile);
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    if (type.equals("contact")) {
                                        startActivityForResult(intent, TAKE_CONTACT_PHOTO);
                                    } else if (type.equals("businesscard")) {
                                        startActivityForResult(intent, TAKE_BUSINESS_CARD_PHOTO);
                                    }
                                }
                            }

                        } else if (listItems[index].equals(getString(R.string.alert_set_photo_item_chooseFromGallery))) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            if (type.equals("contact")) {
                                startActivityForResult(intent, CHOOSE_CONTACT_PHOTO);
                            } else if (type.equals("businesscard")) {
                                startActivityForResult(intent, CHOOSE_BUSINESS_CARD_PHOTO);
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private void alertChangeContactPhoto(final String photoType) {
        final String[] listItems = {getContext().getString(R.string.alert_change_photo_item_removePhoto),
                getContext().getString(R.string.alert_set_photo_item_takePhoto),
                getContext().getString(R.string.alert_set_photo_item_chooseFromGallery)};
        new android.support.v7.app.AlertDialog.Builder(getContext())
                .setTitle(R.string.alert_change_photo_title)
                .setItems(listItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {

                        if (listItems[index].equals((getString(R.string.alert_change_photo_item_removePhoto)))) {
                            if (photoType.equals("contact")) {
                                contactPhoto.setImageResource(R.drawable.ic_account_circle_accent);
                                contactImageUri = null;
                                contactBitmap = null;
                            } else if (photoType.equals("businesscard")) {
                                businessCard.setImageResource(R.drawable.ic_account_circle_accent);
                                businessCardImageUri = null;
                                businessCardBitmap = null;
                            }
                        } else if (listItems[index].equals(getString(R.string.alert_set_photo_item_takePhoto))) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                // Create the File where the photo should go
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile();
                                } catch (IOException e) {
                                    // Error occurred while creating the File
                                }
                                // Continue only if the File was successfully created
                                if (photoFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(getContext(),
                                            "com.nicuz.rubrica.fileprovider",
                                            photoFile);
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    if (photoType.equals("contact")) {
                                        startActivityForResult(intent, TAKE_CONTACT_PHOTO);
                                    } else if (photoType.equals("businesscard")) {
                                        startActivityForResult(intent, TAKE_BUSINESS_CARD_PHOTO);
                                    }
                                }
                            }

                        } else if (listItems[index].equals(getString(R.string.alert_set_photo_item_chooseFromGallery))) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            if (photoType.equals("contact")) {
                                startActivityForResult(intent, CHOOSE_CONTACT_PHOTO);
                            } else if (photoType.equals("businesscard")) {
                                startActivityForResult(intent, CHOOSE_BUSINESS_CARD_PHOTO);
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Rubrica_" + timestamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CHOOSE_CONTACT_PHOTO) {
            contactImageUri = data.getData();

            contactPhoto.setImageBitmap(compressBitmap(
                    contactImageUri,
                    contactPhoto.getWidth(),
                    contactPhoto.getHeight(),
                    "choose"));

            contactBitmap = compressBitmap(
                    contactImageUri,
                    contactPhoto.getWidth(),
                    contactPhoto.getHeight(),
                    "choose");

            contactPhoto.setImageBitmap(contactBitmap);
            data.setData(null);

        } else if (resultCode == RESULT_OK && requestCode == TAKE_CONTACT_PHOTO) {
            contactImageUri = Uri.parse(mCurrentPhotoPath);

            contactBitmap = compressBitmap(
                    contactImageUri,
                    contactPhoto.getWidth(),
                    contactPhoto.getHeight(),
                    "take");

            contactPhoto.setImageBitmap(contactBitmap);
        } else if (resultCode == RESULT_OK && requestCode == CHOOSE_BUSINESS_CARD_PHOTO) {
            businessCardImageUri = data.getData();

            businessCard.setImageBitmap(compressBitmap(
                    businessCardImageUri,
                    businessCard.getWidth(),
                    businessCard.getHeight(),
                    "choose"));

            businessCardBitmap = compressBitmap(
                    businessCardImageUri,
                    businessCard.getWidth(),
                    businessCard.getHeight(),
                    "choose");

            businessCard.setImageBitmap(businessCardBitmap);
            data.setData(null);

        } else if (resultCode == RESULT_OK && requestCode == TAKE_BUSINESS_CARD_PHOTO) {
            businessCardImageUri = Uri.parse(mCurrentPhotoPath);

            businessCardBitmap = compressBitmap(
                    businessCardImageUri,
                    businessCard.getWidth(),
                    businessCard.getHeight(),
                    "take");

            businessCard.setImageBitmap(businessCardBitmap);
        }
    }

    public Bitmap compressBitmap(Uri imageUri, int viewWidth, int viewHeight, String action) {

        Bitmap compressedBitmap = null;
        Bitmap bitmap = null;
        try {
            //Get bitmap from URI
            if (action.equals("choose")) {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            } else if (action.equals("take")) {
                bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            }

            //Compress bitmap
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            byte[] byteArray = out.toByteArray();
            InputStream inputStream = new ByteArrayInputStream(byteArray);

            //Resize bitmap to fit the view
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (action.equals("choose")) {
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
            } else if (action.equals("take")) {
                options.inJustDecodeBounds = false;
                BitmapFactory.decodeFile(mCurrentPhotoPath, options);
            }

            int scaleFactor = Math.min(options.outWidth/viewWidth, options.outHeight/viewHeight);
            //Keep size if scaleFactor equals zero
            if (scaleFactor == 0) { scaleFactor = 1; }

            compressedBitmap = Bitmap.createScaledBitmap(bitmap, options.outWidth/scaleFactor, options.outHeight/scaleFactor, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return compressedBitmap;
    }
    public String encodeBitmap(Bitmap bitmap){
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        }
        return null;
    }

}
