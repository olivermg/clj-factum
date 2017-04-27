package ow.factum.transport;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class Embedded {
    public static Object create() {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("ow.factum.transport.embedded"));

        IFn newFn = Clojure.var("ow.factum.transport.embedded", "new-embeddedtransport");

        return newFn.invoke();
    }
}
