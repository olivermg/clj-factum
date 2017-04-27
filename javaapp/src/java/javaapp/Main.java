package javaapp;

import ow.factum.transport.Embedded;
import ow.factum.ClientStorage;

public class Main {
    public static void main(String[] args) {
        Object transport = Embedded.create();
        ClientStorage cs = new ClientStorage();
        cs.doStuff(transport);

        System.out.println("foobar");
    }
}
