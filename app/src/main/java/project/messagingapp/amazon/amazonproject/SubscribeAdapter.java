package project.messagingapp.amazon.amazonproject;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Priya on 03-06-2018.
 */

public class SubscribeAdapter extends ArrayAdapter<HashMap<String,String>> {

    Context c;
    String n;
    private ArrayList<HashMap<String,String>> myarray;

    SubscribeAdapter(Context c, ArrayList<HashMap<String,String>> values){
        super(c,R.layout.subsrow,values);
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
                    R.layout.subsrow, parent, false);
        }
        TextView username = l.findViewById(R.id.usernamesubs);
        ImageView profilepicture = l.findViewById(R.id.propicsubs);
        Button subscribe = l.findViewById(R.id.subscribe);

        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),PaymentActivity.class);
                i.putExtra("HashMap",userdetails);
                c.startActivity(i);
            }
        });

        username.setText(userdetails.get("username"));
        Glide.with(c)
                .load(userdetails.get("profilepic"))
                .into(profilepicture);


        String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String partnerid = userdetails.get("UID");


        return l;
    }

}
