package shch.sskier.interview.concurrency;

import lombok.ToString;
import shch.sskier.interview.MemoryTest;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * @author Alexander Shchetinin 11.04.2025
 */

@ToString
public class StreamApiConcurrentTrainer extends StreamBase {

    public void runTrainer() {
        // Получение информации о текущем использовании памяти
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        System.out.println("Heap Memory before runTrainer Usage: " + heapMemoryUsage);


        auditMemoryOurClass(); // посмотрим сколько памяти занимают пустые коллекции и переменные в StreamBase
        calculateSize(concurrentList1, "empty concurrentList1 in class StreamApiConcurrentTrainer");

        ConcurrentExtension<Integer, String> extension = new ConcurrentExtension<>("extensionParam");
        extension.setExtensionParam(extension.getExtensionParam());
        extension.setNameStream("Main thread by testing memory ");
        extension.setElement(999);
        extension.setNumStream(999);
        concurrentList1.add(extension);
        calculateSize(concurrentList1, "concurrentList1 with one element ConcurrentExtension");

        arrayListInteger.add(iCount);
        calculateSize(arrayListInteger, "arrayListInteger with one Integer object");

        // Create first thread
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                /*try {
                        Thread.sleep(1); // Задержка в 1 мс чтобы второй поток взял коллекцию первым
                    } catch (InterruptedException e) {
                        System.out.println("Tread_1 interrupted.");
                    }*/
                LocalTime timer = LocalTime.now();
                LocalTime timer2;
                int randomInt = new Random().nextInt();
                for (int i = 0; i < iCount; i++) {
                    ConcurrentExtension<Integer, String> extension = new ConcurrentExtension<>("extensionParam");
                    if (i == 0) calculateSize(extension, " empty ConcurrentExtension"); // проверяем размер объекта
                    extension.setExtensionParam(extension.getExtensionParam() + i);
                    extension.setNameStream("Collection1 Thread1  ");
                    extension.setElement(randomInt + i);
                    extension.setNumStream(i + 1);
                    if (i == 0) calculateSize(extension, " full ConcurrentExtension"); // проверяем размер объекта
                    concurrentList1.add(extension);
                    if (i == 0) calculateSize(concurrentList1, " concurrentList1 with one obj");
                    if (i == iCount - 1) calculateSize(concurrentList1, " concurrentList1 with " + iCount + " obj");


                    // попытка добавить из первого потока данные в коллекцию №2
                    ConcurrentExtension2Impl<Integer, Long> extension2 = new ConcurrentExtension2Impl<>();
                    if (i == 0) calculateSize(extension2, "empty ConcurrentExtension2Impl");
                    extension2.setExtensionParam(Integer.toUnsignedLong(i));
                    extension2.setNameStream("Collection2 Thread1 ");
                    extension2.setElement(randomInt + i);
                    extension2.setNumStream(i + 1);
                    if (i == 0) calculateSize(extension2, "full ConcurrentExtension2Impl");
                    concurrentList2.add(extension2);
                    if (i == 0) calculateSize(concurrentList2, " concurrentList2 with one obj");
                    if (i == iCount - 1) calculateSize(concurrentList2, " concurrentList2 with " + iCount + " obj");

                    // попытка посмотреть записи из второй коллекции, так как 2ой поток уже должен их добавить
                    // получить по индексу
                    // посмотреть полный размер списка (пару раз)
                    if (i % (iCount) == 0) {
                        concurrentList2.get(i).readStream();
                        System.out.println("Текущий размер Collection2 = " + concurrentList2.size());
                    }
                }

                timer2 = LocalTime.now();
                Duration dur = Duration.between(timer, timer2);
                System.out.println("Поток1 выполнился за " + dur.getSeconds() + " сек, " + (dur.getNano() / 1000) + " микросекунд");
            }
        });

        // Create second thread
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                LocalTime timer = LocalTime.now();
                LocalTime timer2;
                int randomInt = new Random().nextInt();
                for (int i = 0; i < iCount; i++) {
                    ConcurrentExtension2Impl<Integer, Long> extension2 = new ConcurrentExtension2Impl<>(Integer.toUnsignedLong(i));
                    extension2.setExtensionParam(Integer.toUnsignedLong(i));
                    extension2.setNameStream("Collection2 Thread2");
                    extension2.setElement(randomInt + i);
                    extension2.setNumStream(i + 1);
                    concurrentList2.add(extension2);

                    // добавляем в первую коллекцию из 2го потока
                    ConcurrentExtension<Integer, String> extension = new ConcurrentExtension<>("extensionParam");
                    extension.setExtensionParam(extension.getExtensionParam() + i);
                    extension.setNameStream("Collection1 Thread2  ");
                    extension.setElement(randomInt + i);
                    extension.setNumStream(i + 1);
                    concurrentList1.add(extension);

                }
                timer2 = LocalTime.now();
                Duration dur = Duration.between(timer, timer2);
                System.out.println("Поток2 выполнился за " + dur.getSeconds() + " сек, " + (dur.getNano() / 1000) + " микросекунд");
            }
        });

        //start threads
        thread1.start();
        thread2.start();

        try {
            Thread.sleep(5000); // Задержка в 5 с
            // Получение информации о текущем использовании памяти
            MemoryUsage heapMemoryUsage2 = memoryMXBean.getHeapMemoryUsage();
            System.out.println("Heap Memory during runTrainer Usage: " + heapMemoryUsage2);
        } catch (InterruptedException e) {
            System.out.println("Tread_1 interrupted.");
        }

        // wait finished threads
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        timer2 = LocalTime.now();

        // вывод в консоль каждого потока по отдельности
        /*for (ConcurrentExtension<Integer, String> extension : concurrentList1) {
            extension.readStream();
        }
        for (ConcurrentExtension2Impl<Integer, Long> extension2 : concurrentList2) {
            extension2.readStream();
        }*/

        Stream<BaseConcurrent<Integer>> concatCollection = Stream.concat(concurrentList2.stream(), concurrentList1.stream());
        // вывод в консоль общей коллекции
        /*for (BaseConcurrent<Integer> baseConcurrent : concatCollection.toList()) {
            baseConcurrent.readStream();
        }*/

        Duration dur = Duration.between(timer, timer2);
        System.out.println("Время выполнения " + dur.getSeconds() + " сек, " + (dur.getNano() / 1000) + " микросекунд");
    }

    /**
     * Использование ArrayList в качестве коллекции для хранения данных:(число элементов в каждой коллекции всегда 150_000)
     * ДОБАВЛЕНИЕ ЭЛЕМЕНТОВ
     * 1. параллельное выполнение 2 потоков с отдельной коллекцией в каждом = 0 сек, 084551 микросекунд
     * 2. последовательное выполнение 1 потока с циклом на 150_000 повторов,
     * но в каждом цикле заполняем сразу две коллекции, т.о. получаем 300_000 элементов = 0 сек, 090593 микросекунд
     * 3. последовательное выполнение 1 потока с двумя циклами на 150_000 повторов,
     * при этом каждый цикл заполняет свою коллекцию = 0 сек, 090037 микросекунд (идентично пункту выше)
     *
     * 4. параллельно-последовательное выполнение 3 потоков, (или 2 потока каждый заполняют обе коллекции)
     * 75_000 заполняется в одном потоке, и остальные 75_000 заполняются в основном потоке = 0 сек, 075569 микросекунд
     * что быстрее, всех пунктов выше, так как тут уже работают 3 потока.
     *
     * НО!!!!! При этом если используем обычный ArrayList для хранения коллекции, то потоки конкурируют между собой
     * и в 50% получаем NPE, из-за того, что сразу 2 потока пытаются добавить элемент в одну и ту же коллекцию.
     *
     * ПРИМЕР:
     * Collection2 Thread2 has 27125 number yours stream! And has -864017957 element with type=java.lang.Integer
     * Collection2 MainThread has 4881 number yours stream! And has 466632511 element with type=java.lang.Integer
     * ТУТ ДАЖЕ ПРОПАЛ ЭЛЕМЕНТ С НОМЕРОМ 27126 !!!!! И ТАКОЕ ЕСЛИ ПОСМОТРЕТЬ КОНСОЛЬ ЧАСТО ПРОИСХОДИТ !!!
     * Collection2 Thread2 has 27127 number yours stream! And has 1546965874 element with type=java.lang.Integer
     * Collection2 Thread2 has 27128 number yours stream! And has -1042938903 element with type=java.lang.Integer
     * Collection2 Thread2 has 27129 number yours stream! And has 666744147 element with type=java.lang.Integer
     * И ПОЛУЧИЛИ NPE
     *  Exception in thread "main" java.lang.NullPointerException: Cannot invoke "shch.sskier.interview.concurrency.BaseConcurrent.readStream()" because "baseConcurrent" is null
     *
     * Использование потокобезопасной обертки Collections.synchronizedList(new ArrayList<>()) спасает ситуацию
     * 5. с использованием Collections.synchronizedList аналогично сценарию из п.4 = 0 сек, 83563 микросекунд
     *
     */


}
