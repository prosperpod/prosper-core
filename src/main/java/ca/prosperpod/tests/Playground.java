package ca.prosperpod.tests;

public class Playground {

    public static void main(String[] args) {
        String test = "define canary \"orange thiqq bird\" swag trees";
        System.out.println(test.substring(test.lastIndexOf("\"") + 1));

        String string2 = test.substring(0, test.indexOf("\""));
        System.out.println(string2);
        System.out.println(string2.split(" ").length);

        String string3 = test.substring(test.lastIndexOf("\"") + 1);
        System.out.println(string3);
        System.out.println(string3.split(" ").length);
    }

}