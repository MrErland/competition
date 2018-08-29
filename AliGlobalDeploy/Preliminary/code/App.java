import java.util.ArrayList;

/**
 * @author Erland
 *      APP类
 */

public class App {

    public int id;
    public ArrayList<Double> cpuHold;       // 高精度
    public ArrayList<Double> memHold;
    public double diskHold;
    public int P, M, PM;

    App(int id, ArrayList<Double> cpu, ArrayList<Double> mem, double disk, int p, int m, int pm){
        this.id = id;
        this.cpuHold = cpu;
        this.memHold = mem;
        this.diskHold = disk;
        this.P = p;
        this.M = m;
        this.PM = pm;
    }
}
