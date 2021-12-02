package lab1;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class MandelbrotParallel {

    public class Parallel implements Runnable {
        int width, height, N;
        double xmin, xmax, ymin, ymax;
        BufferedImage image;

        public Parallel(int width, int height, double xmin, double xmax, double ymin, double ymax, int N) {
            this.width = width;
            this.height = height;
            this.xmin = xmin;
            this.xmax = xmax;
            this.ymin = ymin;
            this.ymax = ymax;
            this.N = N;
        }

        @Override
        public void run() {
            byte[] bw = {(byte) 0xff, (byte) 0};
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1, 2, bw, bw, bw));

            for(int y=0; y<height; ++y) {
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

        public BufferedImage getImage() {
            return image;
        }
    }

    public static boolean checkPoint(int iterations, double xval, double yval) {
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

    public BufferedImage generateMandelbrot(int width, int height, double xmin, double xmax, double ymin, double ymax, int N) {
        int cores = Runtime.getRuntime().availableProcessors();
        Thread[] workers = new Thread[cores];
        Parallel[] parallels = new Parallel[cores];

        byte[] bw = {(byte) 0xff, (byte) 0};
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1, 2, bw, bw, bw));

        double ySize = (ymax-ymin) / cores;
        for(int i=0; i<cores; ++i) {
            parallels[i] = new Parallel(width, height/4, xmin, xmax, ymin+i*ySize, ymin+(i+1)*ySize, N);
            workers[i] = new Thread(parallels[i]);
            workers[i].start();
        }

        for(var worker : workers) {
            try {
                worker.join();
                System.out.println("Worker finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Graphics g = image.getGraphics();
        for(int i=0; i<cores; ++i) {
            g.drawImage(parallels[i].getImage(), 0, i*(height / 4), null);
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

    public static void saveImage(BufferedImage image) throws IOException {
        File outputFile = new File("kleks_" + new SimpleDateFormat("dd_MM_yyyy_HH_mm").format(new java.util.Date()) + ".png");
        ImageIO.write(image, "png", outputFile);
    }

    public static void main(String[] args) throws IOException {
        int size = 8000;
        MandelbrotParallel mandelbrot = new MandelbrotParallel();
//        mandelbrot.saveImage(mandelbrot.generateMandelbrot(size, size));
        System.out.println("Czas wykoniania " + mandelbrot.getExecutionTime(1, size, size) + "ns");
//        mandelbrot.generateMandelbrot(size, size);

//        PrintWriter outputFile = new PrintWriter("pomiary_" + new SimpleDateFormat("dd_MM_yyyy_HH_mm").format(new java.util.Date()) + ".csv");
//        for(int size = 32; size <= 8192; size *= 2) {
//            System.out.println("Size " + size + " started...");
//            outputFile.println(size + ", " + getExecutionTime(200, size, size, TimeUnit.MICROSECONDS));
//            outputFile.flush();
//            System.out.println("Size " + size + " finished...");
//        }
//        outputFile.close();
    }

}
