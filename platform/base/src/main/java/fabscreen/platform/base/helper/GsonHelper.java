package fabscreen.platform.base.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.entity.Module;

public class GsonHelper {
    public static final String A150_1_6W = "A150_1_6w";
    public static final String A250_1_6W = "A250_1_6w";
    public static final String A350_1_6W = "A350_1_6w";
    public static final String A400_1_6W = "A400_1_6w";
    public static final String A150_10W = "A150_10w";
    public static final String A250_10W = "A250_10w";
    public static final String A350_10W = "A350_10w";
    public static final String A400_10W = "A400_10w";


    public Vector getVectorByModuleID(String str, int model, int headType) {
        Map<String, Vector> map = StringToCameraCalibrationTakePhotoVector(str);
        if (headType == Module.ModuleType.HEAD_LASER) {
            switch (model) {
                case IMachine.MachineModel.A150:
                    return map.get(A150_1_6W);
                case IMachine.MachineModel.A250:
                    return map.get(A250_1_6W);
                case IMachine.MachineModel.A350:
                    return map.get(A350_1_6W);
                case IMachine.MachineModel.A400:
                    return map.get(A400_1_6W);
                default:
                    return null;
            }
        } else if (headType == Module.ModuleType.HEAD_LASER_10W) {
            switch (model) {
                case IMachine.MachineModel.A150:
                    return map.get(A150_10W);
                case IMachine.MachineModel.A250:
                    return map.get(A250_10W);
                case IMachine.MachineModel.A350:
                    return map.get(A350_10W);
                case IMachine.MachineModel.A400:
                    return map.get(A400_10W);
                default:
                    return null;
            }
        } else {
            return null;
        }
    }


    public Map<String, Vector> StringToCameraCalibrationTakePhotoVector(String str) {
        if (str == null) {
            HashMap<String, Vector> vectorHashMap = new HashMap<>();
            vectorHashMap.put(A150_1_6W, getVector(167f / 2 - Constants.LASER_CAMERA_OFFSET_X, 165f / 2 - Constants.LASER_CAMERA_OFFSET_Y, 140));
            vectorHashMap.put(A250_1_6W, getVector(252f / 2 - Constants.LASER_CAMERA_OFFSET_X, 260f / 2 - Constants.LASER_CAMERA_OFFSET_Y, 170));
            vectorHashMap.put(A350_1_6W, getVector(345f / 2 - Constants.LASER_CAMERA_OFFSET_X, 357f / 2 - Constants.LASER_CAMERA_OFFSET_Y, 170));
            vectorHashMap.put(A400_1_6W, getVector(410f / 2 - Constants.LASER_10W_CAMERA_OFFSET_X, 410f / 2 - Constants.LASER_10W_CAMERA_OFFSET_Y, 170));
            vectorHashMap.put(A150_10W, getVector(155, 82, 150));
            vectorHashMap.put(A250_10W, getVector(186, 130, 230));
            vectorHashMap.put(A350_10W, getVector(232, 178, 290));
            vectorHashMap.put(A400_10W, getVector(265, 205, 330));
            return vectorHashMap;
        }
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type = new TypeToken<HashMap<String, Vector>>() {
        }.getType();
        return gson.fromJson(str, type);
    }

    public String CameraCalibrationTakePhotoVectorToString(Map<String, Vector> vectorMap) {
        return new GsonBuilder().enableComplexMapKeySerialization().create().toJson(vectorMap);
    }

    private Vector getVector(float positionX, float positionY, float positionZ) {
        Vector vector = new Vector();
        vector.setX(positionX);
        vector.setY(positionY);
        vector.setZ(positionZ);
        return vector;
    }
}
