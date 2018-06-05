package project.messagingapp.amazon.amazonproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Priya on 01-04-2018.
 */

public class Tab3ProfileFragment extends Fragment {


    final ArrayList<String> mylist = new ArrayList<>();
    final ArrayList<String> uidlist = new ArrayList<>();
    private static String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static String TAG = "Tab3ProfileFragment";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab3profile, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Cust", Context.MODE_PRIVATE);
        String str = sharedPreferences.getString("username","username");
        TextView Dumm  = (TextView) rootView.findViewById(R.id.un);
        final ListView subslist = rootView.findViewById(R.id.ListViewofSubscribers);
        Button sendupdates = rootView.findViewById(R.id.sendupdatesbutton);
        Dumm.setText(str);

        if(sharedPreferences.getString("type","typenotfound").equals("Clients")){
            sendupdates.setVisibility(View.GONE);
            String url = "https://messagingapp-48a08.firebaseio.com/BusinessOwner.json";
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
                @Override
                public void onResponse(String s) {
                    ArrayList<HashMap<String,String>> mylist = new ArrayList<>();
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
                    subslist.setAdapter(new SubscribeAdapter(getContext(),mylist));
                }
            },new com.android.volley.Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    System.out.println("" + volleyError);
                }
            });
            RequestQueue rQueue = Volley.newRequestQueue(getContext());
            rQueue.add(request);

        }
        else {
            sendupdates.setVisibility(View.VISIBLE);
            String url = "https://messagingapp-48a08.firebaseio.com/BusinessOwner/" + myid + "/Subscribers.json";
            if (mylist.size() == 0) {
                StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            if (mylist.size() > 0) {
                                mylist.clear();
                            }
                            JSONObject obj = new JSONObject(s);
                            Iterator i = obj.keys();
                            String key = "";
                            while (i.hasNext()) {
                                key = i.next().toString();
                                Log.e(TAG, key + "");
                                uidlist.add(key);
                                String urlclient = "https://messagingapp-48a08.firebaseio.com/Clients/" + key + ".json";
                                StringRequest request = new StringRequest(Request.Method.GET, urlclient, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String s) {
                                        try {

                                            JSONObject obj = new JSONObject(s);
                                            Iterator i = obj.keys();
                                            String client = "";
                                            Log.e(TAG, obj.getString("username"));
                                            mylist.add(obj.getString("username"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new com.android.volley.Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {
                                        System.out.println("" + volleyError);
                                    }
                                });
                                RequestQueue rQueue = Volley.newRequestQueue(getContext());
                                rQueue.add(request);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        System.out.println("" + volleyError);
                    }
                });
                RequestQueue rQueue = Volley.newRequestQueue(getContext());
                rQueue.add(request);

            }

            Log.e(TAG, mylist + "");
            subslist.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, mylist));

            sendupdates.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getContext(),SendUpdatesActivity.class);
                    i.putExtra("uidarray",uidlist);
                    startActivity(i);
                }
            });
        }
        return rootView;
    }
}
