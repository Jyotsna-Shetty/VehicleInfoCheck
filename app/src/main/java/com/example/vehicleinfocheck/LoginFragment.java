package com.example.vehicleinfocheck;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

// Auto generated comments regarding fragment creation to be ignored
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    // ignore parameters
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters


    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }
    }
    // Displays Login fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize EditText variables
        EditText PhoneText= view.findViewById(R.id.Phone);
        EditText PasswordText = view.findViewById(R.id.Password);

        // On clicking the Sign Up button on the login page, app redirects to VAHAN website
        view.findViewById(R.id.SignUpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Getintent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vahan.nic.in/nrservices/faces/user/citizen/createcitizenuser.xhtml"));
                startActivity(Getintent);

            }
        });

        // ON clicking the Login button, the API must be called to check the login credentials , for now the button redirects you to the capturing screen
        view.findViewById(R.id.LoginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ImageActivity.class);
                startActivity(intent);
            }
        });

        // TextChangedListener that detects any changes made in the 'Phone' EditText by user
        PhoneText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Auto generated method
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Auto generated method
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Auto generated method
                // Check for valid input
                if (PhoneText.getText().toString().trim().isEmpty()) {
                    PhoneText.setError("Phone number is required");
                }
                if (PhoneText.getText().length()>10) {
                    PhoneText.setError("Invalid Phone number");
                }
            }
        });

        // TextChangedListener that detects any changes made in the 'Password' EditText by user
        PasswordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Auto generated method
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Auto generated method
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Auto generated method
                // Check for valid input
                if (PasswordText.getText().toString().trim().isEmpty()) {
                    PasswordText.setError("Password is required");
                }
            }
        });
    }
}