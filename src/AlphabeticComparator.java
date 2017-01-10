import java.util.Comparator;

/**
 * Created by Karthik on 1/9/2017.
 */
public class AlphabeticComparator implements Comparator {
    private int keys[] = null;
    private int keys2[] = null;
    private String delim = DVConstants.RECDELIM_SPLIT;

    public AlphabeticComparator(int keys[], String delim){
        this.keys = keys;
        this.delim = delim;
    }

    public AlphabeticComparator(int keys[], int keys2[], String delim){
        this(keys, delim);
        this.keys2 = keys2;
    }

    public int[] getSrcKeys(){
        return this.keys;
    }

    public int[] getTrgKeys(){
        return this.keys2;
    }

    public int compare(Object firstObjToCompare, Object secondObjToCompare){
        String firstKeyArray[] = firstObjToCompare.toString().split(delim, -1);
        String secondKeyArray[] = secondObjToCompare.toString().split(delim, -1);
        int retVal = 0;
        int i= 0;
        try{
            for(;i<keys.length;i++){
                retVal = firstKeyArray[keys[i]].trim().compareTo(secondKeyArray[(keys2!=null?keys2[i]:keys[i])].trim());
                if(retVal==0)
                    continue;
                else
                    return retVal;
            }
        }catch(Exception ex){
            System.out.println("Exception:"+ex.getMessage());
        }
        return 0;
    }

    protected void finalize() throws Throwable{
        try{
            this.keys = null;
        }finally {
            super.finalize();
        }
    }
}
