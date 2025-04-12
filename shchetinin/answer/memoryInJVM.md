
### Получение информации о текущем состоянии памяти кучи

```java
public static void main(String[] args) {
    // Получение информации о текущем использовании памяти
    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();

    System.out.println("Heap Memory Usage: " + heapMemoryUsage);
}
```
