# Методы Stream API


## concat
Используется для объединения двух потоков (Streams) в один.
Это позволяет комбинировать элементы из разных потоков в последовательный поток. 
Метод concat принимает два аргумента — два потока, и возвращает новый поток, содержащий все элементы из обоих потоков.

его реализация находится в интерфейсе ```Stream<T> extends BaseStream<T, Stream<T>>``` 
из пакета ```java.util.stream```

```java
public static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        @SuppressWarnings("unchecked")
        Spliterator<T> split = new Streams.ConcatSpliterator.OfRef<>(
                (Spliterator<T>) a.spliterator(), (Spliterator<T>) b.spliterator());
        Stream<T> stream = StreamSupport.stream(split, a.isParallel() || b.isParallel());
        return stream.onClose(Streams.composedClose(a, b));
    }
```

параметры метода принимают любые подпипы класса T, и сам метод возвращает объект родителя Т.
Сразу же нужно определить для себя, что:
- если у потомков - объектов a и b были специфичные только им переменные и методы, но их не было у родителя(суперкласса) T,
то эти переменные и методы исключаются. Например:
```java
class Animal {
    public void makeSound() {
        System.out.println("Animal sound");
    }
}

class Dog extends Animal {
    public void bark() {
        System.out.println("Woof");
    }
}

class Cat extends Animal {
    public void meow() {
        System.out.println("Meow");
    }
}
```
в результирующем потоке тут будет работать только метод *makeSound*, а методы *bark* и *meow* для Animal пропадут у результирующего потока
как обходное решение проблему можно решить через instanseof(): 
```java
combinedStream.forEach(animal -> {
    if (animal instanceof Dog) {
        ((Dog) animal).bark(); // Безопасное приведение типа
    } else if (animal instanceof Cat) {
        ((Cat) animal).meow(); // Безопасное приведение типа
    }
});
```
но если нам пригодились после конкатеинации методы предка, то нужно задать вопрос, правильное ли решение применияется
при использовании concat() ?

