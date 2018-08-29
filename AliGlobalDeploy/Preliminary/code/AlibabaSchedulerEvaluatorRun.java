import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.HashMap;
import com.google.common.base.Charsets;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Created by mou.sunm on 2018/07/02.
 */
public class AlibabaSchedulerEvaluatorRun {
    // 参数
    public static final double  alpha             = 10.;
    public static final double  beta              = 0.5;
    public static final int     T                 = 98;
    public static final int     EXEC_LIMIT        = 100000;
    
    // 静态数据
    private int                     n;                  // app数
    private int                     N;                  // inst数
    private int                     m;                  // machine数
    private int                     k;                  // 资源种类
    private List<Integer>           cpuIter;            // T个时刻的cpu资源
    private Map<String, Integer>    appIndex;
    private Map<String, Integer>    machineIndex;
    private String[]                apps;
    private String[]                machines;
    private Map<String, Integer>    inst2AppIndex;
    private double[][]              appResources;  
    private double[][]              machineResources;  
    private Map<Integer, Integer>[] appInterference;
    
    // 动态数据
    private Map<String, Integer>       inst2Machine;
    private double[][]                 machineResourcesUsed;
    private Map<Integer, Integer>[]    machineHasApp;
    
    protected double evaluate(BufferedReader bufferedReader) throws IOException {
        double costs = 0.;
        try {
            /** 读取执行数据 */
            List< Pair<String, Integer>> execs = new ArrayList<Pair<String, Integer>>();
            for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                String[] pair = line.split(",", -1);
                if (pair.length != 2) throw new Exception("Invaild solution file 1");
                if (!inst2AppIndex.containsKey(pair[0]) || !machineIndex.containsKey(pair[1]))
                    throw new Exception("Invaild solution file 2");
                execs.add(new ImmutablePair(pair[0], machineIndex.get(pair[1])));
            }
            /** 逐行执行 */
            int iter = 0;
            for (Pair<String, Integer> exec : execs) {
                iter++;
                //if (iter > EXEC_LIMIT) {
                    //System.out.println("超过EXECUTION LIMIT(" + EXEC_LIMIT+"), 执行中断");
                    //break;
                //}
                String  inst        = exec.getLeft();
                Integer machineIt   = exec.getRight();
                pickInstance(inst); // 先将inst从当前所属的machine删除
                String msg = toMachine(inst, machineIt);
                if (!msg.equals("success")) {
                    System.out.println("执行中断于第" + iter + "行: " + msg);
                    break; // 执行失败立即退出
                }
            }
            /** 计算终态得分 */
            // 检查inst是否全部放入machine
            for (String inst : inst2AppIndex.keySet())
                if (!inst2Machine.containsKey(inst)) throw new Exception("instance未全部分配");
            // 检查machine的终态
            for (int j = 0; j < m; j++) {
                Map<Integer, Integer> hasApp = machineHasApp[j];
                if (hasApp.size() == 0) continue;
                // 检查互斥条件
                for (Integer conditionalApp : hasApp.keySet()) {
                    if (hasApp.get(conditionalApp) <= 0) throw new Exception("[DEBUG 1]Stupid Judger.");
                    for (Integer checkApp : appInterference[conditionalApp].keySet()) {
                        if (hasApp.containsKey(checkApp)) {
                            if (hasApp.get(checkApp) > appInterference[conditionalApp].get(checkApp))
                                throw new Exception("终态存在干扰冲突");
                        }
                    }
                }
                // 检查资源限制
                for (int i = 0; i < k; i++)
                    if (dcmp(machineResourcesUsed[j][i] - machineResources[j][i]) > 0)
                        throw new Exception("终态存在资源过载");
                // 技术得分
                for (Integer t : cpuIter) {
                    double usage = machineResourcesUsed[j][t] / machineResources[j][t];
                    costs += 1. + alpha*(Math.exp(Math.max(0., usage - beta)) - 1.);
                }
            }
            costs /= T;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
            costs = 1e9;
        }
        return costs;
    }
    
    // 读取数据
    protected void init(BufferedReader bufferedReader) throws IOException {
        /* Preprocessing: cat *.csv to one file as:
               n
               app_resources.csv
               m
               machine_resources.csv
               N
               instance_deploy.csv
               iterference_cnt
               app_interference.csv
            judge framework 
        */
        /** cpuIter */
        cpuIter = new ArrayList<Integer>();
        for (int i = 0; i < T; i++)
            cpuIter.add(i);
        /** Read app_resources */
        n = Integer.parseInt(bufferedReader.readLine());
        apps = new String[n];
        for (int i = 0; i < n; i++) {
            // appId,resources
            String line = bufferedReader.readLine();
            String[] parts = line.split(",", -1);
            List<Double> resources = new ArrayList<Double>();
            for (String x : parts[1].split("\\|", -1))
                resources.add(Double.parseDouble(x));
            for (String x : parts[2].split("\\|", -1))
                resources.add(Double.parseDouble(x));
            for (int j = 3; j < parts.length; j++)
                resources.add(Double.parseDouble(parts[j]));
            if (i == 0) {
                k = resources.size();
                appIndex = new HashMap<String, Integer>();
                appResources = new double[n][k];
            }
            if (k != resources.size()) 
                throw new IOException("[DEBUG 2]Invaild problem");
            if (appIndex.containsKey(parts[0]))
                throw new IOException("[DEBUG 3]Invaild problem");
            appIndex.put(parts[0], i);
            apps[i] = parts[0];
            for (int j = 0; j < k; j++)
                appResources[i][j] = resources.get(j);
        }
        /** Read machine_resources*/
        m = Integer.parseInt(bufferedReader.readLine());
        machineResources = new double[m][k];
        machineResourcesUsed = new double[m][k];
        machineIndex = new HashMap<String, Integer>();
        machineHasApp = new Map[m];
        machines = new String[m];
        for (int i = 0; i < m; i++) {
            // machineId,resources
            String line = bufferedReader.readLine();
            String[] parts = line.split(",", -1);
            if (machineIndex.containsKey(parts[0]))
                throw new IOException("[DEBUG 4]Invaild problem");
            machineIndex.put(parts[0], i);
            machines[i] = parts[0];
            machineHasApp[i] = new HashMap<Integer, Integer>();
            double cpu = Double.parseDouble(parts[1]);
            double mem = Double.parseDouble(parts[2]);
            for (int j = 0; j < T; j++) {
                machineResources[i][j]   = cpu;
                machineResources[i][T+j] = mem;
            }
            for (int j = 3; j < parts.length; j++)
                machineResources[i][2*T + j - 3] = Double.parseDouble(parts[j]);
            for (int j = 0; j < k; j++)
                machineResourcesUsed[i][j] = 0.;
        }
        /** Read instance_deploy */
        N = Integer.parseInt(bufferedReader.readLine());
        inst2AppIndex = new HashMap<String, Integer>();
        inst2Machine  = new HashMap<String, Integer>();
        for (int i = 0; i < N; i++) {
            String line = bufferedReader.readLine();
            String[] parts = line.split(",", -1);
            if (inst2AppIndex.containsKey(parts[0]))
                throw new IOException("[DEBUG 5]Invaild problem");
            if (!appIndex.containsKey(parts[1]))
                throw new IOException("[DEBUG 6]Invaild problem");
            inst2AppIndex.put(parts[0], appIndex.get(parts[1]));
            if (!"".equals(parts[2])) {
                if (!machineIndex.containsKey(parts[2]))
                    throw new IOException("[DEBUG 7]Invaild problem");
                toMachine(parts[0], machineIndex.get(parts[2]), false);
            }
        }
        /** Read app_interference */
        int icnt = Integer.parseInt(bufferedReader.readLine());
        appInterference = new Map[n];
        for (int i = 0; i < n; i++)
            appInterference[i] = new HashMap<Integer, Integer>();
        for (int i = 0; i < icnt; i++) {
            String line = bufferedReader.readLine();
            String[] parts = line.split(",", -1);
            if (!appIndex.containsKey(parts[0]) || !appIndex.containsKey(parts[1]))
                throw new IOException("[DEBUG 8]Invaild problem");
            int app1 = appIndex.get(parts[0]);
            int app2 = appIndex.get(parts[1]);
            int limit = Integer.parseInt(parts[2]);
            Map<Integer, Integer> inter = appInterference[app1];
            if (inter.containsKey(app2))
                throw new IOException("[DEBUG 9]Invaild problem");
            if (app1 == app2) limit += 1; //self-interference +1 here
            inter.put(app2, limit);
        }
    }
    
    
    private String toMachine(String inst, int machineIt)
    {
        return toMachine(inst, machineIt, true);
    }
    private String toMachine(String inst, int machineIt, boolean doCheck)
    {
        int appIt       = inst2AppIndex.get(inst);
        Map<Integer, Integer> hasApp = machineHasApp[machineIt];
        if (doCheck) {
            // 检查互斥规则
            int nowHas = 0;
            if (hasApp.containsKey(appIt))
                nowHas = hasApp.get(appIt);
            for (Integer conditionalApp : hasApp.keySet()) {
                if (hasApp.get(conditionalApp) <= 0) continue;
                if (!appInterference[conditionalApp].containsKey(appIt)) continue;
                if (nowHas + 1 > appInterference[conditionalApp].get(appIt)) {
                    return "App Interference, inst: " + inst + ", "
                        + apps[conditionalApp] + " -> " + apps[appIt] + ", "
                        + (nowHas + 1) + " > " + appInterference[conditionalApp].get(appIt); 
                }
            }
            for (Integer checkApp : hasApp.keySet()) {
                if (!appInterference[appIt].containsKey(checkApp)) continue;
                if (hasApp.get(checkApp) > appInterference[appIt].get(checkApp)) {
                    return "App Interference, inst: " + inst + ", "
                        + apps[appIt] + " -> " + apps[checkApp] + ", "
                        + (nowHas + 1) + " > " + appInterference[appIt].get(checkApp); 
                }
            }
            // 检查资源限制
            for (int i = 0; i < k; i++)
                if (dcmp(machineResourcesUsed[machineIt][i] + appResources[appIt][i] - machineResources[machineIt][i]) > 0) 
                    return "Resource Limit: inst: " + inst + ", " 
                        + "machine: " + machines[machineIt] + ", app: " + apps[appIt] + ", resIter: " + i + ", "
                        + machineResourcesUsed[machineIt][i] + " + " + appResources[appIt][i] + " > " + machineResources[machineIt][i];
        }
        // 将inst放入新的machine
        inst2Machine.put(inst, machineIt);
        if (!hasApp.containsKey(appIt))
            hasApp.put(appIt, 0);
        hasApp.put(appIt, hasApp.get(appIt) + 1);
        for (int i = 0; i < k; i++)
            machineResourcesUsed[machineIt][i] += appResources[appIt][i];
        
        return "success";
    }
    private void pickInstance(String inst)
    {
        if (!inst2Machine.containsKey(inst)) return;
        int appIt       = inst2AppIndex.get(inst);
        int fromMachine = inst2Machine.get(inst);
        // 更新machineHasApp
        Map<Integer, Integer> fromHasApp = machineHasApp[fromMachine];
        fromHasApp.put(appIt, fromHasApp.get(appIt) - 1);
        if (fromHasApp.get(appIt) <= 0)
            fromHasApp.remove(appIt);
        // 更新machineResourcesUsed
        for (int i = 0; i < k; i++)
            machineResourcesUsed[fromMachine][i] -= appResources[appIt][i];
        // 更新inst2Machine
        inst2Machine.remove(inst);
    }

    private int dcmp(double x) {
        if (Math.abs(x) < 1e-9) return 0;
        return x < 0. ? -1 : 1;
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length != 5 && args.length != 2){
            System.err.println("传入参数有误，使用方式为：java -cp xxx.jar com.aliyun.tianchi.mgr.evaluate.evaluate.file.evaluator.AlibabaSchedulerEvaluatorRun app_resources.csv machine_resources.csv instance_deploy.csv app_interference.csv result.csv");
            return;
        }
        
        InputStream problem;
        InputStream result;
        if (args.length == 5) {
            // 将赛题拼成评测数据
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 4; i++) {
                List<String> lines = new ArrayList<String>();
                BufferedReader bs = new BufferedReader(new FileReader(new File(args[i])));
                for (String line = bs.readLine(); line != null; line = bs.readLine())
                    lines.add(line);
                sb.append(""+lines.size()).append("\n");
                for (String line : lines)
                    sb.append(line).append("\n");
            }
            String alldata = sb.toString();
            problem = new ByteArrayInputStream(alldata.getBytes());
            result = new FileInputStream(args[4]);
        }
        else {
            problem = new FileInputStream(args[0]);
            result = new FileInputStream(args[1]);
        }
        
        // 评测
        AlibabaSchedulerEvaluatorRun evaluator = new AlibabaSchedulerEvaluatorRun();
        evaluator.init(new BufferedReader(new InputStreamReader(problem, Charsets.UTF_8)));
        double score = evaluator.evaluate(new BufferedReader(new InputStreamReader(result, Charsets.UTF_8)));
        System.out.println("选手所得分数为：" + score);
    }
}
