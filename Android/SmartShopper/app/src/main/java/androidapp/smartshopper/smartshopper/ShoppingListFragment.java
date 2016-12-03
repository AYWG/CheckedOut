package androidapp.smartshopper.smartshopper;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShoppingListFragment extends Fragment {
    private Context context;
    private ProductAdapter adapter;
    private List<Product> cartItems = new ArrayList<Product>();
    private double totalPrice;
    private String[] listNameOpts = {};
    private String email;
    private boolean loggedIn;
    private String currList = "";

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    public ShoppingListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        currList = sharedPref.getString("curr_list", "");
        email = sharedPref.getString(getString(R.string.curr_user), "");
        loggedIn = sharedPref.getBoolean(getString(R.string.login_stat), false);

        if(loggedIn) {

            try {
                String getListsReq = new RequestBuilder().buildGetListNamesJSON(email);
                String getAllListResp = new SendRequest().execute(getListsReq).get();

                JSONObject respJSON = new JSONObject(getAllListResp);
                String stat = respJSON.getString("status");

                //JSONArray listsArray = respJSON.getJSONArray("list_names");
                JSONObject listNamesJSON = new JSONObject();
                Toast.makeText(getActivity(), stat, Toast.LENGTH_SHORT).show();


                if(stat.equals("success")) {
                    List<String> listNames = new JSONParser().parseListNames(getAllListResp);
                    listNames.add("Add New List");
                    listNameOpts = listNames.toArray(new String[0]);

                    editor.putString("list_names", getAllListResp);
                    editor.commit();
                }
                else {
                    Toast.makeText(getActivity(), "Shopping Lists Cannot be Retrieved", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        adapter = new ProductAdapter(this.context, R.layout.search_result, new ArrayList<Product>());
        ListView list = (ListView) view.findViewById(R.id.cart_list);
        list.setAdapter(adapter);

        final TextView total = (TextView) view.findViewById(R.id.cart_summary);
        total.setText("Total: " + Double.toString(round(totalPrice, 2)));

        final Spinner listNameSpin = (Spinner) view.findViewById(R.id.all_usr_list);
        ArrayAdapter<String> listNamesAdpt = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, listNameOpts);
        listNameSpin.setAdapter(listNamesAdpt);

        final Button saveList = (Button) view.findViewById(R.id.new_list_button);

        listNameSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String listSelected = listNameOpts[position];
                if(listSelected == "Add New List") {
                    editor.putString("curr_list", "default_list");
                }
                else {
                    editor.putString("curr_list", listSelected);
                    if(sharedPref.contains(listSelected)) {
                        String cartString = sharedPref.getString(listSelected, "");
                        try {
                            JSONObject cartJSON = new JSONObject(cartString);

                            cartItems = new JSONParser().parseCart(cartString);
                            totalPrice = cartJSON.getDouble("total_price");

                            adapter.updateProductList(cartItems);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        String defaultUser = "";
                        String user = sharedPref.getString(getString(R.string.curr_user), defaultUser);
                        String defaultList = "";
                        String list = sharedPref.getString("cart", defaultList);
                        String request = new RequestBuilder().buildGetListReq(user, listSelected);

                        try {
                            String jsonResponse = new SendRequest().execute(request).get();
                            String stat = new JSONObject(jsonResponse).getString("status");

                            if(stat == "success") {
                                cartItems = new JSONParser().parseCart(jsonResponse);
                                adapter.updateProductList(cartItems);

                                editor.putString(listSelected, jsonResponse);
                                editor.commit();
                            }
                            else {
                                //put toast here
                            }
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                return;
            }
        });

        saveList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                String newListName = listNameEntry.getText().toString();
                String defaultUser = "";
                String user = sharedPref.getString(getString(R.string.curr_user), defaultUser);
                String defaultList = "";
                String list = sharedPref.getString("cart", defaultList);
                String request = new RequestBuilder().buildSaveListReq(user, newListName, list);

                try {
                    String jsonResponse = new SendRequest().execute(request).get();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
                }
                */
                FragmentManager fm = getFragmentManager();
                CreateListFragment newListDialog = new CreateListFragment();
                newListDialog.show(fm, "fragment_new_list");
            }
        });

        if(loggedIn) {
            if(currList.equals("")) {
                currList = "default_list";
            }

            

            if (cartItems != null) {
                adapter.updateProductList(cartItems);

                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Product selected = cartItems.get(position);

                        ShopListHandler listHandler = new ShopListHandler(getActivity(), "default_list");
                        List<Product> updatedList = listHandler.deleteFromList(selected);
                        double newTotal = listHandler.getListTotal();

                        adapter.updateProductList(updatedList);
                        total.setText("Total: " + Double.toString(round(newTotal, 2)));
                    }
                });
            }
        }
        else {
            try {
                String defaultVal = "";
                final String cartString = sharedPref.getString("default_list", defaultVal);
                JSONObject cartJSON = new JSONObject(cartString);

                cartItems = new JSONParser().parseCart(cartString);
                totalPrice = cartJSON.getDouble("total_price");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cartItems != null) {
                adapter.updateProductList(cartItems);

                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Product selected = cartItems.get(position);

                        ShopListHandler listHandler = new ShopListHandler(getActivity(), "default_list");
                        List<Product> updatedList = listHandler.deleteFromList(selected);
                        double newTotal = listHandler.getListTotal();

                        adapter.updateProductList(updatedList);
                        total.setText("Total: " + Double.toString(round(newTotal, 2)));
                    }
                });
            }
        }

        return view;
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private class SendRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... request) {
            SmartShopClient client = new SmartShopClient(getActivity());
            if(client.getStatus())
                return client.sendRequest(request[0]);
            else
                return "Connection Not Established";
        }

        @Override
        protected void onPostExecute(String request) {
            super.onPostExecute(request);
        }
    }
}
