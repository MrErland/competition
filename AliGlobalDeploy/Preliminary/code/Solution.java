import java.util.ArrayList;

/**
 * @author Erland
 *              解的形式
 */

public class Solution {

    public ArrayList<Submit> submits;   // 迁移顺序
    public ArrayList<MachineInstance> machineInstances;   // 包含实例的物理机
    public double socre;        // 得分
    private Info info;

    public ArrayList<Integer> instanceNoInMachines;     // 没有分配物理机的实例

    Solution(Info info){
        this.info = info;
        socre = 0.0;
        machineInstances = new ArrayList<>(info.machines.size());
        submits = new ArrayList<>();
        instanceNoInMachines = new ArrayList<>();

        for(int i = 0; i < info.machines.size(); i++){      // 将每个物理机初始化
            machineInstances.add(new MachineInstance(info, info.machines.get(i).id));
        }
        initStatus();
    }

    Solution(Solution solution){
        this.socre = solution.socre;
        this.info = solution.info;
//        this.instanceNoInMachines = solution.instanceNoInMachines;

//        this.submits = new ArrayList<>(solution.submits.size());
        this.machineInstances = new ArrayList<>(solution.machineInstances.size());
//        for(Submit submit : solution.submits){
//            this.submits.add(new Submit(submit));
//        }

        for(MachineInstance mi : solution.machineInstances){
            this.machineInstances.add(new MachineInstance(mi));
        }
    }

    private void initStatus(){
        for (Instance instance : info.instances){
            if(instance.machineId == Instance.NoMachine){
                instanceNoInMachines.add(instance.id);
            }
            else {
                for(MachineInstance mi : machineInstances){     // 注意实例初始状态不符合约束，因此不检查约束条件直接插入
                    if(mi.machine.id == instance.machineId){
                        mi.instances.add(instance.id);
                        mi.nowP += info.indexApp.get(instance.appId).P;
                        mi.nowM += info.indexApp.get(instance.appId).M;
                        mi.nowPM += info.indexApp.get(instance.appId).PM;
                        mi.nowDisk += info.indexApp.get(instance.appId).diskHold;
                        App app = info.indexApp.get(instance.appId);
                        if(mi.nowCpu.isEmpty()){
                            for (int i = 0; i < app.cpuHold.size(); i++) {
                                mi.nowCpu.add(app.cpuHold.get(i));
                                mi.nowMem.add(app.memHold.get(i));
                            }
                        }else {
                            for (int i = 0; i < app.cpuHold.size(); i++) {
                                mi.nowCpu.set(i, mi.nowCpu.get(i) + app.cpuHold.get(i));
                                mi.nowMem.set(i, mi.nowMem.get(i) + app.memHold.get(i));
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public void getScore(){
        socre = 0.0;
        for(MachineInstance mi : machineInstances){
            mi.getFitness();
            socre += mi.fitness;
        }
    }
}
