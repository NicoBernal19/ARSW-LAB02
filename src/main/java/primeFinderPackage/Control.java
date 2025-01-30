package primeFinderPackage;

/**
 * @author Juan Pablo Daza Pereira
 * @author Nicolas Bernal Fuquene
 */
public class Control extends Thread {

    private final static int NTHREADS = 3;
    private final static int MAXVALUE = 30000000;
    private final static int TMILISECONDS = 5000;
    private final int NDATA = MAXVALUE / NTHREADS;
    private PrimeFinderThread pft[];
    private boolean paused;

    private Control() {
        super();
        this.pft = new PrimeFinderThread[NTHREADS];
        this.paused = false;
        int i;
        for(i = 0;i < NTHREADS - 1; i++) {
            PrimeFinderThread elem = new PrimeFinderThread(i*NDATA, (i+1)*NDATA, this);
            pft[i] = elem;
        }
        pft[i] = new PrimeFinderThread(i*NDATA, MAXVALUE + 1, this);
    }

    public static Control newControl() {
        return new Control();
    }

    @Override
    public void run() {
        for(int i = 0;i < NTHREADS;i++) {
            pft[i].start();
        }

    }

    public synchronized void checkPaused() throws InterruptedException {
        while(paused) {
            wait();
        }
    }
}