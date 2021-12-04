package lab1;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.*;

public class MandelbrotPool {

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

//    private void work(int width, int widthMin, int widthMax, int height, int heightMin, int heightMax, double xmin, double xmax, double ymin, double ymax, int N, BufferedImage image) {
//        for(int y=heightMin; y<heightMax; ++y) {
//            for(int x=widthMin; x<widthMax; ++x) {
//                double xval = xmin + x*((xmax - xmin) / (width - 1));
//                double yval = ymin + y*((ymax - ymin) / (height - 1));
//                if(checkPoint(N, xval, yval))
//                    image.setRGB(x, y, Color.BLACK.getRGB());
//                else
//                    image.setRGB(x, y, Color.WHITE.getRGB());
//            }
//        }
//
//    }


    public BufferedImage generateMandelbrot(int width, int widthChunkSize, int height, int heightChunkSize, double xmin, double xmax, double ymin, double ymax, int N) {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorPool = Executors.newFixedThreadPool(cores);

        byte[] bw = {(byte) 0xff, (byte) 0};
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1, 2, bw, bw, bw));

        for(int y=0; y<height; y += heightChunkSize) {
            for(int x=0; x<width; x += widthChunkSize) {
                int finalY = y;
                int finalX = x;
                executorPool.submit(() -> {
                    for(int yy = finalY; yy<Integer.min(finalY+heightChunkSize, height); ++yy) {
                        for(int xx = finalX; xx<Integer.min(finalX+widthChunkSize, width); ++xx) {
                            double xval = xmin + xx*((xmax - xmin) / (width - 1));
                            double yval = ymin + yy*((ymax - ymin) / (height - 1));
                            if(checkPoint(N, xval, yval))
                                image.setRGB(xx, yy, Color.BLACK.getRGB());
                            else
                                image.setRGB(xx, yy, Color.WHITE.getRGB());
                        }
                    }
                });
            }
        }

        executorPool.shutdown();
        try {
            executorPool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return image;
    }

    public BufferedImage generateMandelbrot(int width, int widthChunkSize, int height, int heightChunkSize) {
        return generateMandelbrot(width, widthChunkSize, height, heightChunkSize, -2.1, 0.6, -1.2, 1.2, 200);
    }

    public long getExecutionTime(int iterations, int width, int widthChunkSize, int height, int heightChunkSize, TimeUnit unit) {
        long[] execTime = new long[iterations];
        for(int i=0; i<iterations; ++i) {
            long start = System.nanoTime();
            generateMandelbrot(width, widthChunkSize, height, heightChunkSize);
            long end = System.nanoTime();

            execTime[i] = end - start;
        }

        double avgTime = 0;
        for(var i : execTime)
            avgTime += i;
        avgTime /= iterations;

        return unit.convert((long)avgTime, TimeUnit.NANOSECONDS);
    }

    public long getExecutionTime(int iterations, int width, int widthChunkSize, int height, int heightChunkSize) {
        return getExecutionTime(iterations, width, widthChunkSize, height, heightChunkSize, TimeUnit.NANOSECONDS);
    }

    public void saveImage(BufferedImage image) throws IOException {
        File outputFile = new File("kleks_workers_" + new SimpleDateFormat("dd_MM_yyyy_HH_mm").format(new java.util.Date()) + ".png");
        ImageIO.write(image, "png", outputFile);
    }

    public static void main(String[] args) throws IOException {
        int size = 8000;
        int chunkSize = 100;
        MandelbrotPool mandelbrot = new MandelbrotPool();
        System.out.println("Czas wykoniania " + mandelbrot.getExecutionTime(1, size, chunkSize, size, chunkSize) + "ns");
        mandelbrot.saveImage(mandelbrot.generateMandelbrot(size, chunkSize, size, chunkSize));

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
