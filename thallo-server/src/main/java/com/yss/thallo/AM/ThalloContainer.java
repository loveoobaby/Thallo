package com.yss.thallo.AM;

import java.util.List;
import java.util.Objects;

public class ThalloContainer {

    private String imageName;
    private String imageTag;
    private int memeory;
    private int vcores;
    private List<String> bindPorts;

    public ThalloContainer(){

    }

    public ThalloContainer(int memeory, int vcores){
        this.memeory = memeory;
        this.vcores = vcores;
    }

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getMemeory() {
        return memeory;
    }

    public void setMemeory(int memeory) {
        this.memeory = memeory;
    }

    public int getVcores() {
        return vcores;
    }

    public void setVcores(int vcores) {
        this.vcores = vcores;
    }

    public List<String> getBindPorts() {
        return bindPorts;
    }

    public void setBindPorts(List<String> bindPorts) {
        this.bindPorts = bindPorts;
    }

    @Override
    public String toString() {
        return "DockerContainer{" +
                "imageName='" + imageName + '\'' +
                ", memeory=" + memeory +
                ", vcores=" + vcores +
                ", bindPorts=" + bindPorts +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThalloContainer that = (ThalloContainer) o;
        return memeory == that.memeory &&
                vcores == that.vcores;
    }

    @Override
    public int hashCode() {

        return Objects.hash(memeory, vcores);
    }
}
