import java.io.*;
import java.util.*;

/**
 * Created by Karthik on 1/9/2017.
 */
public class TextFileSorter extends Thread {
    private String inputFile = null;
    private String keyIdx="";
    private Comparator alphabeticComparator=null;
    private String outputDir="";
    public TextFileSorter(String outputDir, String inputFile, String keyIdx, Comparator alphabeticComparator){
        this.outputDir = outputDir;
        this.inputFile = inputFile;
        this.keyIdx = keyIdx;
        this.alphabeticComparator = alphabeticComparator;
    }

    public void run(){
        try{
            List<File> l = this.sortInBatch(new File(this.inputFile), this.alphabeticComparator);
            this.mergeSortedFiles(l, new File(this.inputFile.replace(".txt","_"+this.keyIdx+".txt")), this.alphabeticComparator);
            Runtime.getRuntime().gc();
            Thread.sleep(1000);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    public List<File> sortInBatch(File file, Comparator<String> cmp) throws IOException {
        List<File> files = new ArrayList<File>();
        FileReader frObj = new FileReader(file);
        BufferedReader fbr = new BufferedReader(frObj);
        int totalrowread = 0;
        try{
            List<String> templist = new ArrayList<String>();
            String line = "";
            int batchSizeInBytes = 0;
            try{
                while(line!=null){
                    if(Runtime.getRuntime().freeMemory()<DVConstants.RAMSIZE)
                        throw new RuntimeException("Can't free enough memory.");
                    if((line=fbr.readLine())==null)
                        throw new IOException("Empty records detected while sorting");
                    do{
                        templist.add(line);
                        batchSizeInBytes += line.length();
                    }while((Runtime.getRuntime().freeMemory()> DVConstants.RAMSIZE) && batchSizeInBytes < DVConstants.BATCHSIZE &&
                            ((line = fbr.readLine())!=null));
                    files.add(sortAndSave(templist,cmp));
                    templist.clear();
                    batchSizeInBytes = 0;
                    Runtime.getRuntime().gc();
                    Thread.sleep(1000);
                }
            }catch(EOFException e){
                if(templist.size()>0){
                    files.add(sortAndSave(templist,cmp));
                    templist.clear();
                    System.out.println("EOF Exception="+e.getMessage());
                }
            }

        }catch(Exception oef){
            System.out.println("Exception during sort in batch="+oef.getMessage());
            oef.printStackTrace();
        }
        finally {
            fbr.close();
            frObj.close();
        }
        return files;
    }

    public File sortAndSave(List<String> tmpList, Comparator<String> cmp) throws IOException {
        Collections.sort(tmpList, cmp);
        System.out.println(this.outputDir);
        File newtmpfile = File.createTempFile("sortInBatch", "flatfile", new File(this.outputDir+"/tmp"));
        newtmpfile.deleteOnExit();
        FileWriter fwObj = new FileWriter(newtmpfile);
        BufferedWriter fbw = new BufferedWriter(fwObj);
        try{
            for(String r: tmpList){
                fbw.write(r);
                fbw.newLine();
            }
        }finally{
            fbw.flush();
            fbw.close();
            fwObj.close();
        }
        return newtmpfile;
    }

    public int mergeSortedFiles(List<File> files, File outputfile, Comparator<String> cmp) throws IOException {
        PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>();
        for(File f: files){
            BinaryFileBuffer bfb = new BinaryFileBuffer(f, cmp);
            pq.add(bfb);
        }
        FileWriter fwObj = new FileWriter(outputfile);
        BufferedWriter fbw = new BufferedWriter(fwObj);
        int rowcounter = 0;
        try{
            while (pq.size()>0){
                BinaryFileBuffer bfb = pq.poll();
                String r = bfb.pop();
                fbw.write(r);
                fbw.newLine();
                ++rowcounter;
                if(bfb.empty()){
                    bfb.fbr.close();
                    bfb.fr.close();
                    bfb.originalfile.delete();
                }else{
                    pq.add(bfb);
                }
            }
        }finally {
            fbw.close();
            fwObj.close();
        }
        return rowcounter;
    }

    public static void main(String[] args){
        boolean isNumeric = false;
        String inputFile = "./output/extracts/abc.txt";
        String outputDir = "./output/extracts";
        int[] keys={1};
        String keyIdx = "";
        for(int i:keys){
            keyIdx = "_"+i;
        }

        TextFileSorter sorterObj = null;
        Comparator alphabeticComparator = new AlphabeticComparator(keys, DVConstants.RECDELIM_SPLIT);
        sorterObj = new TextFileSorter(outputDir, inputFile, keyIdx, alphabeticComparator);
        if(sorterObj!=null){
            try{
                sorterObj.start();
                sorterObj.join();
            }catch(Exception exp){
                System.out.println(exp.getMessage());
            }
        }
    }

    public void destroy(){
        this.stop();
    }
    protected void finalize() throws Throwable{
        try{
            this.inputFile =null;
            this.alphabeticComparator = null;
        }finally {
            super.finalize();
        }
    }

}

class BinaryFileBuffer implements Comparable<BinaryFileBuffer>{
    public FileReader fr;
    public BufferedReader fbr;
    private List<String> buf = new ArrayList<String>();
    int currentpointer = 0;
    Comparator<String> mCMP;
    public File originalfile;

    public BinaryFileBuffer(File f, Comparator<String> cmp) throws IOException{
        originalfile = f;
        mCMP = cmp;
        fr = new FileReader(f);
        fbr = new BufferedReader(this.fr);
        reload();
    }

    public boolean empty(){
        return buf.size()==0;
    }

    private void reload() throws IOException{
        buf.clear();
        try{
            String line;
            while((buf.size() < DVConstants.BUFFERSIZE) && ((line = fbr.readLine())!= null))
                buf.add(line);

        }catch(EOFException eof){
            System.out.println(eof.getMessage());
        }
    }

    public String peek(){
        if(empty()) return null;
        return buf.get(currentpointer);
    }

    public String pop() throws IOException{
        String answer = peek();
        ++currentpointer;
        if(currentpointer == buf.size()){
            reload();
            currentpointer = 0;
        }
        return answer;
    }

    @Override
    public int compareTo(BinaryFileBuffer b) {
        return mCMP.compare(peek(), b.peek());
    }
}
