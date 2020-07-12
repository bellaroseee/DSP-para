package para;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class FilterEmpty {
    static ForkJoinPool POOL = new ForkJoinPool();
    static final int CUTOFF = 1;

    public static int[] filterEmpty(String[] arr) {
        int[] bitset = mapToBitSet(arr);
        int[] bitsum = ParallelPrefixSum.parallelPrefixSum(bitset);
        int[] result = mapToOutput(arr, bitsum);
        return result;
    }

    public static int[] mapToBitSet(String[] arr) {
        return POOL.invoke(new MapToBitSetTask(arr, 0, arr.length, new int[arr.length]));
    }

   /* public static int[] combine(int[] right, int[] left) {
        int[] ret = new int[right.length + left.length];
        for (int i = 0; i < right.length; i++) {
            ret[i] = right[i];
        }
        for (int i = right.length; i < left.length; i++) {
            ret[i] = left[i];
        }
        return ret;
    }*/

    public static class MapToBitSetTask extends RecursiveTask<int[]> {
        String[] arr; //input array
        int hi, lo; //diving down into the 'smallest' task
        int[] bitset;

        public MapToBitSetTask(String[] arr, int lo, int hi, int[] bitset) {
            this.arr = arr;
            this.lo = lo;
            this.hi = hi;
            this.bitset = bitset;
        }

        @Override
        protected int[] compute() {
            if (hi - lo <= CUTOFF) {
                if (arr[lo].length() == 0) {
                    bitset[lo] = 0;
                } else {
                    bitset[lo] = 1;
                }
                return bitset;
            }

            int mid = lo + (hi - lo) / 2;

            MapToBitSetTask left = new MapToBitSetTask(arr, lo, mid, bitset);
            MapToBitSetTask right = new MapToBitSetTask(arr, mid, hi, bitset);

            left.fork();
            right.compute();
            left.join();
            return bitset;
        }
    }

    public static int[] mapToOutput(String[] input, int[] bitsum) {
        return POOL.invoke(new MapToOutputTask(input, bitsum, 0, input.length, new int[helper(bitsum)]));
    }

    //finding how many distinct values are in bitsum
    private static int helper(int[] bitsum) {
        int ret = bitsum[0];
        for (int i = 0; i < bitsum.length - 1; i++) {
            if (bitsum[i + 1] != bitsum[i]) {
                ret++;
            }
        }
        return ret;
    }

    public static class MapToOutputTask extends RecursiveTask<int[]> {
        String[] input;
        int[] bitsum;
        int[] output;
        int lo, hi;

        public MapToOutputTask(String[] input, int[] bitsum, int lo, int hi, int[] output) {
            this.input = input;
            this.bitsum = bitsum;
            this.lo = lo;
            this.hi = hi;
            this.output = output;
        }

        @Override
        protected int[] compute() {
            //for each element in the input, store in result at location sepcified by prefix sum - 1,
            //only if the element meet the condition of bit vector
            if (hi - lo <= CUTOFF) {
                if (input[lo].length() != 0) {
                    output[bitsum[lo] - 1] = input[lo].length();
                }
                return output;
            }

            int mid = lo + (hi - lo) / 2;

            MapToOutputTask left = new MapToOutputTask(input, bitsum, 0, mid, output);
            MapToOutputTask right = new MapToOutputTask(input, bitsum, mid, hi, output);

            left.fork();
            right.compute();
            left.join();

            return output;
        }
    }

    private static void usage() {
        System.err.println("USAGE: FilterEmpty <String array>");
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            usage();
        }
        String[] arr = args[0].replaceAll("\\s*", "").split(",");
        System.out.println(Arrays.toString(filterEmpty(arr)));
    }
}
