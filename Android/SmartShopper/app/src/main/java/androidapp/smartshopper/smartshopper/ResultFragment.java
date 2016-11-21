package androidapp.smartshopper.smartshopper;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResultFragment extends ListFragment {
    private final String ARG_KEY = "json_response";

    //private ListView listView;
    private String jsonResp;
    private List<Product> result;
    private Context context;

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.context = getActivity();

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            jsonResp = bundle.getString(ARG_KEY);
        }

        if(jsonResp == null) {
            Toast.makeText(getActivity(), "The Item You Searched For Doesn't Exist :(", Toast.LENGTH_LONG).show();
        }

        if(jsonResp.equals("Connection Not Established")) {
            Toast.makeText(getActivity(), jsonResp, Toast.LENGTH_LONG).show();
        }

        JSONParser parser = new JSONParser();
        this.result = parser.parseJSON(jsonResp);

        if(result.isEmpty()) {
            Toast.makeText(getActivity(), "The Item You Searched For Doesn't Exist :(", Toast.LENGTH_LONG).show();
        }

        ProductAdapter adapter = new ProductAdapter(this.context, R.layout.search_result, this.result);
        this.setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        //ProductAdapter adapter = (ProductAdapter) l.getAdapter();
        Product selected = this.result.get(position);
        String productJSON = selected.toJSON();

        Bundle bundle = new Bundle();
        bundle.putString("product_json", productJSON);
        DetailFragment newDetailFrag = new DetailFragment();
        newDetailFrag.setArguments(bundle);

        FragmentManager fragMan = getFragmentManager();
        fragMan.beginTransaction()
                .replace(R.id.result_frame, newDetailFrag)
                .addToBackStack("product_detail")
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .commit();
    }

    private boolean addToCart(Product toAdd) {
        try{
            JSONObject jsonAppend = new JSONObject();
            jsonAppend.put("name", toAdd.getName());
            jsonAppend.put("price", toAdd.getPrice());
            jsonAppend.put("store", toAdd.getStore());
            jsonAppend.put("image", toAdd.getImg());
            jsonAppend.put("url", toAdd.getURL());

            String fileName = getResources().getString(R.string.cart_file_name);
            File file = this.context.getFileStreamPath(fileName);

            if(file.exists()) {
                FileInputStream inputStream = this.context.openFileInput(fileName);

                StringBuilder builder = new StringBuilder();
                int ch;
                while((ch = inputStream.read()) != -1) {
                    builder.append((char)ch);
                }
                inputStream.close();
                String cartString = builder.toString();
                //Toast.makeText(getActivity(), cartString, Toast.LENGTH_SHORT).show();

                boolean alreadyAdded = false;
                List<Product> currCart = new JSONParser().parseCart(cartString);

                for(int i = 0; i < currCart.size(); i++) {
                    Product currProduct = currCart.get(i);
                    System.out.println("image1:" + currProduct.getImg());
                    System.out.println("image2:" + toAdd.getImg());
                    if(currProduct.getImg().equals(toAdd.getImg())) {
                        alreadyAdded = true;
                    }
                }

                JSONObject cartJSON = new JSONObject(cartString);
                JSONArray cartArray = cartJSON.getJSONArray("cart_list");
                if(!alreadyAdded) {
                    jsonAppend.put("quantity", "1");
                    cartArray.put(jsonAppend);

                    double totalPrice = cartJSON.getDouble("total_price");
                    totalPrice += Double.parseDouble(toAdd.getPrice());
                    cartJSON.put("total_price", Double.toString(totalPrice));

                    String newCartString = cartJSON.toString(2);
                    byte[] buffer = newCartString.getBytes();
                    //StandardCharsets.US_ASCII

                    FileOutputStream outputStream = this.context.openFileOutput(fileName, Context.MODE_PRIVATE);
                    outputStream.write(buffer);
                    outputStream.close();
                }
                else {
                    for(int i = 0; i < cartArray.length(); i++) {
                        JSONObject currItem = cartArray.getJSONObject(i);
                        if(currItem.getString("image").equals(toAdd.getImg())) {
                            int quantity = currItem.getInt("quantity");
                            quantity += 1;
                            currItem.put("quantity", Integer.toString(quantity));

                            double totalPrice = cartJSON.getDouble("total_price");
                            totalPrice += Double.parseDouble(toAdd.getPrice());
                            cartJSON.put("total_price", Double.toString(totalPrice));

                            String newCartString = cartJSON.toString(2);
                            byte[] buffer = newCartString.getBytes();
                            //StandardCharsets.US_ASCII

                            FileOutputStream outputStream = this.context.openFileOutput(fileName, Context.MODE_PRIVATE);
                            outputStream.write(buffer);
                            outputStream.close();

                            break;
                        }
                    }
                }
            }
            else {
                File newFile = new File(this.context.getFilesDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(newFile);

                JSONObject jsonObj = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                jsonAppend.put("quantity", "1");
                jsonArray.put(jsonAppend);

                jsonObj.put("cart_list", jsonArray);
                jsonObj.put("total_price", toAdd.getPrice());

                String newCartString = jsonObj.toString();
                byte[] buffer = newCartString.getBytes();
                outputStream.write(buffer);
                outputStream.close();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}