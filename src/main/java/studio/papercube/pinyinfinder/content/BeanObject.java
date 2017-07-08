package studio.papercube.pinyinfinder.content;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;

public class BeanObject<T> implements Serializable {
    protected BeanObject() {
    }

    public String toJsonString() {
        return new Gson().toJson(this,
                new TypeToken<T>() {
                    //nothing
                }.getType());
    }
}
