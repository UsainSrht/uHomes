package me.usainsrht.uhomes;

public class IntArray {

    private int min;
    private int max;

    public IntArray(String string) {
        if (string.contains("-")) {
            String[] strings = string.split("-");
            this.min = Integer.parseInt(strings[0]);
            this.max = Integer.parseInt(strings[1]);
        } else {
            this.min = Integer.parseInt(string);
            this.max = Integer.parseInt(string);
        }
    }

    public IntArray(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean isInBetween(int i) {
        return i >= min && i <= max;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public int next() {
        java.util.Random random = new java.util.Random();
        return random.nextInt(getMax() + 1 - getMin()) + getMin();
    }

}
