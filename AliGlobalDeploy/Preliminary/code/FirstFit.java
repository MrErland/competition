
/**
 * @author Erland
 *          首次适应法--初解
 */

public class FirstFit {

    private Info info;
    public Solution solution;

    FirstFit(Info info){
        this.info = info;
        solution = new Solution(info);
    }

    public void run(){
        for(MachineInstance mi : solution.machineInstances){
            while (!mi.isAllow()){
                int inst = mi.instances.get(0);
                if(!mi.removeInstance(info.indexInst.get(inst)))
                    System.out.println("remove error");
                boolean b = false;
                for (int i = solution.machineInstances.size() - 1; i >= 0; i--){
                    MachineInstance nextmi = solution.machineInstances.get(i);
                    if(nextmi.addInstance(info.indexInst.get(inst))) {
                        b = true;
//                        solution.submits.add(new Submit(inst, nextmi.machine.id));      // 迁移的过程记录
                        break;
                    }
                }
                if(!b){
                    System.out.println("init status move error");
                }
            }
        }

        for(int inst : solution.instanceNoInMachines){     // 对于没有分配的实例进行分配
            boolean b = false;
            for(int i = solution.machineInstances.size() - 1; i >= 0; i--){
                MachineInstance mi = solution.machineInstances.get(i);
                if(mi.instances.size() <= 6 && mi.addInstance(info.indexInst.get(inst))){
//                    solution.submits.add(new Submit(inst, mi.machine.id));
                    b = true;
                    break;
                }
            }
            if(!b){
                System.out.println("add new instance error" + inst);
            }
        }

//        info.submits = solution.submits;
    }
}
