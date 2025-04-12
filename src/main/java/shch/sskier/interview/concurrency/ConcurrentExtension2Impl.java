package shch.sskier.interview.concurrency;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Setter
@Getter
@ToString
public class ConcurrentExtension2Impl<T, N> extends BaseConcurrent<T>{

    private N extensionParam;

    public ConcurrentExtension2Impl(N extensionParam) {
        this.extensionParam = extensionParam;
    }

    public ConcurrentExtension2Impl() {

    }


    public List<N> getCopyExtensionParam(short count){
        List<N> nList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < random.nextInt(1, Math.abs(count + 2)) ; i++) {
            nList.add(extensionParam);
        }
        return nList;
    }

}
