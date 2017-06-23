package benchmark;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@State(Scope.Thread)
public class MergeTest {
  private final int LENGTH = 1000;
  volatile List<Data> ordered1;
  volatile List<Data> ordered2;
  List<Data> mixed1;
  List<Data> mixed2;

  public static void main(String[] args) {
    MergeTest benchmark = new MergeTest();
    benchmark.setup();

    System.out.println("ordered1 last item value is: " + benchmark.simpleNestedLoopWithOrdered());
    System.out.println("ordered1 last item value is: " + benchmark.sequentialStreamWithOrdered());
    System.out.println("ordered1 last item value is: " + benchmark.parallelStreamWithOrdered());
    System.out.println("ordered1 last item value is: " + benchmark.nestedLoopBySortedKeyWithOrdered());
    System.out.println("mixed1 last item value is: " + benchmark.simpleNestedLoopWithMixed());
    System.out.println("mixed1 last item value is: " + benchmark.sequentialStreamWithMixed());
    System.out.println("mixed1 last item value is: " + benchmark.parallelStreamWithMixed());
    System.out.println("mixed1 last item value is: " + benchmark.nestedLoopBySortedKeyWithMixed());
  }

  @Builder
  static class Data {
    @Getter private int key;
    @Getter private int val1;
    @Getter @Setter private int val2;
  }

  @Setup
  public void setup() {
    ordered1 = new ArrayList<>(LENGTH);
    ordered2 = new ArrayList<>(LENGTH);
    for (int i = 0; i < LENGTH; i++) {
      ordered1.add(Data.builder().key(i).val1(i).build());
      ordered2.add(Data.builder().key(i).val2(i).build());
    }

    Random rn = new Random(System.nanoTime());
    mixed1 = new ArrayList<>(LENGTH);
    mixed2 = new ArrayList<>(LENGTH);

    for (int i = 0; i < LENGTH; i++) {
      mixed1.add(Data.builder().key(i).val1(i).build());
      mixed2.add(Data.builder().key(i).val2(i).build());
    }

    for (int i = LENGTH - 1; i > 0; i--) {
      int index = rn.nextInt(LENGTH);
      Data temp = mixed1.get(index);
      mixed1.set(index, mixed1.get(i));
      mixed1.set(i, temp);
    }
    for (int i = LENGTH - 1; i > 0; i--) {
      int index = rn.nextInt(LENGTH);
      Data temp = mixed2.get(index);
      mixed2.set(index, mixed2.get(i));
      mixed2.set(i, temp);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(2)
  @Measurement(iterations = 5)
  @Warmup(iterations = 5)
  public int simpleNestedLoopWithOrdered() {
    for (int i = 0; i < LENGTH; i++) {
      for (int j = 0; j < LENGTH; j++) {
        if (ordered1.get(i).getKey() == ordered2.get(j).getKey()) {
          ordered1.get(i).setVal2(ordered2.get(j).getVal2());
          break;
        }
      }
    }
    return ordered1.get(LENGTH - 1).getVal2();
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(2)
  @Measurement(iterations = 5)
  @Warmup(iterations = 5)
  public int sequentialStreamWithOrdered() {
    ordered1 = ordered1.stream()
        .map(data -> {
          data.setVal2(ordered2.stream().filter(data2 -> data.getKey() == data2.getKey()).findFirst().get().getVal2());
          return data;
        }).collect(Collectors.toList());
    return ordered1.get(LENGTH - 1).getVal2();
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(2)
  @Measurement(iterations = 5)
  @Warmup(iterations = 5)
  public int parallelStreamWithOrdered() {
    ordered1 = ordered1.parallelStream()
        .map(data -> {
          data.setVal2(ordered2.stream().filter(data2 -> data.getKey() == data2.getKey()).findFirst().get().getVal2());
          return data;
        }).collect(Collectors.toList());
    return ordered1.get(LENGTH - 1).getVal2();
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(2)
  @Measurement(iterations = 5)
  @Warmup(iterations = 5)
  public int nestedLoopBySortedKeyWithOrdered() {
    int pointer = 0;
    for (int i = 0; i < LENGTH; i++) {
      for (int j = pointer; j < LENGTH; j++) {
        if (ordered1.get(i).getKey() == ordered2.get(j).getKey()) {
          ordered1.get(i).setVal2(ordered2.get(j).getVal2());
          pointer = j + 1;
          break;
        }
      }
    }
    return ordered1.get(LENGTH - 1).getVal2();
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(2)
  @Measurement(iterations = 5)
  @Warmup(iterations = 5)
  public int simpleNestedLoopWithMixed() {
    for (int i = 0; i < LENGTH; i++) {
      for (int j = 0; j < LENGTH; j++) {
        if (mixed1.get(i).getKey() == mixed2.get(j).getKey()) {
          mixed1.get(i).setVal2(mixed2.get(j).getVal2());
          break;
        }
      }
    }
    return mixed1.get(LENGTH - 1).getVal2();
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(2)
  @Measurement(iterations = 5)
  @Warmup(iterations = 5)
  public int sequentialStreamWithMixed() {
    mixed1 = mixed1.stream()
        .map(data -> {
          data.setVal2(mixed2.stream().filter(data2 -> data.getKey() == data2.getKey()).findFirst().get().getVal2());
          return data;
        }).collect(Collectors.toList());
    return mixed1.get(LENGTH - 1).getVal2();
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(2)
  @Measurement(iterations = 5)
  @Warmup(iterations = 5)
  public int parallelStreamWithMixed() {
    mixed1 = mixed1.parallelStream()
        .map(data -> {
          data.setVal2(mixed2.stream().filter(data2 -> data.getKey() == data2.getKey()).findFirst().get().getVal2());
          return data;
        }).collect(Collectors.toList());
    return mixed1.get(LENGTH - 1).getVal2();
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(2)
  @Measurement(iterations = 5)
  @Warmup(iterations = 5)
  public int nestedLoopBySortedKeyWithMixed() {
    mixed1 = mixed1.stream().sorted(Comparator.comparing(Data::getKey)).collect(Collectors.toList());
    mixed2 = mixed2.stream().sorted(Comparator.comparing(Data::getKey)).collect(Collectors.toList());
    int pointer = 0;
    for (int i = 0; i < LENGTH; i++) {
      for (int j = pointer; j < LENGTH; j++) {
        if (mixed1.get(i).getKey() == mixed2.get(j).getKey()) {
          mixed1.get(i).setVal2(mixed2.get(j).getVal2());
          pointer = j + 1;
          break;
        }
      }
    }
    return mixed1.get(LENGTH - 1).getVal2();
  }
}
