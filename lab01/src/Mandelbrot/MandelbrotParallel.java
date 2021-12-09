package Mandelbrot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class MandelbrotParallel {

    public static class MandelbrotWorker implements Runnable {
        int width, height, heightMin, heightMax, N;
        double xmin, xmax, ymin, ymax;
        BufferedImage image;

        public MandelbrotWorker(int width, int height, int heightMin, int heightMax, double xmin, double xmax, double ymin, double ymax, int N, BufferedImage image) {
            this.width = width;
            this.height = height;
            this.heightMin = heightMin;
            this.heightMax = heightMax;
            this.xmin = xmin;
            this.xmax = xmax;
            this.ymin = ymin;
            this.ymax = ymax;
            this.N = N;
            this.image = image;
        }

        @Override
        public void run() {

            for(int y=heightMin; y<heightMax; ++y) {
                for(int x=0; x<width; ++x) {
                    double xval = xmin + x*((xmax - xmin) / (width - 1));
                    double yval = ymin + y*((ymax - ymin) / (height - 1));
                    if(checkPoint(N, xval, yval))
                        image.setRGB(x, y, Color.BLACK.getRGB());
                    else
                        image.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }

        public boolean checkPoint(int iterations, double xval, double yval) {
            double x = 0, y = 0;

            for(int i=0; i<iterations; ++i) {
                double oldX = x, oldY = y;
                x = oldX*oldX - oldY*oldY + xval;
                y = 2*oldX*oldY + yval;

                if(Math.sqrt(x*x + y*y) >= 2)
                    return false;
            }

            return true;
        }
    }


    public BufferedImage generateMandelbrot(int width, int height, double xmin, double xmax, double ymin, double ymax, int N) {
        int cores = Runtime.getRuntime().availableProcessors();
        Thread[] threadWorkers = new Thread[cores];
        MandelbrotWorker[] mandelbrotWorkers = new MandelbrotWorker[cores];

        byte[] bw = {(byte) 0xff, (byte) 0};
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1, 2, bw, bw, bw));

        for(int i=0; i<cores; ++i) {
            int heightMin = i * height / cores;
            int heightMax = (i + 1) * height / cores;
            mandelbrotWorkers[i] = new MandelbrotWorker(width, height, heightMin, heightMax, xmin, xmax, ymin, ymax, N, image);
            threadWorkers[i] = new Thread(mandelbrotWorkers[i]);
            threadWorkers[i].start();
        }

        for(var worker : threadWorkers) {
            try {
                worker.join();
//                System.out.println("Worker finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return image;
    }

    public BufferedImage generateMandelbrot(int width, int height) {
        return generateMandelbrot(width, height, -2.1, 0.6, -1.2, 1.2, 200);
    }

    public long getExecutionTime(int iterations, int width, int height, TimeUnit unit) {
        long[] execTime = new long[iterations];
        for(int i=0; i<iterations; ++i) {
            long start = System.nanoTime();
            generateMandelbrot(width, height);
            long end = System.nanoTime();

            execTime[i] = end - start;
        }

        double avgTime = 0;
        for(var i : execTime)
            avgTime += i;
        avgTime /= iterations;

        return unit.convert((long)avgTime, TimeUnit.NANOSECONDS);
    }

    public long getExecutionTime(int iterations, int width, int height) {
        return getExecutionTime(iterations, width, height, TimeUnit.NANOSECONDS);
    }

    public void saveImage(BufferedImage image) throws IOException {
        File outputFile = new File("kleks_workers_" + new SimpleDateFormat("dd_MM_yyyy_HH_mm").format(new java.util.Date()) + ".png");
        ImageIO.write(image, "png", outputFile);
    }

    public static void main(String[] args) throws IOException {
//        int size = 20000;
        MandelbrotParallel mandelbrot = new MandelbrotParallel();
//        System.out.println("Czas wykoniania " + mandelbrot.getExecutionTime(1, size, size) + "ns");
//        mandelbrot.saveImage(mandelbrot.generateMandelbrot(size, size));

        PrintWriter outputFile = new PrintWriter("Mandelbrot_parallel.csv");
        for(int size = 32; size <= 8192; size *= 2) {
            System.out.println("Size " + size + " started...");
            outputFile.println(size + ", " + mandelbrot.getExecutionTime(20, size, size, TimeUnit.MICROSECONDS));
            outputFile.flush();
            System.out.println("Size " + size + " finished...");
        }
        outputFile.close();
    }

}
