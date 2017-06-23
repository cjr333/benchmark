import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MergeTestOrg {
  @Builder
  static class Data {
    @Getter private int key;
    @Getter private int val1;
    @Getter @Setter private int val2;
  }

  private final int ITERATE = 100;
  private final int LENGTH = 1000;

  boolean debug = false;

  @Test
  public void test() {
    Random rn = new Random(System.nanoTime());
    long start, end;
    long[] latency = {0, 0, 0, 0, 0, 0, 0, 0};

    for (int loop = 0; loop < ITERATE; loop++) {

      // region Sorted Data

      // Prepare data
      List<Data> list1 = new ArrayList<>(LENGTH);
      List<Data> list2 = new ArrayList<>(LENGTH);
      for (int i = 0; i < LENGTH; i++) {
        list1.add(Data.builder().key(i).val1(i).build());
        list2.add(Data.builder().key(i).val2(i).build());
      }

      // solution 1 - simple nested loop
      start = System.nanoTime();
      for (int i = 0; i < LENGTH; i++) {
        for (int j = 0; j < LENGTH; j++) {
          if (list1.get(i).getKey() == list2.get(j).getKey()) {
            list1.get(i).setVal2(list2.get(j).getVal2());
            break;
          }
        }
      }
      end = System.nanoTime();
      latency[0] += end - start;

      validateSolution(list1);

      // Prepare data
      list1 = new ArrayList<>(LENGTH);
      for (int i = 0; i < LENGTH; i++) {
        list1.add(Data.builder().key(i).val1(i).build());
      }

      // solution 2 - stream
      start = System.nanoTime();
      //    list1.parallelStream().forEach(data -> data.setVal2(list2.stream().filter(data2 -> data.getKey() == data2.getKey()).findFirst().get().getVal2()));
      list1 = list1.stream()
          .map(data -> {
            data.setVal2(list2.stream().filter(data2 -> data.getKey() == data2.getKey()).findFirst().get().getVal2());
            return data;
          }).collect(Collectors.toList());
      end = System.nanoTime();
      latency[1] += end - start;

      validateSolution(list1);

      // solution 3 - parallel stream
      start = System.nanoTime();
      //    list1.parallelStream().forEach(data -> data.setVal2(list2.stream().filter(data2 -> data.getKey() == data2.getKey()).findFirst().get().getVal2()));
      list1 = list1.parallelStream()
          .map(data -> {
            data.setVal2(list2.stream().filter(data2 -> data.getKey() == data2.getKey()).findFirst().get().getVal2());
            return data;
          }).collect(Collectors.toList());
      end = System.nanoTime();
      latency[2] += end - start;

      validateSolution(list1);

      // Prepare data
      list1 = new ArrayList<>(LENGTH);
      for (int i = 0; i < LENGTH; i++) {
        list1.add(Data.builder().key(i).val1(i).build());
      }

      // solution 4 - nested loop by sort key
      start = System.nanoTime();
      int pointer = 0;
      for (int i = 0; i < LENGTH; i++) {
        for (int j = pointer; j < LENGTH; j++) {
          if (list1.get(i).getKey() == list2.get(j).getKey()) {
            list1.get(i).setVal2(list2.get(j).getVal2());
            pointer = j + 1;
            break;
          }
        }
      }
      end = System.nanoTime();
      latency[3] += end - start;

      validateSolution(list1);

      // endregion

      // region Unsorted Data

      List<Data> mixed1 = new ArrayList<>(LENGTH);
      List<Data> mixed2 = new ArrayList<>(LENGTH);
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

      // Prepare data
      list1 = new ArrayList<>(LENGTH);
      list1.addAll(mixed1);
      final List<Data> list3 = new ArrayList<>(LENGTH);
      list3.addAll(mixed2);

      // solution 5 - simple nested loop
      start = System.nanoTime();
      for (int i = 0; i < LENGTH; i++) {
        for (int j = 0; j < LENGTH; j++) {
          if (list1.get(i).getKey() == list3.get(j).getKey()) {
            list1.get(i).setVal2(list3.get(j).getVal2());
            break;
          }
        }
      }
      end = System.nanoTime();
      latency[4] += end - start;

      validateSolution(list1);

      // Prepare data
      list1 = new ArrayList<>(LENGTH);
      list1.addAll(mixed1);

      // solution 6 - stream
      start = System.nanoTime();
      list1 = list1.stream()
          .map(data -> {
            data.setVal2(list3.stream().filter(data2 -> data.getKey() == data2.getKey()).findFirst().get().getVal2());
            return data;
          }).collect(Collectors.toList());
      end = System.nanoTime();
      latency[5] += end - start;

      validateSolution(list1);

      // Prepare data
      list1 = new ArrayList<>(LENGTH);
      list1.addAll(mixed1);

      // solution 7 - parallel stream
      start = System.nanoTime();
      list1 = list1.parallelStream()
          .map(data -> {
            data.setVal2(list3.stream().filter(data2 -> data.getKey() == data2.getKey()).findFirst().get().getVal2());
            return data;
          }).collect(Collectors.toList());
      end = System.nanoTime();
      latency[6] += end - start;

      validateSolution(list1);

      // Prepare data
      list1 = new ArrayList<>(LENGTH);
      list1.addAll(mixed1);

      // solution 8 - nested loop by sort key
      start = System.nanoTime();
      list1 = list1.stream().sorted(Comparator.comparing(Data::getKey)).collect(Collectors.toList());
      List<Data> list4 = list3.stream().sorted(Comparator.comparing(Data::getKey)).collect(Collectors.toList());
      pointer = 0;
      for (int i = 0; i < LENGTH; i++) {
        for (int j = pointer; j < LENGTH; j++) {
          if (list1.get(i).getKey() == list4.get(j).getKey()) {
            list1.get(i).setVal2(list4.get(j).getVal2());
            pointer = j + 1;
            break;
          }
        }
      }
      end = System.nanoTime();
      latency[7] += end - start;

      validateSolution(list1);

      // endregion
    }

    for(int i = 0; i < 8; i++) {
      System.out.println(String.format("Solution %d : %f ms", i + 1, (latency[i] / ITERATE) / 1000000.0));
    }
  }

  private void validateSolution(List<Data> list) {
    Random rn = new Random(System.nanoTime());
    if (debug) {
      for (int i = 0; i < 10; i++) {
        int sample = rn.nextInt(LENGTH);
        Assert.isTrue(list.get(sample).getKey() == list.get(sample).getVal1());
        Assert.isTrue(list.get(sample).getKey() == list.get(sample).getVal2());
      }
    }
  }
}
