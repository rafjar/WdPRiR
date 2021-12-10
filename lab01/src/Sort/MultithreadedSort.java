package Sort;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class MultithreadedSort {

    private static void mergeSort(double[] arr) {
        if(arr.length < 2)
            return;

        int mid = arr.length / 2;
        double[] left = new double[mid];
        double[] right = new double[arr.length - mid];

        for(int i=0; i<mid; ++i)
            left[i] = arr[i];
        for(int i=mid; i<arr.length; ++i)
            right[i-mid] = arr[i];

        mergeSort(left);
        mergeSort(right);
        merge(arr, left, right);
    }

    private static void merge(double[] arr, double[] left, double[] right) {
        int i=0, j=0, k=0;

        while(i<left.length && j<right.length) {
            if(left[i] <= right[j])
                arr[k++] = left[i++];
            else
                arr[k++] = right[j++];
        }

        while(i < left.length)
            arr[k++] = left[i++];

        while(j < right.length)
            arr[k++] = right[j++];
    }

    public static void parallelMergeSort(double[] arr) throws InterruptedException {
        ForkJoinPool pool = new ForkJoinPool();
        helperParallelMergeSort(arr, pool);
    }

    private static void helperParallelMergeSort(double[] arr, ForkJoinPool pool) {

        if(arr.length < 2)
            return;

        int mid = arr.length / 2;
        double[] left = new double[mid];
        double[] right = new double[arr.length - mid];

        for(int i=0; i<mid; ++i)
            left[i] = arr[i];
        for(int i=mid; i<arr.length; ++i)
            right[i-mid] = arr[i];

        pool.submit(()->helperParallelMergeSort(left, pool));
        pool.submit(()->helperParallelMergeSort(right, pool));
        merge(arr, left, right);
    }

    public static void main(String[] args) throws InterruptedException {
        int N = 20;
        double[] arr = new double[N];
        for(int i=0; i<N/2; ++i)
            arr[i] = i;
        for(int i=N; i>N/2; --i)
            arr[i-1] = -i+17;

        System.out.println("Sekwencyjny MergeSort");
        System.out.println(Arrays.toString(arr));
        mergeSort(arr);
        System.out.println(Arrays.toString(arr));

        System.out.println("====================================");
        N = 20;
        for(int i=0; i<N/2; ++i)
            arr[i] = i;
        for(int i=N; i>N/2; --i)
            arr[i-1] = -i+17;

        System.out.println("Równoległy MergeSort");
        System.out.println(Arrays.toString(arr));
        parallelMergeSort(arr);
        System.out.println(Arrays.toString(arr));
    }
}
