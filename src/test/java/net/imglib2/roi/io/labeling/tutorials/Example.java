package net.imglib2.roi.io.labeling.tutorials;

import java.util.Objects;

class Example implements Comparable<Example> {

    String a;

    double b;

    int c;

    public Example() {
    }

    public Example(String a, double b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Example example = (Example) o;
        return Double.compare(example.b, b) == 0 &&
                c == example.c &&
                Objects.equals(a, example.a);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c);
    }

    @Override
    public int compareTo(Example o) {
        return this.equals(o) ? 0 : 1;
    }
}