*Вернемся к реализации concat*
```java
public static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
    // Проверка на null. Если объекты a или b равны null, то выбрасывается NPE
    Objects.requireNonNull(a);
    Objects.requireNonNull(b);

    @SuppressWarnings("unchecked")  // показывает что при приведении к Spliterator<T> разработчик берет на себя ответственность, что объекты являются предками класса T
    // создается новый объект сплитератора, 
    Spliterator<T> split = new Streams.ConcatSpliterator.OfRef<>(
            (Spliterator<T>) a.spliterator(), (Spliterator<T>) b.spliterator());

    Stream<T> stream = StreamSupport.stream(split, a.isParallel() || b.isParallel());
    
    return stream.onClose(Streams.composedClose(a, b));
}
```
подробнее об интерфейсе [Spliterator](#Spliterator)











### Spliterator

**Объект для перемещения и разделения элементов источника.** 
Источником элементов, на которые распространяется Spliterator, 
может быть, например, массив, коллекция, канал ввода-вывода или функция генератора.

Spliterator может перемещаться по элементам по отдельности (tryAdvance()) или последовательно, 
массово (forEachRemaining()).
Spliterator может также выделять некоторые из своих элементов (используя trySplit) в качестве другого разделителя,
который будет использоваться в возможных параллельных операциях. 
Операции с использованием Spliterator, которые не могут выполнять разделение или делают это крайне несбалансированным 
или неэффективным образом, вряд ли выиграют от параллелизма.

Обход и разделение конечных элементов; каждый Spliterator полезен только для одного объемного вычисления.
Spliterator также сообщает набор characteristics() своей структуры, источника и элементов из числа
ORDERED (УПОРЯДОЧЕННЫХ), DISTINCT, SORTED, SIZED, NONNULL, IMMUTABLE(НЕИЗМЕНЯЕМЫХ), CONCURRENT, and SUBSIZED

Они могут использоваться клиентами Spliterator для управления, специализации или упрощения вычислений. 
Например, разделитель для Collection будет указывать SIZED, разделитель для Set будет указывать DISTINCT, 
а разделитель для SortedSet также будет указывать SORTED.
Характеристики отображаются в виде простого объединенного набора битов. 
Некоторые характеристики дополнительно ограничивают поведение метода; 
например,при ORDERED методы обхода должны соответствовать заданному порядку. 

**имеет методы:**
+ tryAdvance (попытка продвинуться дальше)

Если существует оставшийся элемент: выполняет над ним заданное действие, возвращая true; else возвращая false.
Если этот разделитель ORDERED, действие выполняется над следующим элементом в порядке совпадения.

Параметры: action – действие, операция которого выполняется не более одного раза.

Возвращается: false, если при входе в этот метод не существовало оставшихся элементов, в противном случае значение true.
Exception: NullPointerException – если action равно null
```java
boolean tryAdvance(Consumer<? super T> action);
```
+ forEachRemaining (для каждого оставшегося)

Выполняет заданное действие для каждого оставшегося элемента последовательно в текущем потоке, 
пока не будут обработаны все элементы или пока действие не вызовет исключение. 
Если этот разделитель ORDERED(УПОРЯДОЧЕН), действия выполняются в порядке встречаемости.

Параметры: action – Действие

Exception: NullPointerException – если указанное действие равно null

Требования к реализации: Реализация по умолчанию повторно вызывает tryAdvance, 
пока не вернет значение false. По возможности его следует переопределять.
```java
default void forEachRemaining(Consumer<? super T> action) {
    do { } while (tryAdvance(action));
}
```

+ trySplit (попытка разделить)

Если этот разделитель может быть секционирован, возвращает разделитель, охватывающий элементы, 
которые при возврате из этого метода не будут охвачены этим разделителем.
Если этот разделитель УПОРЯДОЧЕН, возвращаемый разделитель должен содержать строгий префикс элементов.
Если этот разделитель не охватывает бесконечное число элементов, повторные вызовы trySplit() 
должны в конечном итоге возвращать значение null. 
При ненулевом значении возвращаемого значения:
значение, указанное для estimateSize() перед разделением, после разделения должно быть больше или равно estimateSize() 
для этого и возвращаемого разделителя; 
и если этот разделитель имеет МЕНЬШИЙ РАЗМЕР, то estimateSize() для этого разделителя перед разделением 
должен быть равен сумме estimateSize() для этого и возвращаемого разделителя после разделения.

Этот метод может возвращать значение null по любой причине, включая пустоту, невозможность разделения после начала обхода, 
ограничения структуры данных и соображения эффективности.

Возвращается: разделитель, охватывающий некоторую часть элементов, или значение null, если этот разделитель не может быть разделен

Примечание к API: Идеальный метод trySplit эффективно (без обхода) делит свои элементы ровно пополам, 
обеспечивая сбалансированное параллельное вычисление. 
Многие отклонения от этого идеала остаются весьма эффективными; 
например, только приблизительное разделение приблизительно сбалансированного дерева или дерева, 
в котором конечные узлы могут содержать один или два элемента, не приводит к дальнейшему разделению этих узлов. 
Однако большие отклонения в балансе и/или чрезмерно неэффективная механика trySplit 
обычно приводят к плохой параллельной работе.
```java
Spliterator<T> trySplit();
```

+ estimateSize (приблизительный размер)

Возвращает оценку количества элементов, с которыми можно было бы столкнуться при forEachRemaining, 
или возвращает значение Long.MAX_VALUE , если оно бесконечно, неизвестно или слишком дорого для вычисления.

Если Spliterator SIZED и еще не был частично пройден или разделен, 
или Spliterator SUBSIZED и еще не был частично пройден, эта оценка должна представлять собой точное количество элементов, 
с которыми можно было бы столкнуться при полном обходе. 
В противном случае эта оценка может быть произвольно неточной, но должна уменьшаться, как указано при вызовах trySplit.

Возвращается: предполагаемый размер или Long.MAX_VALUE, если оно бесконечно, неизвестно или слишком дорого для вычисления.

Примечание к API: Даже неточная оценка часто бывает полезной и недорогой для вычисления. 
Например, вспомогательный разделитель приблизительно сбалансированного бинарного дерева может возвращать значение, 
которое оценивает количество элементов в половину от количества его родительского элемента; 
если корневой разделитель не поддерживает точный подсчет, он может оценить размер в степени двойки, 
соответствующей его максимальной глубине.
```java
long estimateSize();
```

+ getExactSizeIfKnown (получитm точный размер, если он известен)

Удобный метод, который возвращает estimateSize(), если этот разделитель имеет размер, иначе -1.

Возвращается: точный размер, если известен, иначе -1.

Требования к реализации: Реализация по умолчанию возвращает результат estimateSize(), 
если Spliterator сообщает характеристику SIZED, и -1 в противном случае.

```java
default long getExactSizeIfKnown() {
    return (characteristics() & SIZED) == 0 ? -1L : estimateSize();
}
```

+ int characteristics

Возвращает набор характеристик Spliterator и его элементов. 
Результат представлен в виде ORed переменных из ORDERED, DISTINCT, SORTED, SIZED, NONNULL, MMUTABLE, CONCURRENT, SUBSIZED. 
Повторные вызовы функции characteristics() для данного разделителя, до или в промежутках между вызовами функции try Split, 
всегда должны возвращать один и тот же результат.

Если Spliterator сообщает о несогласованном наборе характеристик 
(либо возвращаемых при одном вызове, либо при нескольких вызовах), 
нельзя гарантировать никаких вычислений с использованием этого Spliterator.

Возвращается: представление характеристик

Примечание к API: Характеристики данного разделителя до разделения могут отличаться от характеристик после разделения. 
Для конкретных примеров смотрите значения параметров SIZED, SUBSIZED и CONCURRENT.
```java
int characteristics();
```

+ hasCharacteristics

  Возвращает значение true, если характеристики этого разделителя содержат все заданные характеристики.

  Параметры: characteristics – характеристики, которые необходимо проверить

  Возвращается: значение true, если присутствуют все указанные характеристики, в противном случае значение false

  Требования к реализации: Реализация по умолчанию возвращает значение true, 
если заданы соответствующие разряды заданных характеристик.

```java
default boolean hasCharacteristics(int characteristics) {
    return (characteristics() & characteristics) == characteristics;
}
```

+ getComparator

Если источник этого разделителя SORTED при помощи Comparator, возвращает этот Comparator. 
Если источник SORTED в естественном порядке, возвращает null. 
В противном случае, если источник не ОТСОРТИРОВАН, генерируется исключение IllegalStateException.

Возвращается: компаратор или null, если элементы отсортированы в естественном порядке.

Бросает: IllegalStateException – если средство разделения не сообщает о характеристике SORTED.

Требования к реализации!: Реализация по умолчанию всегда вызывает исключение IllegalStateException.
```java
default Comparator<? super T> getComparator() {
    throw new IllegalStateException();
}
```



Пример. Вот класс (не очень полезный, за исключением иллюстрации), который поддерживает массив, 
в котором фактические данные хранятся в четных местах, а несвязанные данные тегов хранятся в нечетных местах. 
Его Spliterator игнорирует теги.
```java
class TaggedArray<T> {   
    private final Object[] elements; // неизменяемый после создания
       
    TaggedArray(T[] data, Object[] tags) { 
        int size = data.length;     
        if (tags.length != size) throw new IllegalArgumentException();     
        this.elements = new Object[2 * size];     
        for (int i = 0, j = 0; i < size; ++i) {       
            elements[j++] = data[i];       
            elements[j++] = tags[i];     
        }   
    }    
    // пытаемся разделить источник, 
    public Spliterator<T> spliterator() {     
        return new TaggedArraySpliterator<>(elements, 0, elements.length);   
    }    
    
    static class TaggedArraySpliterator<T> implements Spliterator<T> {     
        private final Object[] array;     
        private int origin;  // current index, advanced on split or traversal     
        private final int fence; // one past the greatest index      
        
        TaggedArraySpliterator(Object[] array, int origin, int fence) {       
            this.array = array; 
            this.origin = origin; 
            this.fence = fence;     
        }  
        
        public void forEachRemaining(Consumer<? super T> action) {       
            for (; origin < fence; origin += 2) 
                action.accept((T) array[origin]);     
        }      
        
        public boolean tryAdvance(Consumer<? super T> action) {       
            if (origin < fence) {         
                action.accept((T) array[origin]);         
                origin += 2;         
                return true;       
            }       else // cannot advance         
                return false;     
        }      
        
        public Spliterator<T> trySplit() {       
            int lo = origin; // divide range in half       
            int mid = ((lo + fence) >>> 1) & ~1; // force midpoint to be even       
            if (lo < mid) { // split out left half         
                origin = mid; // reset this Spliterator's origin         
                return new TaggedArraySpliterator<>(array, lo, mid);       
            }       else       // too small to split         
                return null;     
        }      
        
        public long estimateSize() {       
            return (long)((fence - origin) / 2);     
        }      
        
        public int characteristics() {       
            return ORDERED | SIZED | IMMUTABLE | SUBSIZED;     
        }   
    } 
}
```

В качестве примера можно привести фреймворк параллельных вычислений, такой как java. util. пакет stream, 
который будет использовать Spliterator в параллельных вычислениях, 
вот один из способов реализации связанного параллельного forEach, 
который иллюстрирует основную идиому использования разделения подзадач до тех пор, 
пока предполагаемый объем работы не станет достаточно небольшим для последовательного выполнения. 
Здесь мы предполагаем, что порядок обработки подзадач не имеет значения; 
различные (разветвленные) задачи могут дополнительно разделять и обрабатывать элементы одновременно в неопределенном порядке. 
В этом примере используется java.util.concurrent.CountedCompleter; 
аналогичные правила применимы и к другим конструкциям параллельных задач.
```java
static <T> void parEach(TaggedArray<T> a, Consumer<T> action) {   
    Spliterator<T> s = a.spliterator();   
    long targetBatchSize = s.estimateSize() / (ForkJoinPool. getCommonPoolParallelism() * 8);   
    new ParEach(null, s, action, targetBatchSize).invoke(); 
}  

static class ParEach<T> extends CountedCompleter<Void> {   
    final Spliterator<T> spliterator;   
    final Consumer<T> action;   
    final long targetBatchSize;    
    
    ParEach(ParEach<T> parent, Spliterator<T> spliterator,           
            Consumer<T> action, long targetBatchSize) {     
        super(parent);     
        this.spliterator = spliterator; 
        this.action = action;     
        this.targetBatchSize = targetBatchSize;   
    }    
    
    public void compute() {     
        Spliterator<T> sub;     
        while (spliterator. estimateSize() > targetBatchSize &&            
                (sub = spliterator. trySplit()) != null) {       
            addToPendingCount(1);       
            new ParEach<>(this, sub, action, targetBatchSize).fork();     
        }     
        spliterator. forEachRemaining(action);     
        propagateCompletion();   
    } 
}
```