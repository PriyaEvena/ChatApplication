package project.messagingapp.amazon.amazonproject;

/**
 * Created by sunny on 4/5/2018.
 */

public class Cust {
    static String username;
    static String phoneno;
    static String type;
    static String photouri;
    static String chatwith="";
    Cust(String username, String phoneno, String photouri,String type) {
        photouri = photouri;
        phoneno = phoneno;
        type = type;
        username = username;
    }
    public static String getUsername(){
        return username;
    }
    public static String getPhoneno(){
        return phoneno;
    }
    static String getType(){
        return type;
    }
}
