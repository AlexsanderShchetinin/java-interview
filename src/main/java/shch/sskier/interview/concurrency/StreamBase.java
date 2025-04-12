package shch.sskier.interview.concurrency;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public abstract class StreamBase {

    // check run time
    static LocalTime timer = LocalTime.now();
    static LocalTime timer2;

    // iteration count
    static int iCount = 100_000;

    // create 2 collection ArrayList - нельзя использовать если есть несколько потоков!!!
    static List<ConcurrentExtension2Impl<Integer, Long>> list2 = new ArrayList<>();
    static List<ConcurrentExtension<Integer, String>> list1 = new ArrayList<>();

    static List<Integer> linkedList = new LinkedList<>();

    // применяем потокобезопасные коллекции!
    static List<ConcurrentExtension2Impl<Integer, Long>> synchronizedList2 = Collections.synchronizedList(new ArrayList<>());
    static List<ConcurrentExtension<Integer, String>> synchronizedList1 = Collections.synchronizedList(new ArrayList<>());


    // Через CopyOnWriteArrayList
    static List<ConcurrentExtension2Impl<Integer, Long>> concurrentList2 = new CopyOnWriteArrayList<>();
    static List<ConcurrentExtension<Integer, String>> concurrentList1 = new CopyOnWriteArrayList<>();


}
