package SimpleTiler;

import java.util.ArrayList;
import java.util.HashSet;

public class DoubleString {
    public String first, second;
    public boolean isTile;

    public DoubleString(String first, String second, boolean isTile) {
        this.first = first;
        this.second = second;
        this.isTile = isTile;
    }

    public DoubleString(String first, boolean isTile) {
        this.first = first;
        this.second = null;
        this.isTile = isTile;
    }

    public static ArrayList<DoubleString> transformStringArrayList(ArrayList<String> list, boolean isTile) {
        ArrayList<DoubleString> transform = new ArrayList<>();

        if(list.size()>=1) {
            for(int i=0; i<list.size(); i++) {
                transform.add(new DoubleString(list.get(i), isTile));
            }
        }

        return transform;
    }
}
