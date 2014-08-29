package uk.ac.standrews.cs.digitising_scotland.population_model.tools;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import uk.ac.standrews.cs.digitising_scotland.population_model.organic.OrganicPopulation;



/**
 * The Class MemoryMonitor uses {@link MemoryMXBean} to log system memory usage.
 * Memory usage is then written out to a file called "memoryUsage_" + a unique time.
 */
public class MemoryMonitor implements Runnable {

    /** The mxbean. */
    private MemoryMXBean mxbean;

    /** The date format. */
    private DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd HH_mm");

    /** The cal. */
    private Calendar cal = Calendar.getInstance();

    /** The running. */
    private boolean running = true;

    /** The time. */
    private int time = 0;
    
    private PrintWriter writer;

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        while (running) {

            if (mxbean == null) {
                mxbean = ManagementFactory.getMemoryMXBean();
            }

            while (true) {
                time++;
                writer.println(time + "\t" + mxbean.getHeapMemoryUsage().getCommitted() + "\t" + mxbean.getHeapMemoryUsage().getUsed());
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    
    public void log(int currentday, int population, int totalPersonListSize) {
        if (mxbean == null) {
            mxbean = ManagementFactory.getMemoryMXBean();
        }
        writer.println((1600 + Math.round(currentday/OrganicPopulation.getDaysPerYear())) + "\t" + population + "\t" + totalPersonListSize + "\t" + mxbean.getHeapMemoryUsage().getUsed() / 1024);
        writer.flush();
        
    }
    
    public void close() {
        writer.close();
    }

    /**
     * Instantiates a new memory monitor.
     */
    public MemoryMonitor() {

        try {
            writer = new PrintWriter("src/main/resources/output/memory_usage" + System.nanoTime() + ".dat", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.err.println("Memory usage file not held");
        }
        //MemoryMXBean

        mxbean = ManagementFactory.getMemoryMXBean();
//        System.out.println(dateFormat.format(cal.getTime()));
    }

    /**
     * Stop.
     */
    public void stop() {

        running = false;
    }
    


}
