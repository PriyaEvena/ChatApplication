package project.messagingapp.amazon.amazonproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

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
public class Tab1ChatsFragment extends Fragment {

    ListView usersList;
    TextView noUsersText;
    ArrayList<HashMap<String,String>> al = new ArrayList<>();

    static final String TAG = "Tab1ChatFragment";
    int totalUsers = 0;
    ProgressDialog pd;
    boolean owner;
    boolean doubleBackToExitPressedOnce= false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1chats, container, false);

        usersList = (ListView)rootView.findViewById(R.id.ListViewOfChats);
        noUsersText = (TextView)rootView.findViewById(R.id.NoUsers);

        pd = new ProgressDialog(getContext());
        pd.setMessage("Loading...");
       // pd.show();

        String url;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Cust", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String uname = sharedPreferences.getString("username","username");
        String type = sharedPreferences.getString("type","notfoundtype");

        Log.e(TAG,type);
        if(type.equals("BusinessOwner")){
            url = "https://messagingapp-48a08.firebaseio.com/Clients.json";
            Log.e(TAG,url);
            owner = true;
        }
        else{
            url = "https://messagingapp-48a08.firebaseio.com/BusinessOwner.json";
            Log.e(TAG,type);
            owner = false;
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

        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cust.chatwith = al.get(position).get("username");
                Intent i = new Intent(getContext(), ChatActivity.class);
                i.putExtra("HashMap",al.get(position));
                i.putExtra("UID",al.get(position).get("UID"));
                startActivity(i);
            }
        });
        return rootView;
    }

    public void doOnSuccess(String s){
        try {
            if(al.size()>0){
                al.clear();
            }
            JSONObject obj = new JSONObject(s);
            Iterator i = obj.keys();
            Log.e(TAG,obj.keys()+"");
            String key = "";
            while(i.hasNext()){
                key = i.next().toString();
                Log.e(TAG,key);
                HashMap<String,String> values = new HashMap<>();
                values.put("username",obj.getJSONObject(key).getString("username"));
                if(!owner) values.put("services",obj.getJSONObject(key).getString("servicesProvided"));
                values.put("onlinestatus",obj.getJSONObject(key).getString("status"));
                values.put("profilepic",obj.getJSONObject(key).getString("photouri"));
                values.put("UID",key);
                al.add(values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e(TAG,al+"");
            noUsersText.setVisibility(View.GONE);
            usersList.setAdapter(new ChatAdapter(getContext(),al));

        //pd.dismiss();
    }



}