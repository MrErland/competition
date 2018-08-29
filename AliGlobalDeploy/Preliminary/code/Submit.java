
/**
 * @author Erland
 *          提交格式
 */
public class Submit {

    public int instanceId;
    public int machineId;

    Submit(int instanceId, int machineId) {
        this.instanceId = instanceId;
        this.machineId = machineId;
    }

    Submit(Submit submit){
        this.machineId = submit.machineId;
        this.instanceId = submit.instanceId;
    }
}
