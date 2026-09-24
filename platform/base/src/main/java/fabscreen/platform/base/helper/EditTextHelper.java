package fabscreen.platform.base.helper;

import android.text.Editable;

public class EditTextHelper {
    static public Editable fixNumberInputSinglePoint(Editable e) {
        if (!e.toString().isEmpty() && e.toString().equals(".")) {
            e.replace(0, e.length(), "0.");
        }
        return e;
    }
}
