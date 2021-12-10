package Sort;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class MultithreadedSort {

    private static double[] mergeSort(double[] arr) {
        double[] retArr = arr.clone();
        helperMergeSort(retArr);

        return retArr;
    }

    private static void helperMergeSort(double[] arr) {
        if(arr.length < 2)
            return;

        int mid = arr.length / 2;
        double[] left = new double[mid];
        double[] right = new double[arr.length - mid];

        for(int i=0; i<mid; ++i)
            left[i] = arr[i];
        for(int i=mid; i<arr.length; ++i)
            right[i-mid] = arr[i];

        helperMergeSort(left);
        helperMergeSort(right);
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

    public static double[] parallelMergeSort(double[] arr) {
        double[] retArr = arr.clone();
        new ForkJoinPool().invoke(new RecursiveAction() {
            @Override
            protected void compute() {
                helperParallelMergeSort(retArr, 0,retArr.length-1);
            }
        });

        return  retArr;
    }

    private static void helperParallelMergeSort(double[] arr, int low, int high) {
        if (low < high) {
            int mid = (low+high)/2;

            RecursiveAction lower = new RecursiveAction() {
                @Override
                protected void compute() {
                    helperParallelMergeSort(arr,low,mid);
                }
            };
            RecursiveAction higher = new RecursiveAction() {
                @Override
                protected void compute() {
                    helperParallelMergeSort(arr,mid+1,high);
                }
            };

            RecursiveAction.invokeAll(lower, higher);
            parallelMerge(arr, low, mid, high);
        }
    }

    private static void parallelMerge(double arr[],int l, int m, int r) {
        // Find sizes of two subarrays to be merged
        int n1 = m - l + 1;
        int n2 = r - m;

        /* Create temp arrays */
        double[] L = new double[n1];
        double[] R = new double[n2];

        /*Copy data to temp arrays*/
        for (int i=0; i<n1; ++i)
            L[i] = arr[l + i];
        for (int j=0; j<n2; ++j)
            R[j] = arr[m + 1+ j];


        /* Merge the temp arrays */

        // Initial indexes of first and second subarrays
        int i = 0, j = 0;

        // Initial index of merged subarry array
        int k = l;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            }
            k++;
        }

        /* Copy remaining elements of L[] if any */
        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }

        /* Copy remaining elements of R[] if any */
        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }

    public static long measureTime(double[] arr, Function<double[], double[]> func) {
        long start = System.nanoTime();
        func.apply(arr);
        long end = System.nanoTime();

        System.out.println("Execution time: " + (end-start) + "ns");

        return end-start;
    }

    public static void main(String[] args) throws InterruptedException {
        // Strony z których kra..inspirowałem się:
        //      https://en.wikipedia.org/wiki/Merge_sort#Merge_sort_with_parallel_merging
        //      https://en.wikipedia.org/wiki/Merge_algorithm#Parallel_merge
        //      https://coderanch.com/t/696561/java/Faster-Merge-Sort-Fork-Join

        for(int N=20; N<=20e6+1; N*=10) {
            double[] arr = new double[N];
            for(int i=0; i<N; ++i)
                arr[i] = new Random().nextDouble();

            System.out.println("Rozmiar tablicy: " + N);
            var serialTime = measureTime(arr, new Function<double[], double[]>() {
                @Override
                public double[] apply(double[] doubles) {
                    return mergeSort(doubles);
                }
            });

            var parallelTime = measureTime(arr, new Function<double[], double[]>() {
                @Override
                public double[] apply(double[] doubles) {
                    return parallelMergeSort(doubles);
                }
            });

            System.out.println("Czas sekwencyjny: " + serialTime);
            System.out.println("Czas równoległy: " + parallelTime);
            System.out.println("Różnica czasów: " + Math.abs(parallelTime - serialTime));
            System.out.println("Iloczyn czasów (sekwencyjny/równoległy): " + ((float) serialTime / parallelTime));
            System.out.println("===================================================");

        }

        int N = 20000000;
        double[] arr = new double[N];
        for(int i=0; i<N; ++i)
            arr[i] = new Random().nextDouble();

//        System.out.println("Sekwencyjny MergeSort");
//        System.out.println(Arrays.toString(arr));
//        System.out.println(Arrays.toString(mergeSort(arr)));
//
//        System.out.println("====================================");
//
//        System.out.println("Równoległy MergeSort");
//        System.out.println(Arrays.toString(arr));
//        System.out.println(Arrays.toString(parallelMergeSort(arr)));

        boolean arraysEqual = true;
        double[] serial = mergeSort(arr);
        double[] parallel = parallelMergeSort(arr);
        for(int i=0; i<N; ++i)
            if(serial[i] != parallel[i]) {
                arraysEqual = false;
                break;
            }
        if(arraysEqual)
            System.out.println("Tablice są takie same!");
        else
            System.out.println("Tablice są różne!");

    }
}
