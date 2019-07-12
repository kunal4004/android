package za.co.woolworths.financial.services.android.util.barcode;

import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Common class for code scanner runtime exceptions
 */
public class CodeScannerException extends RuntimeException {
    public CodeScannerException() {
        super();
    }

    public CodeScannerException(@Nullable String message) {
        super(message);
    }

    public CodeScannerException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public CodeScannerException(@Nullable Throwable cause) {
        super(cause);
    }

    @RequiresApi(Build.VERSION_CODES.N)
    protected CodeScannerException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression,
								   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
