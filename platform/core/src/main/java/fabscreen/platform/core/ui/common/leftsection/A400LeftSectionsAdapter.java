package fabscreen.platform.core.ui.common.leftsection;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

import fabscreen.platform.core.ui.view.leftsection.A400LeftSectionsView;

public class A400LeftSectionsAdapter extends LeftSectionsAdapter {
    public A400LeftSectionsAdapter(@NonNull List<SectionItem> sectionItems) {
        super(sectionItems);
    }

    @Override
    protected View getView(ViewGroup parent) {
        return new A400LeftSectionsView(parent.getContext());
    }
}
