package product;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by dmhum_000 on 5/13/2016.
 */
public class ProductFeedLine {

    private static SimpleDateFormat dateFMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public final long accountID;
    public final long merchantGroupID;
    public final long merchantID;
    public final long productID;
    public final long modelID;
    public final String pageID;
    public final String variant;
    public final String locale;
    public final String name;
    public final String productURL;
    public final String imageURL;
    public final String description;
    public final String brand;
    public final String subBrand;
    public final String[] catPath;
    public final boolean inStock;
    public final long date;
    public final boolean discontinued;

    public ProductFeedLine(String input){

        List<String> result = new ArrayList<String>();
        int start = 0;
        boolean inQuotes = false;
        for (int current = 0; current < input.length(); current++) {
            if (input.charAt(current) == '\"') inQuotes = !inQuotes; // toggle state
            boolean atLastChar = (current == input.length() - 1);
            if(atLastChar) result.add(input.substring(start));
            else if (input.charAt(current) == ',' && !inQuotes) {
                int realCurrent = current;
                while(start!=current){
                    boolean done = true;
                    char x = input.charAt(start);
                    if (x == '"' || x == '\'' || x == ' '){
                        start++;
                        done = false;
                    }
                    x = input.charAt(current);
                    if (x == '"' || x == '\'' || x == ' '){
                        current--;
                        done = false;
                    }
                    if (done)
                        break;
                }

                result.add(input.substring(start,current));
                start = realCurrent + 1;
            }
        }

        try {
            accountID = Long.parseLong(result.get(0));
        }
        catch(NumberFormatException e){
                throw new NullPointerException("Bad AccountID.");
        }

        try {
            merchantGroupID = Long.parseLong(result.get(1));
        }
        catch(NumberFormatException e){
            throw new NullPointerException("Bad Merchant Group ID.");
        }

        try {
            merchantID = Long.parseLong(result.get(2));
        }
        catch(NumberFormatException e){
            throw new NullPointerException("Bad Merchant ID.");
        }

        try {
            productID = Long.parseLong(result.get(3));
        }
        catch(NumberFormatException e){
            throw new NullPointerException("Bad Product ID.");
        }

        try {
            modelID = Long.parseLong(result.get(4));
        }
        catch(NumberFormatException e){
            throw new NullPointerException("Bad Model ID.");
        }

        pageID = result.get(5).toLowerCase();

        variant = result.get(6).toLowerCase();

        locale = result.get(7).toLowerCase();

        name = result.get(8).toLowerCase();

        productURL = result.get(9)+result.get(10).toLowerCase();

        imageURL = result.get(11)+result.get(12).toLowerCase();

        description = result.get(13);

        brand = result.get(17).toLowerCase();

        subBrand = result.get(18).length()>0?  result.get(18).toLowerCase(): null;

        if (result.get(21).endsWith(":")){
            catPath = result.get(21).toLowerCase().substring(0,result.get(21).length()-1).split(":");
        }else {
            catPath = result.get(21).toLowerCase().split(":");
        }

        inStock = !result.get(28).equals("f");

        try {
            date = dateFMT.parse(result.get(25)).getTime();
        } catch (ParseException e) {
            throw new NullPointerException(e.getMessage()+"<"+result.get(25)+">");
        }

        discontinued = !result.get(30).equals("f");
    }

}
