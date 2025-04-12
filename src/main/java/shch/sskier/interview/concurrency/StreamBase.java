package shch.sskier.interview.concurrency;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;

import java.lang.reflect.Array;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public abstract class StreamBase {

    // check run time
    static LocalTime timer = LocalTime.now();
    static LocalTime timer2;

    // iteration count
    static int iCount = 100;

    // create 2 collection ArrayList - нельзя использовать если есть несколько потоков!!!
    static List<ConcurrentExtension2Impl<Integer, Long>> arrayList2 = new ArrayList<>();
    static List<ConcurrentExtension<Integer, String>> arrayList1 = new ArrayList<>();
    static List<Integer> arrayListInteger = new ArrayList<>();

    static List<Integer> linkedListInteger = new LinkedList<>();
    static Map<Integer, Long> hashMapIntKeyLongValue = new HashMap<>();

    // применяем потокобезопасные коллекции!
    static List<ConcurrentExtension2Impl<Integer, Long>> synchronizedList2 = Collections.synchronizedList(new ArrayList<>());
    static List<ConcurrentExtension<Integer, String>> synchronizedList1 = Collections.synchronizedList(new ArrayList<>());


    // Через CopyOnWriteArrayList
    static List<ConcurrentExtension2Impl<Integer, Long>> concurrentList2 = new CopyOnWriteArrayList<>();
    static List<ConcurrentExtension<Integer, String>> concurrentList1 = new CopyOnWriteArrayList<>();


    public void calculateSize(Object obj, String textObj) {
        // Получаем размер объекта и его полей
        long size = GraphLayout.parseInstance(obj).totalSize();
        System.out.println("Total size of the " + textObj + ": " + size + " bytes");
    }

    public void analiseFieldMemory(Object obj){
        String printable = ClassLayout.parseInstance(obj).toPrintable();
        System.out.println("______________________________________________________________________________");
        System.out.println("ANALISE MEMORY in " + obj.getClass().getName().toUpperCase() + ": " + printable);
        System.out.println("------------------------------------------------------------------------------");
    }

    void auditMemoryOurClass(){
        System.out.println("_________________________________________________");
        System.out.println("-----AUDIT MEMORY ABSTRACT CLASS StreamBase:-----");
        System.out.println("-------------------------------------------------");
        calculateSize(concurrentList1, "empty concurrentList1");
        analiseFieldMemory(concurrentList1);
        calculateSize(concurrentList2, "empty concurrentList2");

        calculateSize(synchronizedList1, "empty synchronizedList1");
        calculateSize(synchronizedList2, "empty synchronizedList2");

        calculateSize(arrayListInteger, "empty arrayListInteger");
        calculateSize(arrayList1, "empty arrayList1");
        calculateSize(arrayList2, "empty arrayList2");

        calculateSize(linkedListInteger, "empty linkedListInteger");
        calculateSize(hashMapIntKeyLongValue, "empty hashMapIntKeyLongValue");

        calculateSize(timer, " LocalTime.now()");
        //calculateSize(timer2, "empty LocalTime");

        System.out.println("-------------------------------------------------");
    }

}
