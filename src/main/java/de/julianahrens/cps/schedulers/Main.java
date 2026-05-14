package de.julianahrens.cps.schedulers;

import de.julianahrens.cps.schedulers.util.DuckDBLogger;

public class Main {

    public static void main(String[] args) {
        System.out.println("MLLF");
        Scheduler mllfScheduler = new MLLFInterrruptableScheduler();
        mllfScheduler.addToScheduler(1, 0, 6, 3, 1);
        mllfScheduler.addToScheduler(2, 0, 7, 4, 1);
        System.out.println((int) mllfScheduler.getNextTaskId(0, -1).get(0));
        mllfScheduler.addToScheduler(1, 0, 6, 2, 1);
        System.out.println(mllfScheduler.getNextTaskId(0, 1).get(0));
        mllfScheduler.addToScheduler(1, 0, 6, 1, 1);
        System.out.println(mllfScheduler.getNextTaskId(0, 1).get(0));
        System.out.println(mllfScheduler.getNextTaskId(0, 1).get(0));

        System.out.println("LLF");
        Scheduler llfInterruptableScheduler = new LLFInterruptableScheduler();
        llfInterruptableScheduler.addToScheduler(1, 0, 6, 3, 1);
        llfInterruptableScheduler.addToScheduler(2, 0, 7, 4, 1);
        System.out.println(llfInterruptableScheduler.getNextTaskId(0, -1).get(0));
        llfInterruptableScheduler.addToScheduler(1, 0, 6, 2, 1);
        System.out.println(llfInterruptableScheduler.getNextTaskId(0, 1).get(0));
        llfInterruptableScheduler.addToScheduler(2, 0, 7, 3, 1);
        System.out.println(llfInterruptableScheduler.getNextTaskId(0, 1).get(0));
        System.out.println(llfInterruptableScheduler.getNextTaskId(0, 1).get(0));

        System.out.println("FCFS");
        Scheduler fcfsNonInterruptableScheduler = new FCFSNonInterruptableScheduler();
        fcfsNonInterruptableScheduler.addToScheduler(1, 0, 6, 3, 1);
        fcfsNonInterruptableScheduler.addToScheduler(2, 0, 7, 4, 1);
        System.out.println(fcfsNonInterruptableScheduler.getNextTaskId(0, -1).get(0));
        fcfsNonInterruptableScheduler.addToScheduler(1, 0, 6, 2, 1);
        System.out.println(fcfsNonInterruptableScheduler.getNextTaskId(0, 1).get(0));
        fcfsNonInterruptableScheduler.addToScheduler(2, 0, 7, 3, 1);
        System.out.println(fcfsNonInterruptableScheduler.getNextTaskId(0, 1).get(0));
        System.out.println(fcfsNonInterruptableScheduler.getNextTaskId(0, 1).get(0));

        DuckDBLogger logger = new DuckDBLogger();
        logger.log(1, 10.0, 200.0, 300, "entree", "LLF", 0, "simple");
    }
}
