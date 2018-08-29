import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * @author Erland
 *              模拟退火法--寻优
 */

public class SA {

    private Solution nowSolution;   // 当前解
    private Info info;
    private Random random;
    private double tempInit;
    private double tempEnd;
    private double speed;
    private int loop;

    SA(Info info, Solution solution){
        this.info = info;
        this.nowSolution = solution;
        random = new Random();
        random.setSeed(1);

        tempInit = 200;
        tempEnd = 0.000001;
        speed = 0.99;
        loop = 100;
    }

    public void run() {
        double T, de;
        long cnt = 0;
        nowSolution.getScore();
        Solution tmpSolution = new Solution(nowSolution);
        int n = getMachine(nowSolution);
        System.out.println("count:" + cnt++ + " score:" + nowSolution.socre + " machine:" + n);
        T = tempInit;
        while (T > tempEnd) {
//            double rate = Math.pow((T - tempEnd) / (tempInit - tempEnd), 2);
            for (int i = 0; i < loop; i++) {
                if(i % 10 == 0)splitMachine(nowSolution, 1);
                mergerMachine(nowSolution, 1);
                moveInstance(nowSolution, 100);    // 迁移
                exchangeInstance(500);       // 交换

                de = nowSolution.socre - tmpSolution.socre;
                if (de < 0) {         // 更优解
                    tmpSolution = new Solution(nowSolution);
                }else if(T > 1) {        // 差解
                    nowSolution = new Solution(tmpSolution);
                }else {
                    if (Math.random() < Math.exp(-de / T)) {
                        tmpSolution = new Solution(nowSolution);   // 接受
                    } else {
                        nowSolution = new Solution(tmpSolution);    // 放弃
                    }
                }
            }
            T *= speed;
            n = getMachine(nowSolution);
            System.out.println("count:" + cnt++ + " score:" + nowSolution.socre + " machine:" + n);
        }
        info.submitMachineInst(nowSolution);        // 打印最终机器状态
//        info.submits = nowSolution.submits;
    }

    private int getMachine(Solution solution){
        int n = 0;
        for(MachineInstance mi : solution.machineInstances){
            if(mi.instances.size() > 0) n++;
        }
        return n;
    }

