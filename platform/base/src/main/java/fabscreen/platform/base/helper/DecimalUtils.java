package fabscreen.platform.base.helper;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class DecimalUtils {
    public static String getNoMoreThan1DigitsFloor(float number) {
        DecimalFormat format = new DecimalFormat("0.#");
        format.setRoundingMode(RoundingMode.FLOOR);
        return format.format(number);
    }
}
