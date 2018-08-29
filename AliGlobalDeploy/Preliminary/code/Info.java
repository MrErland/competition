import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * @author Erland
 *      读取文件的数据/结果数据
 */

public class Info {

    // read info
    public ArrayList<Machine> machines;
    public ArrayList<Instance> instances;
    public ArrayList<App> apps;

    public ArrayList<Constraint> constraints;

    //index
    public ArrayList<Machine> indexMach;
    public ArrayList<Instance> indexInst;
    public ArrayList<App> indexApp;

    // submit info
    public ArrayList<Submit> submits;        // 提交文件中包含了所有实例的迁移

    Info(){
        readInfo();
        getIndex();
    }

    public void submitMachineInst(Solution solution){
        try {
            File csv = new File("submit/MachineInst_b.csv");
            BufferedWriter writer = new BufferedWriter(new FileWriter(csv, false));
            for(MachineInstance mi : solution.machineInstances){
                if(mi.instances.isEmpty()) continue;
                for(int inst : mi.instances) {
                    writer.write("Machine_" + Integer.toString(mi.machine.id) + "," + "inst_" + Integer.toString(inst));
                    writer.newLine();
                }
            }
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void submitInfo() {
        if(submits.isEmpty()) return;
        try {
            Date date = new Date();
            String sd = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
            File csv = new File("submit/submit_b_"+sd+".csv");
         //   File csv = new File("submit/submit.csv");
            BufferedWriter writer = new BufferedWriter(new FileWriter(csv, false));
            for(Submit s : submits){
                writer.write("inst_" + Integer.toString(s.instanceId) + "," + "machine_" + Integer.toString(s.machineId));
                writer.newLine();
            }
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 建立索引数组
    private void getIndex(){
        indexMach = new ArrayList<>(Collections.nCopies(9999, machines.get(0)));
        for(Machine m : machines){
            indexMach.set(m.id, m);
        }

        indexInst = new ArrayList<>(Collections.nCopies(99999 + 1, instances.get(0)));
        for(Instance i : instances){
            indexInst.set(i.id, i);
        }

        indexApp = new ArrayList<>(Collections.nCopies(9999, apps.get(0)));
        for(App a : apps){
            indexApp.set(a.id, a);
        }
    }

    public void readInfo(){

        // read machine
        machines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("data/scheduling_preliminary_b_machine_resources_20180726.csv"));
            String line = null;
            while ((line = reader.readLine()) != null){
                String[] item = line.split(",");
                int id = Integer.parseInt(item[0].split("_")[1]);
                int cpu = Integer.parseInt(item[1]);
                int mem = Integer.parseInt(item[2]);
                int disk = Integer.parseInt(item[3]);
                int p = Integer.parseInt(item[4]);
                int m = Integer.parseInt(item[5]);
                int pm = Integer.parseInt(item[6]);
                machines.add(new Machine(id, cpu, mem, disk, p, m, pm));
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // read instances
        instances = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("data/scheduling_preliminary_b_instance_deploy_20180726.csv"));
            String line = null;
            while ((line = reader.readLine()) != null){
                String[] item = line.split(",");
                int id = Integer.parseInt(item[0].split("_")[1]);
                int appid = Integer.parseInt(item[1].split("_")[1]);
                int machineid = Instance.NoMachine;
                if(item.length > 2){
                    machineid = Integer.parseInt(item[2].split("_")[1]);
                }
                instances.add(new Instance(id, appid, machineid));
            }
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        // read app
        apps = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("data/scheduling_preliminary_b_app_resources_20180726.csv"));
            String line = null;
            while ((line = reader.readLine()) != null){
                String[] item = line.split(",");
                int id = Integer.parseInt(item[0].split("_")[1]);
                ArrayList<Double> cpu = new ArrayList<>();
                for(String s : item[1].split("\\|")){       // 注意转义字符
                    cpu.add(Double.parseDouble(s));
                }
                ArrayList<Double> mem = new ArrayList<>();
                for(String s : item[2].split("\\|")){
                    mem.add(Double.parseDouble(s));
                }
                double disk = Double.parseDouble(item[3]);
                int p = Integer.parseInt(item[4]);
                int m = Integer.parseInt(item[5]);
                int pm = Integer.parseInt(item[6]);
                apps.add(new App(id, cpu, mem, disk, p, m, pm));
            }
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        // read constraints
        constraints = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("data/scheduling_preliminary_b_app_interference_20180726.csv"));
            String line = null;
            while ((line = reader.readLine()) != null){
                String[] item = line.split(",");
                int appid1 = Integer.parseInt(item[0].split("_")[1]);
                int appid2 = Integer.parseInt(item[1].split("_")[1]);
                int count = Integer.parseInt(item[2]);
                constraints.add(new Constraint(appid1, appid2, count));
            }
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
