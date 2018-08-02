package za.co.woolworths.financial.services.android.util.barcode;

final class Rect {
    private final int mLeft;
    private final int mTop;
    private final int mRight;
    private final int mBottom;

    public Rect(int left, int top, int right, int bottom) {
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;
    }

    public int getLeft() {
        return mLeft;
    }

    public int getTop() {
        return mTop;
    }

    public int getRight() {
        return mRight;
    }

    public int getBottom() {
        return mBottom;
    }

    public int getWidth() {
        return mRight - mLeft;
    }

    public int getHeight() {
        return mBottom - mTop;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * (31 * mLeft + mTop) + mRight) + mBottom;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Rect) {
            Rect other = (Rect) obj;
            return mLeft == other.mLeft && mTop == other.mTop && mRight == other.mRight && mBottom == other.mBottom;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[(" + mLeft + "; " + mTop + ") - (" + mRight + "; " + mBottom + ")]";
    }
}
