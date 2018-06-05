package project.messagingapp.amazon.amazonproject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Priya on 18-05-2018.
 */

public class ChatAdapter extends ArrayAdapter<HashMap<String,String>> {

    Context c;
    String n;
    private ArrayList<HashMap<String,String>> myarray;

    ChatAdapter(Context c, ArrayList<HashMap<String,String>> values){
        super(c,R.layout.chatrow,values);
        this.c = c;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View l = convertView;
        final HashMap<String,String> userdetails =getItem(position);
        View pl = parent;

        if (l == null) {
            l = LayoutInflater.from(getContext()).inflate(
                    R.layout.chatrow, parent, false);
        }
        TextView username = l.findViewById(R.id.usernamechat);
        //final TextView newmsgs = l.findViewById(R.id.newm);
        TextView onlinestatus = l.findViewById(R.id.onlinestatuschat);
        ImageView profilepicture = l.findViewById(R.id.propic);

        username.setText(userdetails.get("username"));
        onlinestatus.setText(userdetails.get("onlinestatus"));
        Glide.with(c)
                .load(userdetails.get("profilepic"))
                .into(profilepicture);


        String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String partnerid = userdetails.get("UID");
        String url = "https://messagingapp-48a08.firebaseio.com/messages/"+myid+"_"+partnerid+".json";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject obj = new JSONObject(s);
                    n = (String) obj.get("newmsg");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new com.android.volley.Response.ErrorListener(){
             @Override
             public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
             }
        });
        RequestQueue rQueue = Volley.newRequestQueue(getContext());
        rQueue.add(request);

        //newmsgs.setText(n);
        return l;
    }

}
