package fabscreen.platform.core.ui.common.leftsection;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

import fabscreen.platform.core.ui.view.leftsection.J1LeftSectionView;

public class J1LeftSectionsAdapter extends LeftSectionsAdapter {
    public J1LeftSectionsAdapter(@NonNull List<SectionItem> sectionItems) {
        super(sectionItems);
    }

    @Override
    protected View getView(ViewGroup parent) {
        return new J1LeftSectionView(parent.getContext());
    }
}
