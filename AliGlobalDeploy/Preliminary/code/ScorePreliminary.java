
/**
 * @author Erland
 *          初赛计分
 */

public class ScorePreliminary {
    private String error;
    private double score;
    private Info info;
    private Solution solution;

    ScorePreliminary(Info info, Solution solution){
        this.info = info;
        this.solution = solution;
        score = 0.;
        error = new String();
    }

    boolean runSubmit() {
        int n = 0;
        for(MachineInstance mi : solution.machineInstances){
            if(mi.isAllow()){
                n += mi.instances.size();
                mi.getFitness();
                score += mi.fitness;
            }else{
                error = "Not allowed";
                return false;
            }
        }
        if(n != info.instances.size()) return false;
        return true;
    }

    public void print(){
        System.out.println("Score = " + score);
    }

    public void error(){
        System.out.println(error);
    }
}
