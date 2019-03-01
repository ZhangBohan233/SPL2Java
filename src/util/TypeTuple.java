package util;

class TypeTuple {

    private int t1, t2;

    TypeTuple(final int t1, final int t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TypeTuple && t1 == ((TypeTuple) obj).t1 && t2 == ((TypeTuple) obj).t2;
    }

    @Override
    public int hashCode() {
        return ((t1 & 0xff) << 8) | t2 & 0xff;
    }
}
