package com.example.vehicleinfocheck;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    EditText UsernameText, Pass1, Pass2, PhoneNo;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.SignUpButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* if (validateInput())  {
                    Toast myToast = Toast.makeText(getActivity(),"account Created", Toast.LENGTH_SHORT);
                    // Call the API
                }*/
            }
        });
    }

    boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    boolean isPasswordValid1(String s)  {
        return (Pattern.compile("[0-9]").matcher(s).find());
    }
    boolean isPasswordValid2(String s)  {
        return (Pattern.compile("[^A-Za-z0-9]").matcher(s).find());
    }


    boolean validateInput() {

        if (UsernameText.getText().toString()==null || UsernameText.getText().toString().trim().isEmpty()) {
            UsernameText.setError("Invalid Email (check for spaces)");
            return false;
        }
        if (Pass1.getText().toString()==null || Pass1.getText().toString().trim().isEmpty()) {
            Pass1.setError("Invalid Password");
            return false;
        }
        if (Pass1.getText().length() < 8) {
            Pass1.setError("Password must be at least 8 characters long");
            return false;
        }
        if (!isPasswordValid1(Pass1.getText().toString()) || !isPasswordValid2(Pass1.getText().toString())) {
            Pass1.setError("Password must contain at least 1 numeric and 1 special character");
            return false;
        }
        if (!Pass2.equals(Pass1)) {
            Pass2.setError("Password does not match");
            return false;
        }
        // checking the proper email format
        if (!isEmailValid(UsernameText.getText().toString())) {
            UsernameText.setError("Please Enter Valid Email");
            return false;
        }
        else return true;
    }
}