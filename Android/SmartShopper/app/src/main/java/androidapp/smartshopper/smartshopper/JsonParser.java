package androidapp.smartshopper.smartshopper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by JohnS on 2016-10-23.
 */

public class JsonParser {
    private String ITEMS_TAG = "items";
    private String COLUMN_TAG = "col_name_1";
    private String NAME_TAG = "name";
    private String PRICE_TAG = "price";
    private String STORE_TAG = "store";
    private String URL_TAG = "image";

    private String CART_TAG = "cart_list";
    private String QUANTITY_TAG = "quantity";

    public JsonParser() {}

    public List<Product> parseJSON(String json) {
        if(json != null) {
            List<Product> parseList = new ArrayList<Product>();

            try{
                JSONObject jsonObj = new JSONObject(json);
                JSONArray items = jsonObj.getJSONArray(ITEMS_TAG);

                for(int i = 0; i < items.length(); i++) {
                    //JSONObject currColle = collections.getJSONObject(i);
                    JSONArray currColle = items.getJSONArray(i);

                    //Iterator<?> colleKeys = currColle.keys();
                    for(int j = 0; j < currColle.length(); j++){
                        JSONObject currItem = currColle.getJSONObject(j);
                        JSONObject currData = currItem.getJSONObject("data");

                        String name = currData.getString(NAME_TAG);
                        String price = currData.getString(PRICE_TAG);
                        String store = currData.getString(STORE_TAG);
                        String img = currData.getString(URL_TAG);

                        if(!img.substring(0, 1).equals("h"))
                            img = "http:" + img;
                        //String htmlImg = img.substring(2, img.length());

                        Product currProduct = new Product(name, price, store, img, null);
                        parseList.add(currProduct);
                    }
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

            return parseList;
        }
        else {
            return null;
        }
    }

    public List<Product> parseCart(String json) {
        if(json != null) {
            List<Product> parseList = new ArrayList<Product>();

            try{
                JSONObject jsonObj = new JSONObject(json);
                JSONArray items = jsonObj.getJSONArray(CART_TAG);

                for(int i = 0; i < items.length(); i++) {
                    JSONObject currItem = items.getJSONObject(i);

                    String name = currItem.getString(NAME_TAG);
                    String price = currItem.getString(PRICE_TAG);
                    String store = currItem.getString(STORE_TAG);
                    String img = currItem.getString(URL_TAG);
                    String quantity = currItem.getString(QUANTITY_TAG);

                    Product currProduct = new Product(name, price, store, img, quantity);
                    parseList.add(currProduct);
                }

            }catch (JSONException e) {
                e.printStackTrace();
            }

            return parseList;
        }
        else {
            return null;
        }
    }
}
