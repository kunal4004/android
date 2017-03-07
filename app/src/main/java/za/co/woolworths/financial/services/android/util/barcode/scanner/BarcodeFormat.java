package za.co.woolworths.financial.services.android.util.barcode.scanner;

import net.sourceforge.zbar.Symbol;

import java.util.ArrayList;
import java.util.List;

public class BarcodeFormat {
    private int mId;
    private String mName;

    public static final BarcodeFormat EAN8 = new BarcodeFormat(Symbol.EAN8, "EAN8");
    public static final BarcodeFormat UPCE = new BarcodeFormat(Symbol.UPCE, "UPCE");
    public static final BarcodeFormat UPCA = new BarcodeFormat(Symbol.UPCA, "UPCA");
    public static final BarcodeFormat EAN13 = new BarcodeFormat(Symbol.EAN13, "EAN13");
    public static final BarcodeFormat ISBN13 = new BarcodeFormat(Symbol.ISBN13, "ISBN13");
    public static final BarcodeFormat CODE128 = new BarcodeFormat(Symbol.CODE128, "CODE128");
    public static final BarcodeFormat NONE = new BarcodeFormat(Symbol.NONE, "NONE");
    public static final List<BarcodeFormat> ALL_FORMATS = new ArrayList<>();

    static {
        ALL_FORMATS.add(BarcodeFormat.EAN8);
        ALL_FORMATS.add(BarcodeFormat.UPCE);
        ALL_FORMATS.add(BarcodeFormat.UPCA);
        ALL_FORMATS.add(BarcodeFormat.EAN13);
        ALL_FORMATS.add(BarcodeFormat.ISBN13);
        ALL_FORMATS.add(BarcodeFormat.CODE128);
    }

    private BarcodeFormat(int id, String name) {
        mId = id;
        mName = name;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public static BarcodeFormat getFormatById(int id) {
        for (BarcodeFormat format : ALL_FORMATS) {
            if (format.getId() == id) {
                return format;
            }
        }
        return BarcodeFormat.NONE;
    }
}