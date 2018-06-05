package project.messagingapp.amazon.amazonproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Priya on 01-04-2018.
 */

public class Tab2ContactsFragment extends android.support.v4.app.Fragment {


    ArrayList<HashMap<String,String>> mylist = new ArrayList<>();
    static final String TAG = "Tab2ContactsFragment";
    ListView lv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2contacts, container, false);

        String url;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Cust", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String uname = sharedPreferences.getString("username","username");
        String type = sharedPreferences.getString("type","notfoundtype");
        lv = rootView.findViewById(R.id.ListViewofProfiles);

        if(type.equals("BusinessOwner")){
            url = "https://messagingapp-48a08.firebaseio.com/Clients.json";
            Log.e(TAG,url);
        }
        else{
            url = "https://messagingapp-48a08.firebaseio.com/BusinessOwner.json";
            Log.e(TAG,type);
        }
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                doOnSuccess(s);
            }
        },new com.android.volley.Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(getContext());
        rQueue.add(request);

        return rootView;
    }
    public void doOnSuccess(String s){
        try {
            if(mylist.size()>0){
                mylist.clear();
            }
            JSONObject obj = new JSONObject(s);
            Iterator i = obj.keys();
            String key = "";
            while(i.hasNext()){
                key = i.next().toString();
                HashMap<String,String> values = new HashMap<>();
                values.put("username",obj.getJSONObject(key).getString("username"));
                values.put("onlinestatus",obj.getJSONObject(key).getString("status"));
                values.put("profilepic",obj.getJSONObject(key).getString("photouri"));
                values.put("UID",key);
                mylist.add(values);
                Log.e(TAG,key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        lv.setAdapter(new ChatAdapter(getContext(),mylist));
    }
}