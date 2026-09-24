package fabscreen.platform.base.service.machine;

import java.util.Objects;

public class Vector {
    // TODO: 2022/1/27 Not same with doc, same with hongyan
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public static final int B = 4;
    public static final int X2 = 6;
    private float x;
    private float y;
    private float z;
    private float b;
    private float x2;
    private boolean xChange;
    private boolean yChange;
    private boolean zChange;
    private boolean bChange;
    private boolean x2Change;

    public Vector() {
    }

    public Vector(float x, float y, float z, float b, float x2) {
        setX(x);
        setY(y);
        setZ(z);
        setB(b);
        setX2(x2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector vector = (Vector) o;
        return Double.compare(vector.x, x) == 0 && Double.compare(vector.y, y) == 0 && Double.compare(vector.z, z) == 0 && Double.compare(vector.b, b) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        xChange = true;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        yChange = true;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
        zChange = true;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
        bChange = true;
    }

    public float getX2() {
        return x2;
    }

    public void setX2(float b) {
        this.x2 = b;
        x2Change = true;
    }

    public boolean isxChange() {
        return xChange;
    }

    public boolean isyChange() {
        return yChange;
    }

    public boolean iszChange() {
        return zChange;
    }

    public boolean isbChange() {
        return bChange;
    }

    public boolean isx2Change() {
        return x2Change;
    }


    @Override
    public String toString() {
        return "Vector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", b=" + b +
                ", x2=" + x2 +
                ", xChange=" + xChange +
                ", yChange=" + yChange +
                ", zChange=" + zChange +
                ", bChange=" + bChange +
                ", x2Change=" + x2Change +
                '}';
    }

    public float getValueByAxis(int axis) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
            case B:
                return b;
            case X2:
                return x2;
            default:
                return -1;
        }
    }

    public void setValueByAxis(int axis, float value) {
        switch (axis) {
            case X:
                setX(value);
                break;
            case Y:
                setY(value);
                break;
            case Z:
                setZ(value);
                break;
            case B:
                setB(value);
                break;
            case X2:
                setX2(value);
                break;
            default:
        }

    }
}
