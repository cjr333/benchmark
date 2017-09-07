package memory;

import objectexplorer.MemoryMeasurer;
import org.junit.Test;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class MeasureUsage {
  @Test
  public void test() {
    long memory = MemoryMeasurer.measureBytes(Collections.singletonList(1));
    long memory2 = MemoryMeasurer.measureBytes(Arrays.asList(1));
    ExtendAbstarctList abstractList = new ExtendAbstarctList();
    ExtendAbstarctSet abstractSet = new ExtendAbstarctSet();
    long memory3 = MemoryMeasurer.measureBytes(abstractList);
    long memory4 = MemoryMeasurer.measureBytes(abstractSet);
    System.out.println(memory + " vs " + memory2 + " vs " + memory3 + " vs " + memory4);
  }

  static class ExtendAbstarctList<E> extends AbstractList<E> {
    private int a = 0;
    @Override
    public E get(int index) {
      return null;
    }

    @Override
    public int size() {
      return 0;
    }
  };

  static class ExtendAbstarctSet<E> extends AbstractSet<E> {
      private int a = 0;
      protected transient int modCount = 0;

    private class ltr implements Iterator<E> {

      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public E next() {
        return null;
      }
    }

    @Override
    public Iterator<E> iterator() {
      return null;
    }

    @Override
    public int size() {
      return 0;
    }
  }

  @Test
  public void test2() {
    Integer t1 = 1;
    Integer[] t2 = new Integer[1];
    t2[0] = new Integer(1);
    //    t2[1] = new Integer(2);
    //    t2[2] = new Integer(3);
    //    t2[3] = new Integer(3);
    Long[] t3 = new Long[2];
    t3[0] = new Long(1L);
    t3[1] = new Long(2L);
    long memory = MemoryMeasurer.measureBytes(t1);
    long memory2 = MemoryMeasurer.measureBytes(t2);
    long memory3 = MemoryMeasurer.measureBytes(t3);
    System.out.println(memory + " vs " + memory2 + " vs " + memory3);
  }

  @Test
  public void test3() {
    A a = new A();
    B b = new B();
    C c = new C();
    D d = new D();
    long memory = MemoryMeasurer.measureBytes(a);
    long memory2 = MemoryMeasurer.measureBytes(b);
    long memory3 = MemoryMeasurer.measureBytes(c);
    long memory4 = MemoryMeasurer.measureBytes(d);
    System.out.println(memory + " vs " + memory2 + " vs " + memory3 + " vs " + memory4);
  }

  public static class A {
    private Long[] a;
  }

  public static class B {
    private int[] b = new int[1];
    public B() {
      b[0] = 1;
    }
  }

  public static class C {
    private Integer[] c = new Integer[1];
    public C() {
      c[0] = new Integer(1);
    }
  }

  public static class D {
    private byte a = 1;
    private byte b = 1;
    private byte c = 1;
    private byte d = 1;
    private byte e = 1;
  }

  @Test
  public void test4() {
    int[] t1 = new int[4];
    t1[0] = 1;
    t1[1] = 1;
    t1[2] = 1;
    t1[3] = 1;
    Integer[] t2 = new Integer[4];
    t2[0] = 1;
    t2[1] = 2;
    t2[2] = 3;
    t2[3] = 4;
    Integer[] t3 = new Integer[4];
    t3[0] = new Integer(1);
    t3[1] = new Integer(1);
    t3[2] = new Integer(1);
    t3[3] = new Integer(1);
    long memory = MemoryMeasurer.measureBytes(t1);
    long memory2 = MemoryMeasurer.measureBytes(t2);
    long memory3 = MemoryMeasurer.measureBytes(t3);
    System.out.println(memory + " vs " + memory2 + " vs " + memory3);
  }
}

