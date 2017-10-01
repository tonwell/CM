package droid.crowdmap;

import android.os.Environment;
import android.os.StatFs;

import java.text.DecimalFormat;

/**
 * Created by ton on 07/12/15.
 */
public class Util {
    private static String floatForm(double data){
        return new DecimalFormat("#.##").format(data);
    }

    public static long getFreeInternalMemory(){
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        return ( statFs.getAvailableBlocks() * (long) statFs.getBlockSize());
    }

    public static String bytesToHuman (long fM){

        long KB = 1024;
        long MB = KB * 1024;
        long GB = MB * 1024;
        if(fM >= 0 && fM<KB){
            return floatForm(fM) + " bytes";
        } else if (fM < MB) {
            return floatForm((double) fM/KB) + " KB";
        } else if (fM < GB) {
            return floatForm((double) fM/MB) + " MB";
        } else if (fM >= GB) {
            return floatForm((double) fM/GB) + "GB";
        } else {
            return "-1";
        }
    }
}