    private void reduceOneMachine(Solution nowSolution, int times){
        int randMachineA;
        MachineInstance mi;
        Comparator<Integer> comparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int app1 = info.indexInst.get(o1).appId;
                int app2 = info.indexInst.get(o2).appId;
                double disk1 = info.indexApp.get(app1).diskHold;
                double disk2 = info.indexApp.get(app2).diskHold;
                if(disk1 != disk2) {
                    if(disk1 > disk2) return 1;
                    else return -1;
                }else{
                    return 0;
                }
            }
        };

        for(int i = 0; i < times; i++){
            int br = 0;
            while (true){
                randMachineA = random.nextInt(nowSolution.machineInstances.size());
                mi = nowSolution.machineInstances.get(randMachineA);
                if(mi.fitness > 1.0 && mi.machine.cpu == 32) break;
                br++;
                if(br > 3000) return;
            }
            Collections.sort(mi.instances, comparator);
            for(int j = mi.instances.size() - 1; j >= 0; j--){
                int inst = mi.instances.get(j);
                boolean b = true;
                for(int t = 0; t < 3000; t++){
                    int k = random.nextInt(nowSolution.machineInstances.size());
                    MachineInstance mb = nowSolution.machineInstances.get(k);
                    if(mb.fitness < 2.0 && k != randMachineA && mb.addInstance(info.indexInst.get(inst))){
                        mi.removeInstance(info.indexInst.get(inst));
//                        nowSolution.submits.add(new Submit(inst, mb.machine.id));
                        double prea = mi.fitness;
                        double preb = mb.fitness;
                        mi.getFitness();
                        mb.getFitness();
                        double nowa = mi.fitness;
                        double nowb = mb.fitness;
                        nowSolution.socre = nowSolution.socre - prea - preb + nowa + nowb;
                        b = false;
                        break;
                    }
                }
                if(b) break;
            }
        }
    }

    private void reduceMachine(Solution nowSolution, int times){
        int randMachineA, randMachineB;
        MachineInstance mA, mB;
        Comparator<Integer> comparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int app1 = info.indexInst.get(o1).appId;
                int app2 = info.indexInst.get(o2).appId;
                double disk1 = info.indexApp.get(app1).diskHold;
                double disk2 = info.indexApp.get(app2).diskHold;
                if(disk1 != disk2) {
                    if(disk1 > disk2) return 1;
                    else return -1;
                }else{
                    return 0;
                }
            }
        };

        for(int i = 0; i < times; i++){
            // 先找到两个机器，再尽量往外进行迁移，最后尝试合并
            while(true) {
                randMachineA = random.nextInt(nowSolution.machineInstances.size());
                mA = nowSolution.machineInstances.get(randMachineA);
                if(mA.fitness > 1 && mA.machine.disk == 1440) break;
            }
            while(true) {
                randMachineB = random.nextInt(nowSolution.machineInstances.size());
                if(randMachineA == randMachineB) continue;
                mB = nowSolution.machineInstances.get(randMachineB);
                if(mB.fitness > 1) break;
            }
            // sort inst
            Collections.sort(mA.instances, comparator);
            Collections.sort(mB.instances, comparator);

            for(int t = 0; t < 5000; t++){
                int k = random.nextInt(nowSolution.machineInstances.size());
                if(randMachineA == k) continue;
                if(nowSolution.machineInstances.get(k).instances.size() == 0) continue;
                for(int j = mA.instances.size() - 1; j >= 0; j--){
                    int inst = mA.instances.get(j);
                    if(nowSolution.machineInstances.get(k).addInstance(info.indexInst.get(inst))){
                        mA.removeInstance(info.indexInst.get(inst));
 //                       nowSolution.submits.add(new Submit(inst, nowSolution.machineInstances.get(k).machine.id));
                        double prea = mA.fitness;
                        double preb = nowSolution.machineInstances.get(k).fitness;
                        mA.getFitness();
                        nowSolution.machineInstances.get(k).getFitness();
                        double nowa = mA.fitness;
                        double nowb = nowSolution.machineInstances.get(k).fitness;
                        nowSolution.socre = nowSolution.socre - prea - preb + nowa + nowb;
                    }
                }
                if(randMachineB == k) continue;
                for(int j = mB.instances.size() - 1; j >= 0; j--) {
                    int inst = mB.instances.get(j);
                    if (randMachineB != k && nowSolution.machineInstances.get(k).addInstance(info.indexInst.get(inst))) {
                        mB.removeInstance(info.indexInst.get(inst));
//                        nowSolution.submits.add(new Submit(inst, nowSolution.machineInstances.get(k).machine.id));
                        double prea = mB.fitness;
                        double preb = nowSolution.machineInstances.get(k).fitness;
                        mB.getFitness();
                        nowSolution.machineInstances.get(k).getFitness();
                        double nowa = mB.fitness;
                        double nowb = nowSolution.machineInstances.get(k).fitness;
                        nowSolution.socre = nowSolution.socre - prea - preb + nowa + nowb;
                    }
                }
            }

            for(int j = mA.instances.size() - 1; j >= 0; j--){
                int inst = mA.instances.get(j);
                if(mB.addInstance(info.indexInst.get(inst))){
                    mA.removeInstance(info.indexInst.get(inst));
//                    nowSolution.submits.add(new Submit(inst, mB.machine.id));
                    double prea = mA.fitness;
                    double preb = mB.fitness;
                    mA.getFitness();
                    mB.getFitness();
                    double nowa = mA.fitness;
                    double nowb = mB.fitness;
                    nowSolution.socre = nowSolution.socre - prea - preb + nowa + nowb;
                }else{
                    break;
                }
            }
        }
    }

    private void moveInstance(Solution nowSolution, int times) {
        int randMachineA, randMachineB = 0, instA = 0;
        boolean isMove;
        int cnt = 0;
        for(int i = 0; i < times; i++) {
            while (true) {
                randMachineA = random.nextInt(nowSolution.machineInstances.size());     // 迁出机器
                if (nowSolution.machineInstances.get(randMachineA).fitness >= 1.5) {
                    break;
                }
            }
            MachineInstance randMA = nowSolution.machineInstances.get(randMachineA);
            Instance instance = null;
            isMove = false;
            for(int t = 0; t < 5; t++) {
                instA = randMA.instances.get(random.nextInt(randMA.instances.size()));   // 迁出实例
                instance = info.indexInst.get(instA);
                for(int j = 0; j < 3000; j++) {
                    randMachineB = random.nextInt(nowSolution.machineInstances.size());  // 迁入机器
                    if (randMachineA != randMachineB && nowSolution.machineInstances.get(randMachineB).fitness <= 2.0
                            && nowSolution.machineInstances.get(randMachineB).instances.size() > 0
                            && nowSolution.machineInstances.get(randMachineB).addInstance(instance)) {
                        isMove = true;
                        break;
                    }else{
                        isMove = false;
                    }
                }
                if(isMove) break;
            }
            if(!isMove) continue;
            cnt++;
            MachineInstance randMB = nowSolution.machineInstances.get(randMachineB);
            randMA.removeInstance(instance);   // 迁出
//            nowSolution.submits.add(new Submit(instA, randMB.machine.id));

            double prea = randMA.fitness;
            double preb = randMB.fitness;
            randMA.getFitness();
            randMB.getFitness();
            double nowa = randMA.fitness;
            double nowb = randMB.fitness;
            nowSolution.socre = nowSolution.socre - prea - preb + nowa + nowb;
        }
//        System.out.print("Move Instances: " + cnt + "  ");
    }

    private void splitMachine(Solution nowSolution, int times){
        int randMachineA, randMachineB, randMachineC;
        for(int i = 0; i < times; i++){
            while (true) {
                randMachineA = random.nextInt(nowSolution.machineInstances.size());     // 迁出机器
                if (nowSolution.machineInstances.get(randMachineA).fitness >= 2.0) {
                    break;
                }
            }
            while (true) {
                randMachineB = random.nextInt(nowSolution.machineInstances.size());
                if (nowSolution.machineInstances.get(randMachineB).instances.size() == 0) {
                    break;
                }
            }
            while (true) {
                randMachineC = random.nextInt(nowSolution.machineInstances.size());
                if (randMachineB != randMachineC && nowSolution.machineInstances.get(randMachineC).instances.size() == 0) {
                    break;
                }
            }
            MachineInstance miA = nowSolution.machineInstances.get(randMachineA);
            MachineInstance miB = nowSolution.machineInstances.get(randMachineB);
            MachineInstance miC = nowSolution.machineInstances.get(randMachineC);
            for(int j = miA.instances.size() - 1; j >= 0; j--){
                Instance inst = info.indexInst.get(miA.instances.get(j));
                if(j % 2 == 0){
                    if(miB.addInstance(inst)){
                        miA.removeInstance(inst);
                    }else if(miC.addInstance(inst)){
                        miA.removeInstance(inst);
                    }else{
           //             System.out.println("Split error.");
                        break;
                    }
                }else {
                    if(miC.addInstance(inst)){
                        miA.removeInstance(inst);
                    }else if(miB.addInstance(inst)){
                        miA.removeInstance(inst);
                    }else {
          //              System.out.println("Split error.");
                        break;
                    }
                }
            }
            double prea = miA.fitness;
            double preb = miB.fitness;
            double prec = miC.fitness;
            miA.getFitness(); miB.getFitness(); miC.getFitness();
            double nowa = miA.fitness;
            double nowb = miB.fitness;
            double nowc = miC.fitness;
            nowSolution.socre = nowSolution.socre - prea -preb - prec + nowa + nowb + nowc;
        }

    }

    private void mergerMachine(Solution nowSolution, int times){
        int randMachineA, randMachineB;
        for(int i = 0; i < times; i++){
            while (true) {
                randMachineA = random.nextInt(nowSolution.machineInstances.size());
                if (nowSolution.machineInstances.get(randMachineA).fitness == 1.0) {
                    break;
                }
            }
            while (true) {
                randMachineB = random.nextInt(nowSolution.machineInstances.size());
                if (nowSolution.machineInstances.get(randMachineB).fitness == 1.0) {
                    break;
                }
            }
            MachineInstance miA = nowSolution.machineInstances.get(randMachineA);       // 从A合并到B
            MachineInstance miB = nowSolution.machineInstances.get(randMachineB);
            if(miA.machine.disk > miB.machine.disk){
                MachineInstance t = miB;
                miB = miA;
                miA = t;
            }
            for(int j = miA.instances.size() - 1; j >= 0; j--){
                Instance inst = info.indexInst.get(miA.instances.get(j));
                if(miB.addInstance(inst)){
                    miA.removeInstance(inst);
                }else {
                    continue;
                }
            }
            double prea = miA.fitness;
            double preb = miB.fitness;
            miA.getFitness();
            miB.getFitness();
            nowSolution.socre = nowSolution.socre - prea -preb + miA.fitness + miB.fitness;
        }
    }

    private void exchangeInstance(int times) {
        int cnt = 0;
        for (int i = 0; i < times; i++) {
            int machineA, machineB, instA, instB;
            int n = nowSolution.machineInstances.size();
            while (true) {
                machineA = random.nextInt(n);
                if (nowSolution.machineInstances.get(machineA).fitness > 1.0) {
                    break;
                }
            }
            while (true) {
                machineB = random.nextInt(n);
                if (nowSolution.machineInstances.get(machineB).fitness > 1.0 && machineA != machineB) {
                    break;
                }
            }
            MachineInstance ma = nowSolution.machineInstances.get(machineA);
            MachineInstance mb = nowSolution.machineInstances.get(machineB);
            int na = ma.instances.size();
            int nb = mb.instances.size();
            instA = ma.instances.get(random.nextInt(na));
            instB = mb.instances.get(random.nextInt(nb));
            Instance ia = info.indexInst.get(instA);
            Instance ib = info.indexInst.get(instB);
            if(ia.appId == ib.appId) continue;
            boolean success = false;

            if (ma.addInstance(ib)) {
                mb.removeInstance(ib);
                if (mb.addInstance(ia)) {
//                    nowSolution.submits.add(new Submit(instB, ma.machine.id));
//                    nowSolution.submits.add(new Submit(instA, mb.machine.id));
                    ma.removeInstance(ia);

                    double prea = ma.fitness;
                    double preb = mb.fitness;
                    ma.getFitness();
                    mb.getFitness();
                    double nowa = ma.fitness;
                    double nowb = mb.fitness;
                    nowSolution.socre = nowSolution.socre - prea - preb + nowa + nowb;
                    success = true;
                    cnt++;
                } else {
                    ma.removeInstance(info.indexInst.get(instB));
                    mb.addInstance(info.indexInst.get(instB));
                }
            }
            if(!success){
                int machineT;
                while (true){
                    machineT = random.nextInt(n);
                    if(nowSolution.machineInstances.get(machineT).instances.size() == 0)
                        break;
                }
                MachineInstance mt = nowSolution.machineInstances.get(machineT);
                if(mt.addInstance(ia)){
                    ma.removeInstance(ia);
                    if(ma.addInstance(ib)){
                        mb.removeInstance(ib);
                        if(mb.addInstance(ia)){
                            mt.removeInstance(ia);
//                            nowSolution.submits.add(new Submit(instA, mt.machine.id));
//                            nowSolution.submits.add(new Submit(instB, ma.machine.id));
//                            nowSolution.submits.add(new Submit(instA, mb.machine.id));
                            double prea = ma.fitness;
                            double preb = mb.fitness;
                            ma.getFitness();
                            mb.getFitness();
                            double nowa = ma.fitness;
                            double nowb = mb.fitness;
                            nowSolution.socre = nowSolution.socre - prea - preb + nowa + nowb;
                            cnt++;
                        }else{
                            mb.addInstance(ib);
                            ma.removeInstance(ib);
                            ma.addInstance(ia);
                            mt.removeInstance(ia);
                        }
                    }else{
                        ma.addInstance(ia);
                        mt.removeInstance(ia);
                    }
                }
            }
        }
//        System.out.println("Exchange Instances: " + cnt);
    }
}
