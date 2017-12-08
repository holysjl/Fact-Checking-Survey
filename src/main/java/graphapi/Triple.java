package graphapi;

/**
 This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
 It is licensed under the Creative Commons Attribution License
 (see http://creativecommons.org/licenses/by/3.0) by
 the YAGO-NAGA team (see http://mpii.de/yago-naga).





 This class provides the simple datatype of a pair */
public class Triple<F,S,T> implements Comparable<Triple<F,S,T>> {
    public F first;
    public S second;
    public T third;

    public F first() {
        return first;
    }
    public void setFirst(F first) {
        this.first=first;
    }
    public S second() {
        return second;
    }
    public void setSecond(S second) {
        this.second=second;
    }
    public T third() {
        return third;
    }
    public void setThird(T third) {
        this.third=third;
    }

    /** Constructs a Pair*/
    public Triple(F first, S second, T third) {
        super();
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /** Constructs an empty pair */
    public Triple(){
        super();
    }

    public int hashCode() {
        return(first.hashCode()^second.hashCode()^third.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Triple && ((Triple<?,?,?>)obj).first.equals(first) && ((Triple<?,?,?>)obj).second.equals(second) && ((Triple<?,?,?>)obj).third.equals(third);
    }
    /** Returns "first/second"*/
    public String toString() {
        return first+"/"+second+"/"+third;
    }

    @SuppressWarnings("unchecked")
    public int compareTo(Triple<F, S, T> o) {
        int firstCompared=((Comparable<F>)first).compareTo(o.first());
        if(firstCompared!=0) return(firstCompared);
        int secondCompared=((Comparable<S>)second).compareTo(o.second());
        if(secondCompared!=0) return(secondCompared);
        return(((Comparable<T>)third).compareTo(o.third()));
    }
}