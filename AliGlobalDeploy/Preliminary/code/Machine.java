
/**
 * @author Erland
 *      物理机类
 */

public class Machine{

    public int id;     // id号，为了方便，设置成int
    public int cpu, mem, disk;
    public int PMax, MMax, PMMax;

    Machine(int id, int cpu, int mem, int disk, int p, int m, int pm){
        this.id = id;
        this.cpu = cpu;
        this.mem = mem;
        this.disk = disk;
        this.PMax = p;
        this.MMax = m;
        this.PMMax = pm;
    }

}


