package para;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class LongestSequence {
    public static class SequenceRange {
        public int matchingOnLeft, matchingOnRight; //number of matching values on left / right
        public int longestRange, sequenceLength;

        public SequenceRange(int left, int right, int longest, int length) {
            this.matchingOnLeft = left;
            this.matchingOnRight = right;
            this.longestRange = longest;
            this.sequenceLength = length;
        }
    }

    public static SequenceRange sequential(int val, int[] arr, int lo, int hi) {
        int longestRange = 0;
        int count = 0;

        for (int i = lo; i < hi; i++) {
            if (arr[i] == val) {
                count++;
            } else {
                if (count > longestRange) {
                    longestRange = count;
                }
                count = 0;
            }
        }
        int leftMatch = 0;
        for (int j = lo; j < hi; j++) {
            if (arr[j] == val) {
                leftMatch++;
            } else {
                break;
            }
        }
        int rightMatch = 0;
        for (int j = hi - 1; j >= lo; j--) {
            if (arr[j] == val) {
                rightMatch++;
            } else {
                break;
            }
        }
        return new SequenceRange(leftMatch, rightMatch, longestRange, hi - lo);
    }

    public static SequenceRange combine(SequenceRange right, SequenceRange left) {
        int ml = left.matchingOnLeft;
        int mr = right.matchingOnRight;
        int longestRange = 0;
        int sequenceLength = right.sequenceLength + left.sequenceLength;
        if (right.matchingOnRight == right.sequenceLength) { //[1, 0, 0, 0][1, 1, 1, 1]
            mr = right.matchingOnRight + left.matchingOnRight;
        }
        if (left.matchingOnLeft == left.sequenceLength) { //[1, 1, 1, 1][0, 0, 0, 0]
            ml = right.matchingOnLeft + left.matchingOnLeft;
        }
        //[1, 0, 0, 1][1, 1, 0, 0] or [1, 0, 0, 0][1, 1, 0, 0]
        int temp = right.matchingOnLeft + left.matchingOnRight;
        longestRange = Math.max(temp, right.longestRange);
        longestRange = Math.max(longestRange, left.longestRange);
        return new SequenceRange(ml, mr, longestRange, sequenceLength);
    }

    static final ForkJoinPool POOL = new ForkJoinPool();
    static int CUTOFF;

    public static int getLongestSequence(int val, int[] arr, int sequentialCutoff) {
        CUTOFF = sequentialCutoff;
        return POOL.invoke(new LongestSequenceTask(val, arr, 0, arr.length)).longestRange;
    }

    public static class LongestSequenceTask extends RecursiveTask<SequenceRange> {
        int[] arr;
        int val;
        int lo;
        int hi;

        public LongestSequenceTask(int val, int[] arr, int lo, int hi) {
            this.arr = arr;
            this.val = val;
            this.lo = lo;
            this.hi = hi;
        }

        @Override
        protected SequenceRange compute() {
            if (hi - lo <= CUTOFF) {
                return sequential(val, arr, lo, hi);
            }

            int mid = lo + (hi - lo) / 2;

            LongestSequenceTask left = new LongestSequenceTask(val, arr, lo, mid);
            LongestSequenceTask right = new LongestSequenceTask(val, arr, mid, hi);

            left.fork();

            return combine(right.compute(), left.join());
        }

    }

    private static void usage() {
        System.err.println("USAGE: LongestSequence <number> <array> <sequential cutoff>");
        System.exit(2);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            usage();
        }
        int val = 0;
        int[] arr = null;
        try {
            val = Integer.parseInt(args[0]);
            String[] stringArr = args[1].replaceAll("\\s*",  "").split(",");
            arr = new int[stringArr.length];
            for (int i = 0; i < stringArr.length; i++) {
                arr[i] = Integer.parseInt(stringArr[i]);
            }
            System.out.println(getLongestSequence(val, arr, Integer.parseInt(args[2])));
        } catch (NumberFormatException e) {
            usage();
        }
    }
}
