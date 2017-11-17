package ca.prosperpod.core;

public class Pod {

    private String identifier;

    public Pod(Integer podNum){
        this.identifier = "pod-" + podNum.toString();
    }

}
