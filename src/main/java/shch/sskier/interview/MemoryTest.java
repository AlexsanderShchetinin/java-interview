package shch.sskier.interview;

import org.openjdk.jol.info.GraphLayout;
import shch.sskier.interview.concurrency.StreamApiConcurrentTrainer;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class MemoryTest {


    public static void main(String[] args) {
        var streamApiConcurrentTrainer = new StreamApiConcurrentTrainer();

        streamApiConcurrentTrainer.runTrainer();

        // Получение информации о текущем использовании памяти
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        System.out.println("Heap Memory after runTrainer Usage: " + heapMemoryUsage);

        // Память, занимаемая объектом
        //System.out.println("Size of myObject: " + getObjectSize(myObject) + " bytes");
    }


}
