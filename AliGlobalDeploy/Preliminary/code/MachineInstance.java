
import java.util.ArrayList;

/**
 *  机器内有实例存在的机器类
 */

public class MachineInstance {

    public int nowP, nowM, nowPM;               // 当前的P, M, PM
    public ArrayList<Integer> instances;       // 内部所有的实例
    public ArrayList<Double> nowCpu;           // cpu占用
    public ArrayList<Double> nowMem;
    public double nowDisk;

    private Info info;
    public Machine machine;
    public double fitness;          // 评价值

    MachineInstance(Info info, int machineid){     // 初始化函数
        this.info = info;
        machine = info.indexMach.get(machineid);
        instances = new ArrayList<>();
        nowCpu = new ArrayList<>();
        nowMem = new ArrayList<>();
        nowDisk = 0;
        nowP = nowM = nowPM = 0;
        fitness = 0.0;
    }

    MachineInstance(MachineInstance mi){
        this.info = mi.info;
        this.machine = mi.machine;

        this.fitness = mi.fitness;
        this.nowP = mi.nowP;
        this.nowM = mi.nowM;
        this.nowPM = mi.nowPM;
        this.nowDisk = mi.nowDisk;
        this.instances = new ArrayList<>(mi.instances);
        int n = mi.nowCpu.size();
        this.nowCpu = new ArrayList<>(n);
        this.nowMem = new ArrayList<>(n);
        for(int i = 0 ; i < n; i++){
            this.nowMem.add(mi.nowMem.get(i));
            this.nowCpu.add(mi.nowCpu.get(i));
        }
    }

    public boolean addInstance(Instance instance){     // 如果插入失败，则放弃插入，返回false
        App app = info.indexApp.get(instance.appId);
        int tmpP = nowP;
        int tmpM = nowM;
        int tmpPM = nowPM;
        double tmpDisk = nowDisk;

        if(tmpP + app.P <= machine.PMax)
            tmpP += app.P;
        else
            return false;

        if(tmpM + app.M <= machine.MMax)
            tmpM += app.M;
        else
            return false;

        if(tmpPM + app.PM <= machine.PMMax)
            tmpPM += app.PM;
        else
            return false;

        if(tmpDisk + app.diskHold <= machine.disk)
            tmpDisk += app.diskHold;
        else
            return false;

        int n = info.indexApp.get(instance.appId).cpuHold.size();
        ArrayList<Double> tmpCpu = new ArrayList<>(nowCpu);
        ArrayList<Double> tmpMem = new ArrayList<>(nowMem);
        if(tmpCpu.isEmpty()){
            for(int i = 0; i < n; i++){
                tmpCpu.add(app.cpuHold.get(i));
                tmpMem.add(app.memHold.get(i));
                if(tmpCpu.get(i) > machine.cpu || tmpMem.get(i) > machine.mem)
                    return false;
            }
        }else {
            for (int i = 0; i < n; i++) {
                tmpCpu.set(i, nowCpu.get(i) + app.cpuHold.get(i));
                tmpMem.set(i, nowMem.get(i) + app.memHold.get(i));
                if(tmpCpu.get(i) > machine.cpu || tmpMem.get(i) > machine.mem)
                    return false;
            }
        }

        // 检查一遍app_interference约束
        if(!instances.isEmpty()) {
            n = info.constraints.size();
            for (int i = 0; i < n; i++) {
                int app1 = info.constraints.get(i).appId1;
                int app2 = info.constraints.get(i).appId2;
                int count = info.constraints.get(i).count;
                if(app2 != instance.appId && app1 != instance.appId) continue;
                int a = 0, b = 0;
                for (int inst : instances){
                    if(app1 == info.indexInst.get(inst).appId) a++;
                    if(app2 == info.indexInst.get(inst).appId) b++;
                }
                if(app1 != app2) {
                    if (a > 0 && b >= count && app2 == instance.appId) return false;
                    if (a == 0 && b > count && app1 == instance.appId) return false;
                }else{
                    if ( a >= count + 1) return false;
                }
            }
        }

        instances.add(instance.id);
        nowP = tmpP;
        nowM = tmpM;
        nowPM = tmpPM;
        nowDisk = tmpDisk;
        nowCpu = tmpCpu;
        nowMem = tmpMem;
        return true;
    }

    public boolean isAllow(){
        if(instances.isEmpty()) return true;
        if(nowP > machine.PMax || nowM > machine.MMax || nowPM > machine.PMMax) return false;
        if(nowDisk > machine.disk) return false;
        for(int i = 0; i < nowCpu.size(); i++){
            if(nowCpu.get(i) > machine.cpu || nowMem.get(i) > machine.mem)
                return false;
        }

        int n = info.constraints.size();
        for (int i = 0; i < n; i++) {
            int app1 = info.constraints.get(i).appId1;
            int app2 = info.constraints.get(i).appId2;
            int count = info.constraints.get(i).count;
            int a = 0, b = 0;
            for (int inst : instances) {
                if (app1 == info.indexInst.get(inst).appId) a++;
                if (app2 == info.indexInst.get(inst).appId) b++;
            }
            if (app1 != app2) {
                if (a > 0 && b > count){
                    return false;
                }
            }else {
                if (a > count + 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean removeInstance(Instance instance){   // 从machine中移除某个实例
        App app = info.indexApp.get(instance.appId);
        if(instances.isEmpty()) return false;
        int index = instances.indexOf(instance.id);
        if(index == -1) return false;
        instances.remove(index);

        nowP -= app.P;
        nowM -= app.M;
        nowPM -= app.PM;
        nowDisk -= app.diskHold;

        int n = app.cpuHold.size();
        for(int i = 0; i < n; i++){
            nowCpu.set(i, nowCpu.get(i) - app.cpuHold.get(i));
            nowMem.set(i, nowMem.get(i) - app.memHold.get(i));
        }
        return true;
    }

    public void getFitness(){
        fitness = 0.0;
        if(instances.size() == 0) return;
        int n = info.apps.get(0).cpuHold.size();
        for(int i = 0; i < n; i++) {
            double c = nowCpu.get(i) / machine.cpu;
            fitness += (1.0 + 10.0 * (Math.exp(Math.max(0.0, c - 0.5)) - 1));
        }
        fitness /= n;
    }

    public boolean isMoreCost(){
        double level = (double) machine.cpu / 2.0;
        for(double d : nowCpu){
            if(d > level) return true;
        }
        return false;
    }
}
