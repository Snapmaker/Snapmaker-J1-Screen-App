package fabscreen.platform.base.service.machine.structure;

import static fabscreen.platform.base.service.machine.Vector.B;
import static fabscreen.platform.base.service.machine.Vector.X;
import static fabscreen.platform.base.service.machine.Vector.X2;
import static fabscreen.platform.base.service.machine.Vector.Y;
import static fabscreen.platform.base.service.machine.Vector.Z;

import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.service.machine.Vector;

/**
 * Transform coordinateList data to vectorSet data, vice versa.
 */
public class StructureVectorMapper {
    public static Vector structureListToVector(List<CoordinateStructure> coordinateStructureList) {
        Vector vector = new Vector();
        for (CoordinateStructure c : coordinateStructureList) {
            switch (c.getAxis()) {
                case X:
                    vector.setX(c.getVector());
                    break;
                case Y:
                    vector.setY(c.getVector());
                    break;
                case Z:
                    vector.setZ(c.getVector());
                    break;
                case B:
                    vector.setB(c.getVector());
                    break;
                case X2:
                    vector.setX2(c.getVector());
                    break;
            }
        }
        return vector;
    }

    public static List<CoordinateStructure> vectorToStructureList(Vector vector) {

        ArrayList<CoordinateStructure> coordinateStructures = new ArrayList<>();
        if (vector.isxChange()) {
            coordinateStructures.add(new CoordinateStructure(X, vector.getX()));
        }
        if (vector.isyChange()) {
            coordinateStructures.add(new CoordinateStructure(Y, vector.getY()));
        }
        if (vector.iszChange()) {
            coordinateStructures.add(new CoordinateStructure(Z, vector.getZ()));
        }
        if (vector.isbChange()) {
            coordinateStructures.add(new CoordinateStructure(B, vector.getB()));
        }
        if (vector.isx2Change()) {
            coordinateStructures.add(new CoordinateStructure(X2, vector.getX2()));
        }
        return coordinateStructures;
    }
}
