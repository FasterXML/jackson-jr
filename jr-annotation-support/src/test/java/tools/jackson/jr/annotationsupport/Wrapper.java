package tools.jackson.jr.annotationsupport;

import java.util.Objects;

public final class Wrapper {
    TestClasses.Cow cow;
    String farmerName;

    public TestClasses.Cow getCow() {
        return cow;
    }

    public void setCow(TestClasses.Cow cow) {
        this.cow = cow;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Wrapper wrapper)) {
            return false;
        }
        return Objects.equals(cow, wrapper.cow) && Objects.equals(farmerName, wrapper.farmerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cow, farmerName);
    }

    @Override
    public String toString() {
        return "Wrapper{" +
                "cow=" + cow +
                ", farmerName='" + farmerName + '\'' +
                '}';
    }
}
