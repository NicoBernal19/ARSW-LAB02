package primeFinderPackage;
/**
 *
 * @author Juan Pablo Daza Pereira
 * @author Nicolas Bernal Fuquene
 */

import java.util.LinkedList;
import java.util.List;

public class PrimeFinderThread extends Thread{

    int a,b;
    private List<Integer> primes;
    private Control control;

    public PrimeFinderThread(int a, int b, Control control) {
        super();
        this.primes = new LinkedList<>();
        this.a = a;
        this.b = b;
        this.control = control;
    }

    @Override
    public void run(){
        for (int i= a;i < b;i++){
            try {
                control.checkPaused();
                if (isPrime(i)){
                    primes.add(i);
                    System.out.println(i);
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    boolean isPrime(int n) {
        boolean ans;
        if (n > 2) {
            ans = n%2 != 0;
            for(int i = 3;ans && i*i <= n; i+=2 ) {
                ans = n % i != 0;
            }
        } else {
            ans = n == 2;
        }
        return ans;
    }

    public List<Integer> getPrimes() {
        return primes;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void setPrimes(List<Integer> primes) {
        this.primes = primes;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }
}
