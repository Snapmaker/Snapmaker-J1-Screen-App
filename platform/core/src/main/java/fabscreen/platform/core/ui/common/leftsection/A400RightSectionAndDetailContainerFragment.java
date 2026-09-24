package fabscreen.platform.core.ui.common.leftsection;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.common.VerticalSpaceItemDecoration;

public abstract class A400RightSectionAndDetailContainerFragment extends BaseFragment {

    @BindView(R2.id.rv_sections)
    protected RecyclerView mRvSections;

    private A400RightSectionsAdapter mAdapter;
    protected final List<SectionItem> mSectionItems = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initContainerView();
    }

    private void initContainerView() {
        setTitle(getTitle());
        if (mSectionItems.size() > 0) {
            mSectionItems.clear();
        }

        mSectionItems.addAll(getLeftSections());

        mRvSections.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = getSectionsAdapter(mSectionItems);
        mRvSections.setAdapter(mAdapter);
        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration(DimensUtils.dp2px(12));
        mRvSections.addItemDecoration(decoration);
        mAdapter.setOnSectionSelectedListener(this::onSectionSelected);
        if (mSectionItems.size() == 0) {
            return;
        }
        setSelection(getDefaultSelection());
    }

    /**
     * Override this method to set give a default detail fragment to show, use the first fragment if not override.
     */
    protected int getDefaultSelection() {
        return 0;
    }

    protected void onSectionSelected(int position, boolean isUserClick) {
        if (isUserClick) {
            playNormalClickSound();
        }
        Fragment fragment = mSectionItems.get(position).fragment;
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fcv_detail, fragment).commit();
    }

    protected void setSelection(int selection) {
        mAdapter.setSelection(selection);
    }

    @Override
    protected abstract int getLayoutResID();

    protected abstract List<SectionItem> getLeftSections();

    protected abstract A400RightSectionsAdapter getSectionsAdapter(List<SectionItem> sectionItems);

    protected abstract String getTitle();

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selection", mAdapter.getSelection());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            setSelection(savedInstanceState.getInt("selection", 0));
        }
    }
}
