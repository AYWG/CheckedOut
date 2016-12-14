package androidapp.smartshopper.smartshopper;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    private Context context;
    //private EditText idField;
    //private EditText passField;
    //private Button login;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        //get Java reference to UI elements
        final EditText idField = (EditText) view.findViewById(R.id.id);
        final EditText passField = (EditText) view.findViewById(R.id.pw);
        final Button login = (Button) view.findViewById(R.id.login_button);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get text entered into the available fields
                String id = idField.getText().toString();
                String pw = passField.getText().toString();
                //build login request
                String loginReq = new RequestBuilder().buildLoginReq(id, pw);
                try {
                    //send request and obtain response
                    String jsonResponse = new SendRequest(getActivity()).execute(loginReq).get();

                    //get status of response and name of user logged in
                    JSONObject respJSON = new JSONObject(jsonResponse);
                    String status = respJSON.getString("status");
                    String name = respJSON.getString("name");

                    //display toast message corresponding to status
                    if(status.equals("DNE"))
                        Toast.makeText(getActivity(), "Wrong Username", Toast.LENGTH_SHORT).show();
                    else if(status.equals("failed"))
                        Toast.makeText(getActivity(), "Wrong Password", Toast.LENGTH_SHORT).show();
                    else if(status.equals("exception"))
                        Toast.makeText(getActivity(), "Kooner's Fault", Toast.LENGTH_SHORT).show();
                    else if(status.equals("Not Verified"))
                        Toast.makeText(getActivity(), "Please Check Email for Verification", Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(getActivity(), "Login Successful", Toast.LENGTH_SHORT).show();

                        //put email and name into shared preferences
                        SharedPrefSingle sharedPref = SharedPrefSingle.getInstance(getActivity());
                        sharedPref.put(SharedPrefSingle.prefKey.CURR_EMAIL, id);
                        sharedPref.put(SharedPrefSingle.prefKey.CURR_NAME, name);
                        //set login status as true
                        sharedPref.put(SharedPrefSingle.prefKey.LOGIN_STAT, true);
                        getActivity().onBackPressed();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        //launch account creation fragment when corresponding button pressed
        TextView accCreate = (TextView) view.findViewById(R.id.createAcc);
        accCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccCreateFragment createAccFrag = new AccCreateFragment();

                FragmentManager fragMan = getFragmentManager();
                fragMan.beginTransaction()
                        .replace(R.id.result_frame, createAccFrag)
                        .addToBackStack("account_creation")
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .commit();
            }
        });

        //launch account verify fragment when corresponding button pressed
        TextView accVerify = (TextView) view.findViewById(R.id.verifyAcc);
        accVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccVerifyFragment verifyAccFrag = new AccVerifyFragment();

                FragmentManager fragMan = getFragmentManager();
                fragMan.beginTransaction()
                        .replace(R.id.result_frame, verifyAccFrag)
                        .addToBackStack("account_verify")
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .commit();
            }
        });

        return view;
    }
}
