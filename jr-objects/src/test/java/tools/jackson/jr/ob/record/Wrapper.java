package tools.jackson.jr.ob.record;

import java.util.Objects;

public final class Wrapper {
    Java17RecordTest.Cow cow;
    String farmerName;

    public Java17RecordTest.Cow getCow() {
        return cow;
    }

    public void setCow(Java17RecordTest.Cow cow) {
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
