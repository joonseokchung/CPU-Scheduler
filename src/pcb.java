import java.util.Objects;
import java.util.Random;

public class pcb implements Comparable<pcb> {
    private int pid;
    private int arrivalT, totalCPUT, CPUburst, IOburst; // arrival time, total CPU time, Maximum CPU burst time, Maximum IO burst time
    private int quantum, RemainingQuantum; // Using in round-robin
    private int remainingCPUT, randomizedCPUburst, randomizedIOburst; //remaining cpu time, randomized cpu burst , randomized io burst
    //Finishing time , Turnaround time, CPU time (time that remained on running state)
    //IO time (time that remained on blocked state), Waiting time (time that remained on ready state)
    private int finishingT, turnaroundT, CPUT, IOT, waitingT = 0;
    private state processState; // process state
    private Random random = new Random();
    public pcb(int id,int A,int C, int B, int IO){
        this.pid = id;
        this.arrivalT = A;
        this.totalCPUT = C;
        this.remainingCPUT = C;
        this.CPUburst = B;
        this.IOburst = IO;
        this.processState = New.getInstance();
    }
    public void setProcessState(state s){
        this.processState = s;
    }
    public void addTime(){
        processState.addTime(this);
    }
    //put random number up to given Maximum CPU/IO time
    public void randomBurst(){
        if(this.CPUburst >0) {
            this.randomizedCPUburst = random.nextInt(this.CPUburst) + 1;
        }
        else{
            this.randomizedCPUburst = this.CPUburst;
        }
        if(this.IOburst >0){
            this.randomizedIOburst = random.nextInt(this.IOburst)+1;

        }
        else{
            this.randomizedIOburst = this.IOburst;
        }
    }
    //print process summary
    public void printInfo(){
        System.out.println("---\t-- pid = "+this.pid+" --\t---");
        System.out.println("Arrival time : "+this.arrivalT +", Total CPU time : "+this.totalCPUT +", CPU burst bound : "+this.CPUburst +", IO burst bound : "+this.IOburst);
        System.out.println("Finishing time : "+this.finishingT +", Turnaround time : "+this.turnaroundT);
        System.out.println("CPU time : "+this.CPUT +", IO time : "+this.IOT +", Waiting time : "+this.waitingT);
        System.out.println("---\t---\t---\t---\t---\t---");
    }
    public boolean isRunning(){
        if(Objects.equals(this.processState, running.getInstance())){
            return true;
        }
        return false;
    }
    public boolean isWaiting(){
        if(Objects.equals(this.processState, waiting.getInstance())){
            return true;
        }
        return false;
    }
    //need to compare processes when adding process to priority queue
    public int getPid(){
        return this.pid;
    }
    public int getArrivalT(){
        return this.arrivalT;
    }
    public void setQuantum(int q){
        this.quantum = q;
    }
    public void initRemainingQuantum(){
        this.RemainingQuantum = this.quantum;
    }
    public int getRemainingQuantum(){
        return this.RemainingQuantum;
    }
    public void setRemainingQuantum(int q){
        this.RemainingQuantum = q;
    }
    public int getRemainingCPUT(){
        return this.remainingCPUT;
    }
    public void setRemainingCPUT(int t){
        this.remainingCPUT = t;
    }
    public int getRandomizedCPUburst(){
        return this.randomizedCPUburst;
    }
    public void setRandomizedCPUburst(int t){
        this.randomizedCPUburst = t;
    }
    public int getRandomizedIOburst(){
        return this.randomizedIOburst;
    }
    public void setRandomizedIOburst(int t){
        this.randomizedIOburst = t;
    }
    public int getFinishingT(){
        return this.finishingT;
    }
    public void setFinishingT(int t){
        this.finishingT = t;
    }
    public int getTurnaroundT(){
        this.turnaroundT = this.finishingT -this.arrivalT;
        return this.turnaroundT;
    }
    public int getCPUT(){
        return this.CPUT;
    }
    public void setCPUT(int t){
        this.CPUT = t;
    }
    public int getIOT(){
        return this.IOT;
    }
    public void setIOT(int t){
        this.IOT = t;
    }
    public int getWaitingT(){
        return this.waitingT;
    }
    public void setWaitingT(int t){
        this.waitingT = t;
    }

    @Override
    public int compareTo(pcb p) {       //using when enqueuing to priority queue
        if(this.randomizedCPUburst < p.getRandomizedCPUburst()) return -1;
        else if(this.randomizedCPUburst >p.getRandomizedCPUburst()) return 1;
        return 0;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Process cpu : ");
        sb.append(this.randomizedCPUburst);
        return sb.toString();
    }
}
interface state {
    //adjust time to each variable according to its state
    public void addTime(pcb p);
}
class New implements state {
    private static New New = new New();
    private New(){}
    public static New getInstance(){
        return New;
    }
    public void addTime(pcb p) {
        p.setFinishingT(p.getFinishingT()+1);
    }


}
class ready implements state {
    private static ready ready = new ready();
    private ready(){}
    public static ready getInstance(){
        return ready;
    }
    public void addTime(pcb p) {
        p.setFinishingT(p.getFinishingT()+1);
        p.setWaitingT(p.getWaitingT()+1);
        p.initRemainingQuantum();
    }
}
class running implements state {
    private static running running = new running();
    private running(){}
    public static running getInstance(){
        return running;
    }
    public void addTime(pcb p) {
        p.setFinishingT(p.getFinishingT()+1);
        p.setCPUT(p.getCPUT()+1);
        p.setRemainingCPUT(p.getRemainingCPUT()-1);
        p.setRandomizedCPUburst(p.getRandomizedCPUburst()-1);
        p.setRemainingQuantum(p.getRemainingQuantum()-1);
    }
}
class waiting implements state {
    private static waiting waiting= new waiting();
    private waiting(){}
    public static waiting getInstance(){
        return waiting;
    }
    public void addTime(pcb p) {
        p.setFinishingT(p.getFinishingT()+1);
        p.setIOT(p.getIOT()+1);
        p.setRandomizedIOburst(p.getRandomizedIOburst()-1);
        p.initRemainingQuantum();
    }
}
class terminated implements state {
    private static terminated terminated = new terminated();
    private terminated(){}
    public static terminated getInstance(){
        return terminated;
    }
    public void addTime(pcb p) {}
}