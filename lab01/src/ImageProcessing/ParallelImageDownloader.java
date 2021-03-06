package ImageProcessing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelImageDownloader {

    private static void downloadImage(String website, String filename, boolean filterImage, double sigma, int kernelRadius, int nFilters) throws IOException {
        URL imageURL = new URL(website);
        if(filterImage) {
            BufferedImage image = ImageIO.read(imageURL);
            for(int i=0; i<nFilters; ++i) {
                image = gaussianFilter(image, sigma, kernelRadius);
                System.out.println("Filter " + (i+1) + " of " + nFilters);
            }
            ImageIO.write(image, "png", new File("download/" + filename));
        }

        else
            ImageIO.write(ImageIO.read(imageURL), "png", new File("download/" + filename));

        System.out.println("Image saved");
    }

    public static void downloadImagesFromSite(String website, boolean filterImage, double sigma, int kernelRadius, int nFilters) throws IOException, InterruptedException {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executors = Executors.newFixedThreadPool(cores);

        Document site = Jsoup.connect(website).get();
        Elements elements = site.select("[href$=.png]");

        for(var element : elements) {
            executors.submit(() -> {
                try {
                    downloadImage(element.attr("abs:href"), element.text(), filterImage, sigma, kernelRadius, nFilters);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executors.shutdown();
        executors.awaitTermination(1, TimeUnit.DAYS);
    }

    private static double[][] buildGaussianKernel(double sigma, int radius) {
        double[][] kernel = new double[2*radius + 1][2*radius + 1];
        double sum = 0;
        for(int x=0; x<2*radius + 1; ++x) {
            for(int y=0; y<2*radius + 1; ++y) {
                kernel[x][y] = Math.exp(- (Math.pow(x-radius, 2) + Math.pow(y-radius, 2)) / (2 * sigma * sigma));
                sum += kernel[x][y];
            }
        }

        for(int x=0; x<2*radius + 1; ++x)
            for(int y=0; y<2*radius + 1; ++y)
                kernel[x][y] /= sum;

        return kernel;
    }

    private static BufferedImage gaussianFilter(BufferedImage imageToProcess, double sigma, int kernelRadius) {
        BufferedImage processedImage = new BufferedImage(imageToProcess.getWidth(), imageToProcess.getHeight(), imageToProcess.getType());
        double[][] filter = buildGaussianKernel(sigma, kernelRadius);
        float[] tempColors = {0, 0, 0};

        for(int x=0; x<imageToProcess.getWidth(); ++x) {
            for(int y=0; y<imageToProcess.getHeight(); ++y) {
                // If it is border then do nothing
                if(x-kernelRadius < 0 || x+kernelRadius >= imageToProcess.getWidth() || y-kernelRadius < 0 || y+kernelRadius >= imageToProcess.getHeight())
                    processedImage.setRGB(x, y, imageToProcess.getRGB(x, y));
                else {
                    float[] newColors = {0, 0, 0};
                    for(int xx=x-kernelRadius; xx<=x+kernelRadius; ++xx) {
                        for(int yy=y-kernelRadius; yy<=y+kernelRadius; ++yy) {
                            for(int c=0; c<3; ++c) {
                                new Color(imageToProcess.getRGB(xx, yy)).getRGBColorComponents(tempColors);
                                newColors[c] += tempColors[c] * filter[xx-x+kernelRadius][yy-y+kernelRadius];
                            }
                        }
                    }
                    processedImage.setRGB(x, y, new Color(newColors[0], newColors[1], newColors[2]).getRGB());
                }
            }
        }

        return processedImage;
    }

    public static long getExecutionTime(int iterations, String website, boolean filterImage, double sigma, int kernelRadius, int nFilters, TimeUnit unit) throws IOException, InterruptedException {
        long[] execTime = new long[iterations];
        for(int i=0; i<iterations; ++i) {
            long start = System.nanoTime();
            downloadImagesFromSite(website, filterImage, sigma, kernelRadius, nFilters);
            long end = System.nanoTime();

            execTime[i] = end - start;
        }

        double avgTime = 0;
        for(var i : execTime)
            avgTime += i;
        avgTime /= iterations;

        return unit.convert((long)avgTime, TimeUnit.NANOSECONDS);
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        long start = System.nanoTime();
        downloadImagesFromSite("http://www.if.pw.edu.pl/~mrow/dyd/wdprir/", true, 5, 5, 3);
        long end = System.nanoTime();

        System.out.println("Execution time: " + (end-start));
    }
}
