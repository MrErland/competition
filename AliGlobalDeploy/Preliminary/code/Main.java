/**
 * @author Erland
 * @version 2.0
 */

public class Main {

    public static void main(String[] args){

        Info info = new Info();

        // 首次适应
        FirstFit ff = new FirstFit(info);
        ff.run();

        // 模拟退火
        SA sa = new SA(info, ff.solution);
        sa.run();

        // 写文件
//        info.submitInfo();
    }
}
