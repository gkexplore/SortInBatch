/**
 * Created by Karthik on 1/9/2017.
 */
public class DVConstants {
    public static int RAMSIZE = 1800000;
    public static final int BATCHSIZE = (RAMSIZE * 9)/20;
    public static final int BUFFERSIZE = 512;
    public static final int KEY_BATCHSIZE = 250;
    public static final String RECDELIM = "|";
    public static final String RECDELIM_SPLIT = "\\"+RECDELIM;

}
