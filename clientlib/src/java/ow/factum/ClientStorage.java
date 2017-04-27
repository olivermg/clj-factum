package ow.factum;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class ClientStorage {
    private IFn require;
    private Object cs;

    public ClientStorage() {
        this.require = Clojure.var("clojure.core", "require");

        require.invoke(Clojure.read("ow.factum.clientstorage"));
    }

    public void doStuff(Object transport) {
        IFn plus = Clojure.var("clojure.core", "+");
        Long res = (Long)plus.invoke(11, 22);

        IFn newcs = Clojure.var("ow.factum.clientstorage", "new-clientstorage");
        Object cs = newcs.invoke(transport);
        System.out.println(cs);

        System.out.println("doing stuff:" + res);
    }
}
