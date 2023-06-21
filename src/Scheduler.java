import java.util.LinkedList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Collections;
public abstract class Scheduler {
    // queue strategy and scheduling strategy
    private queuing queuing;
    private scheduling scheduling;
    private pcb[] processes;
    private int processNum;
    private int time=0; // program time counter
    private int finishing_time; //finishing time
    private int running; // running process index number
    private int[] state_counter = {0,0,0,0,0}; // {new,ready,running,waiting,terminated} contains process number of each state
    private int cpu_util,io_util; //time of whatever job is in running/blocked
    private int a_tur_t,a_wai_t; // all turnaround/waiting time
    private String CPU,IO,ave_tur_t,ave_wai_t,Throughput_processes; // Percentage of time some job is running/blocked , average turnaround/waiting time , Throughput_processes
    private Queue<pcb> ready_q = new LinkedList<>(); // ready queue
    public Scheduler(pcb[] p){
        this.processes = p;
    }
    public void setQueuing(queuing q){
        this.queuing = q;
    }
    public void setScheduling(scheduling s){
        this.scheduling = s;
    }
    //program execute using given infos
    public void exe(scheduling s, queuing q,int quant){
        //setting strategy and random burst variables of process
        init(s,q,quant);
        while(this.state_counter[4]<processNum){
            //set arrived process into ready state and enqueue to ready queue
            for(int i = 0; i<this.processNum;i++){
                if(this.processes[i].getArrivalT() ==this.time){
                    this.processes[i].setProcessState(ready.getInstance());
                    this.state_counter[0]--;
                    this.state_counter[1]++;
                    qadd(this.ready_q,this.processes[i]);
                }
            }
            //set state of process by scheduling strategy
            do {
                this.running = running_check(this.state_counter, this.processes, this.queuing, this.ready_q, running);
                waiting_check(this.state_counter, this.processes, this.queuing, this.ready_q);
            }while(!processes[running].isRunning() && !ready_q.isEmpty());
            //add time and adjust variables of each process by their state
            time_plus();
        }
        //print Info of processes and summary of Scheduler
        calculate();
        printSummary();
    }
    public void init(scheduling s, queuing q, int quant){
        this.processNum = this.processes.length;
        for(int i = 0;i< this.processNum;i++){
            //set random variables of each process
            this.processes[i].randomBurst();
            this.processes[i].setQuantum(quant);
            this.processes[i].setProcessState(New.getInstance());
            this.state_counter[0]++;
        }
        //set strategy
        setScheduling(s);
        setQueuing(q);
    }
    public void time_plus(){
        this.time++;
        //if there's running process, add time to CPU_util
        if(this.state_counter[2]>0){
            this.cpu_util++;
        }
        //if there's waiting process, add time to io_util
        if(this.state_counter[3]>0){
            this.io_util++;
        }
        //adjust variables of each process by their state
        for(int i = 0; i<this.processNum;i++){
            this.processes[i].addTime();
        }
    }
    //get needed info from process and adjust result variables.
    public void calculate(){
        for(int i = 0;i< this.processNum;i++){
            this.a_tur_t += this.processes[i].getTurnaroundT();
            this.a_wai_t += this.processes[i].getWaitingT();
        }
        this.finishing_time = --this.time;
        this.CPU = String.format("%.2f",((double)this.cpu_util/(double)this.finishing_time)*100);
        this.IO = String.format("%.2f",((double)this.io_util/(double)this.finishing_time)*100);
        this.ave_tur_t = String.format("%.2f",((double)this.a_tur_t/(double)processNum));
        this.ave_wai_t = String.format("%.2f",((double)this.a_wai_t/(double)processNum));
        this.Throughput_processes = String.format("%.2f",((double)this.processNum/(double)finishing_time)*100);
    }
    public void qadd(Queue<pcb> q, pcb p){
        queuing.q_add(q,p);
    }
    public int running_check(int[] s_c, pcb[] p, queuing qs, Queue<pcb> q, int r){
        return scheduling.running_check(s_c,p,qs,q,r);
    }
    public void waiting_check(int[] s_c, pcb[] p, queuing qs, Queue<pcb> q){
        scheduling.waiting_check(s_c,p,qs,q);
    }
    public void printSummary(){
        System.out.println("\n====\t\tPrint Summary\t\t====\n");
        for(int i = 0;i< this.processNum;i++){
            this.processes[i].printInfo();
        }
        System.out.println("\n====\t\tProgram Summary\t\t====");
        System.out.println("Finishing Time : "+ this.finishing_time);
        System.out.println("CPU utilization : "+CPU+"%");
        System.out.println("I/O utilization : "+IO+"%");
        System.out.println("Throughput in processes completed per hundred time units : "+this.Throughput_processes);
        System.out.println("Average turnaround time : "+this.ave_tur_t);
        System.out.println("Average waiting time : "+this.ave_wai_t);
        System.out.println("===\t\t===\t\t===\t\t===\t\t===");
    }
}
class schedule extends Scheduler {
    public schedule(pcb[] p) {
        super(p);
    }
}
interface queuing{
    // add pcb to ready_queue
    public void q_add(Queue<pcb> q, pcb p);
}
class q_sch implements queuing {
    public void q_add(Queue<pcb> q, pcb p) {
        q.offer(p);
    }
}
class pq_sch implements queuing {
    public void q_add(Queue<pcb> q, pcb p) {
        PriorityQueue<pcb> que = new PriorityQueue<pcb>();//define priority queue on cpu burst
        while(!q.isEmpty()){
            que.offer(q.poll());
        }
        que.offer(p);
        while(!que.isEmpty()){ // give sorted priority queue to ready_queue
            q.offer(que.poll());
        }
        System.out.println(q);
    }
}
interface scheduling{
    // check running state process and change state according to its variables
    // running -> terminated , running -> waiting , running -> ready
    public int running_check(int[] s_c, pcb[] p, queuing qs, Queue<pcb> q, int r);
    // check waiting state process and change state according to its variables
    // waiting -> ready
    public void waiting_check(int[] s_c, pcb[] p, queuing qs, Queue<pcb> q);
    // get process index number from ready_queue
    //ready -> running
    public int popAndRun(int[] s_c, pcb[] p, Queue<pcb> q);
}
class FCFSorSJF implements scheduling {
    public int running_check(int[] s_c, pcb[] p, queuing qs, Queue<pcb> q, int r) {
        if(p[r].isRunning()){
            if(p[r].getRemainingCPUT() == 0){     // set state into terminated when remaining cpu time is 0
                p[r].setProcessState(terminated.getInstance());
                s_c[2]--;
                s_c[4]++;
                return popAndRun(s_c,p,q);  // return running process index number from ready_queue
            }
            else if(p[r].getRandomizedCPUburst() == 0 && p[r].getRemainingCPUT() != 0){    // set state into terminated when cpu burst is 0
                p[r].setProcessState(waiting.getInstance());
                s_c[2]--;
                s_c[3]++;
                return popAndRun(s_c,p,q);
            }
            else if(p[r].getRandomizedCPUburst() > 0){
                return r;
            }
        }
        else{   // if there's no running process, run process that got from ready_queue
            return popAndRun(s_c,p,q);
        }
        return r;
    }
    public void waiting_check(int[] s_c, pcb[] p, queuing qs, Queue<pcb> q) {
        waitingCheck(s_c,p,qs,q);
    }
    public int popAndRun(int[] s_c, pcb[] p, Queue<pcb> q) {
        return pop_Run(s_c,p,q);
    }
    public static void waitingCheck(int[] s_c, pcb[] p, queuing qs, Queue<pcb> q) {
        for(int i=0;i<p.length;i++){
            if(p[i].isWaiting()){       // check every process whether is waiting
                if(p[i].getRandomizedIOburst() ==0){            //if io burst is 0, set process state into ready and add to ready_queue also give random cpu burst and io burst
                    p[i].setProcessState(ready.getInstance());
                    s_c[3]--;
                    s_c[1]++;
                    p[i].randomBurst();
                    qs.q_add(q,p[i]);
                }
            }
        }
    }
    public static int pop_Run(int[] s_c, pcb[] p, Queue<pcb> q) {
        if (!q.isEmpty()) {
            for(int i = 0;i<p.length;i++) {
                if(p[i].getPid() == q.peek().getPid()){      //set process state into running that got from ready_queue
                    p[i].setProcessState(running.getInstance());
                    q.poll();
                    s_c[1]--;
                    s_c[2]++;
                    return i;
                }
            }
        }
        return 0;
    }
}
class RR implements scheduling {
    public int running_check(int[] s_c, pcb[] p, queuing qs, Queue<pcb> q, int r) {
        if(p[r].isRunning()){
            if(p[r].getRemainingCPUT() == 0){
                p[r].setProcessState(terminated.getInstance());
                s_c[2]--;
                s_c[4]++;
                return popAndRun(s_c,p,q);
            }
            else if(p[r].getRemainingQuantum()==0 && p[r].getRandomizedCPUburst() > 0 && p[r].getRemainingCPUT() != 0){  // if quantum is over, set process state into ready and add to ready_queue
                p[r].setProcessState(ready.getInstance());
                p[r].initRemainingQuantum();                    // initialize Remaining Quantum of process
                s_c[2]--;
                s_c[1]++;
                qs.q_add(q,p[r]);
                return popAndRun(s_c,p,q);
            }
            else if(p[r].getRandomizedCPUburst() == 0 && p[r].getRemainingCPUT() != 0){
                p[r].setProcessState(waiting.getInstance());
                p[r].initRemainingQuantum();
                s_c[2]--;
                s_c[3]++;
                return popAndRun(s_c,p,q);
            }
        }
        else if(!p[r].isRunning()){
            return popAndRun(s_c,p,q);
        }
        return r;
    }
    public void waiting_check(int[] s_c, pcb[] p, queuing qs, Queue<pcb> q) {
        FCFSorSJF.waitingCheck(s_c,p,qs,q);
    }
    public int popAndRun(int[] s_c, pcb[] p, Queue<pcb> q) {
        return FCFSorSJF.pop_Run(s_c,p,q);
    }
}