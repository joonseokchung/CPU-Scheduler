import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
public class Main {
    public static void main(String[] args) throws IOException {
        if(args.length < 2 || args.length > 5){
            System.out.println("Usage: java Scheduling/Main <inputFileName> <schedulingAlgorithm>");
            System.out.println("available SchedulingAlgorithm : FCFS, SJF, RR with quantum 1/10/100");
            return ;
        }

        //get infos from file
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            StringTokenizer st = new StringTokenizer(br.readLine(),"() ");
            br.close();
            int processNum = Integer.parseInt(st.nextToken());  //number of process
            //create process followed by info that got from file
            pcb[] processes = new pcb[processNum];
            for(int i = 0;i<processNum;i++){
                processes[i] = new pcb(2023+i,Integer.parseInt(st.nextToken()),Integer.parseInt(st.nextToken()),Integer.parseInt(st.nextToken()),Integer.parseInt(st.nextToken()));
            }
            String schedulingMethod = args[1];     //get scheduling method name
            int quantum = 0;                       //using in RR
            try{
                if(args.length>3){
                    quantum = Integer.parseInt(args[4]);
                }
            }
            catch (NumberFormatException ex){
                System.out.println("incorrect quantum");
                return;
            }
            catch (ArrayIndexOutOfBoundsException ex){
                System.out.println("Usage: java Scheduling/Main <inputFileName> <schedulingAlgorithm>");
                System.out.println("available SchedulingAlgorithm : FCFS, SJF, RR with quantum 1/10/100");
                return;
            }
            //variables that set strategy of Scheduler and array that will contain processes
            scheduling s_method = null;
            queuing q_method = null;
            //get strategy variables
            switch(schedulingMethod){
                case("FCFS"): {
                    s_method = new FCFSorSJF();
                    q_method = new q_sch();
                    break;}
                case("SJF"): {
                    s_method = new FCFSorSJF();
                    q_method = new pq_sch();
                    break;}
                case("RR"): {
                    s_method = new RR();
                    q_method = new q_sch();
                    break;}
                default: {
                    System.out.println("Scheduling method name incorrect");
                    System.out.println("available SchedulingAlgorithm : FCFS, SJF, RR with quantum 1/10/100");
                    return;}
            }
            //execute scheduler
            System.out.println("\nScheduling "+processNum+" processes by "+schedulingMethod);
            schedule My_Scheduler = new schedule(processes);
            My_Scheduler.exe(s_method,q_method,quantum);
        }
        catch(FileNotFoundException e) {
            System.out.println("File not Found");
            System.out.println("write correct file name");
        }
    }
}