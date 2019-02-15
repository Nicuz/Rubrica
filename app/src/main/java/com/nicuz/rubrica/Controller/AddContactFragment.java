package com.nicuz.rubrica.Controller;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

public class AddContactFragment extends Fragment {

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

    private ArrayList<PhoneNumber> additionalPhoneNumbers;
    private PhoneNumbersAdapter phoneNumbersAdapter;

    private ImageView contactPhoto;
    private TextInputEditText firstNameField;
    private TextInputEditText lastNameField;
    private Spinner phonetype;
    private TextInputEditText phoneNumber;
    private TextInputEditText emailField;
    private TextInputEditText companyField;
    private TextInputEditText addressField;
    private ImageView businessCard;

    private TextInputLayout firstNameLayout;
    private TextInputLayout lastNameLayout;
    private TextInputLayout phoneNumberLayout;
    private TextInputLayout emailLayout;

    public AddContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_contact, container, false);

        //Toolbar settings
        Toolbar toolbar = view.findViewById(R.id.toolbar_add_contact);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        //Database instance
        final Database database = Database.getInstance(getActivity());

        contactPhoto = view.findViewById(R.id.add_contact_ImageView);
        contactPhoto.setImageResource(R.drawable.ic_account_circle_accent);

        ImageView accountIcon = view.findViewById(R.id.add_contact__account_icon);
        accountIcon.setImageResource(R.drawable.ic_account_circle_accent);
        final ImageView phoneIcon = view.findViewById(R.id.add_contact_recyclerview_phoneIcon);
        phoneIcon.setImageResource(R.drawable.ic_phone_accent);
        ImageView emailIcon = view.findViewById(R.id.contact_details_email_icon);
        emailIcon.setImageResource(R.drawable.ic_email_accent);
        ImageView companyIcon = view.findViewById(R.id.add_contact_company_icon);
        companyIcon.setImageResource(R.drawable.ic_work_accent);
        ImageView locationIcon = view.findViewById(R.id.add_contact_location_icon);
        locationIcon.setImageResource(R.drawable.ic_location_accent);
        businessCard = view.findViewById(R.id.add_contact_businessCard);
        businessCard.setImageResource(R.drawable.ic_work_accent);

        final MaterialButton choosePhotoBtn = view.findViewById(R.id.add_contact_choosePhotoBtn);
        choosePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hasPermissions(getContext(), PERMISSIONS)){
                    new android.support.v7.app.AlertDialog.Builder(getContext())
                            .setTitle("Permission needed")
                            .setMessage("Rubrica asks for camera and storage permission to let the user choose or take a photo for new contacts")
                            .setPositiveButton("I got it", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                } else {
                    if (contactImageUri == null) {
                        alertSetContactPhoto("contact");
                    } else {
                        alertChangeContactPhoto("contact");
                    }
                }
        }});

        //Dynamic rows for additional phone numbers
        additionalPhoneNumbers = new ArrayList<>();
        RecyclerView dynamicRowsRecyclerView = view.findViewById(R.id.add_contact_recyclerview_phoneNumbersList);
        dynamicRowsRecyclerView.setNestedScrollingEnabled(false);
        dynamicRowsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        phoneNumbersAdapter = new PhoneNumbersAdapter(additionalPhoneNumbers);
        dynamicRowsRecyclerView.setAdapter(phoneNumbersAdapter);

        //Swipe left to remove items in Recyclerview
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                additionalPhoneNumbers.remove(viewHolder.getAdapterPosition());
                phoneNumbersAdapter.notifyDataSetChanged();
            }

        }).attachToRecyclerView(dynamicRowsRecyclerView);

        //Add dynamic fields for additional phone numbers
        MaterialButton addPhone = view.findViewById(R.id.add_contact_addPhoneBtn);
        addPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneNumber emptyPhoneNumberObject = new PhoneNumber("", "Mobile");
                additionalPhoneNumbers.add(emptyPhoneNumberObject);
                phoneNumbersAdapter.notifyDataSetChanged();
            }
        });

        final MaterialButton chooseBusinessCardBtn = view.findViewById(R.id.add_contact_chooseBusinessCardBtn);
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
                    if (businessCardImageUri == null) {
                        alertSetContactPhoto("businesscard");
                    } else {
                        alertChangeContactPhoto("businesscard");
                    }
                }
            }});


        //Textfields
        firstNameField = view.findViewById(R.id.add_contact_firstNameLabel);
        lastNameField = view.findViewById(R.id.add_contact_lastNameLabel);
        phonetype = view.findViewById(R.id.add_contact_recyclerview_phoneType);
        phoneNumber = view.findViewById(R.id.add_contact_recyclerview_phoneNumber);
        emailField = view.findViewById(R.id.add_contact_emailLabel);
        companyField = view.findViewById(R.id.add_contact_companyLabel);
        addressField = view.findViewById(R.id.add_contact_addressLabel);

        //TextInputLayouts, used in FormValidator()
        firstNameLayout = view.findViewById(R.id.add_contact_firstNameLayout);
        lastNameLayout = view.findViewById(R.id.add_contact_lastNameLayout);
        phoneNumberLayout = view.findViewById(R.id.add_contact_phoneNumberLayout);
        emailLayout = view.findViewById(R.id.add_contact_emailLayout);

        ImageView saveBtn = view.findViewById(R.id.edit_contact_saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard(getActivity());
                if (FormValidator()) {

                    ArrayList<PhoneNumber> allPhoneNumbers = phoneNumbersAdapter.getNumbers();
                    PhoneNumber primaryPhone = new PhoneNumber(phoneNumber.getText().toString().replaceAll("[^0-9]", ""), phonetype.getSelectedItem().toString());
                    allPhoneNumbers.add(0, primaryPhone);

                    Contact contact = new Contact(
                            capitalize(firstNameField.getText().toString().trim()),
                            capitalize(lastNameField.getText().toString().trim()),
                            allPhoneNumbers,
                            emailField.getText().toString().trim(),
                            companyField.getText().toString().trim(),
                            addressField.getText().toString().trim(),
                            encodeBitmap(contactBitmap),
                            encodeBitmap(businessCardBitmap)
                            );

                    if (database.addContact(contact) != -1) {
                        Toast.makeText(getActivity(), R.string.add_contact_toast_contactSaved,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.add_contact_toast_contactAlreadyExists,
                                Toast.LENGTH_LONG).show();
                    }

                    getActivity().onBackPressed();
                }

            }});

        return view;
    }

    public boolean FormValidator() {
        firstNameLayout.setErrorEnabled(false);
        lastNameLayout.setErrorEnabled(false);
        phoneNumberLayout.setErrorEnabled(false);
        emailLayout.setErrorEnabled(false);

        if (firstNameField.getText().toString().length() == 0) {
            firstNameLayout.setError(getContext().getString(R.string.contact_form_required_error));
            return false;
        }

        if (lastNameField.getText().toString().length() == 0) {
            lastNameLayout.setError(getContext().getString(R.string.contact_form_required_error));
            return false;
        }

        if (phoneNumber.getText().toString().length() == 0) {
            phoneNumberLayout.setError(getContext().getString(R.string.contact_form_required_error));
            return false;
        }

        Matcher isMailValid = Pattern.compile("^(.+)@(.+)$").matcher(emailField.getText().toString());
        if (emailField.getText().toString().length() !=0 && !isMailValid.matches()) {
            emailLayout.setError(getContext().getString(R.string.contact_form_invalidEmail));
            return false;
        }

        return true;
    }

    public String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public static boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
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
                            e.printStackTrace();
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
        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
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

        // Save file: path for use with ACTION_VIEW intents
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
