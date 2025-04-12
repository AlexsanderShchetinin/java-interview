package shch.sskier.interview.concurrency;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseConcurrent<T> {


    private int numStream;
    private String nameStream;
    private T element;

    public BaseConcurrent(){
    }

    public BaseConcurrent(int numStream, String nameStream, T element){
        this.nameStream = nameStream;
        this.numStream = numStream;
        this.element = element;
    }

    public void readStream(){
        System.out.println(nameStream + " has " + numStream + " number yours stream! And has "
                + element + " element with type=" + element.getClass().getName());
    }


}
