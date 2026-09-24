package fabscreen.features.machinetools.control.j1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.a400.A400CNCControlFragment;
import fabscreen.features.machinetools.control.a400.A400DryBoxControlFragment;
import fabscreen.features.machinetools.control.a400.A400LaserControlFragment;
import fabscreen.features.machinetools.control.a400.A400EnclosureControlFragment;
import fabscreen.features.machinetools.control.a400.A400AirPurifierControlFragment;
import fabscreen.features.machinetools.control.common.S30FilamentControlFragment;
import fabscreen.features.machinetools.control.common.S30HeatedBedControlFragment;
import fabscreen.platform.base.view.BaseFragment;

public class J1ControlSectionsFragment extends BaseFragment {

    @BindView(R2.id.rv_control_sections)
    RecyclerView mRvSectionList;

    private ArrayList<SectionItem> mSectionItems = new ArrayList<>();
    private J1ControlSectionsViewModel mViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        mSectionItems.addAll(mViewModel.getControlSections());
        onItemClicked(mViewModel.getControlSections().get(0).id);
        SectionListAdapter adapter = new SectionListAdapter(mSectionItems);
        adapter.setOnItemClickListener(this::onItemClicked);
        mRvSectionList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRvSectionList.setHasFixedSize(true);
        mRvSectionList.setItemAnimator(null);
        mRvSectionList.setAdapter(adapter);
    }

    private void onItemClicked(@SectionItem.SectionId int id) {
        Fragment fragment = null;
        switch (id) {
            case SectionItem.JOG:
                fragment = J1JogControlFragment.newInstance();
                break;
            case SectionItem.FILAMENT:
                fragment = S30FilamentControlFragment.newInstance();
                break;
            case SectionItem.HEATED_BED:
                fragment = S30HeatedBedControlFragment.newInstance();
                break;
            case SectionItem.ENCLOSURE:
                fragment = A400EnclosureControlFragment.newInstance();
                break;
            case SectionItem.AIR_PURIFIER:
                fragment = A400AirPurifierControlFragment.newInstance();
                break;
            case SectionItem.ELECTRIC_MACHINE:
                fragment = J1MotorControlFragment.newInstance();
                break;
            case SectionItem.LASER:
                fragment = A400LaserControlFragment.newInstance();
                break;
            case SectionItem.CNC:
                fragment = A400CNCControlFragment.newInstance();
                break;
            case SectionItem.DRIER:
                fragment = A400DryBoxControlFragment.newInstance();
                break;

        }
        if (fragment == null) return;
        showDetailFragment(fragment);
    }

    private void showDetailFragment(Fragment fragment) {
        if (getParentFragment() instanceof J1ControlFragment) {
            ((J1ControlFragment) getParentFragment()).showControlDetail(fragment);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_control_sections;
    }

    @Override
    protected J1ControlSectionsViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(J1ControlSectionsViewModel.class);
    }

    static class SectionListAdapter extends RecyclerView.Adapter<SectionListAdapter.ViewHolder> {
        private ArrayList<SectionItem> mCategories;
        private OnItemClickListener mListener;

        public SectionListAdapter(@NonNull ArrayList<SectionItem> categories) {
            mCategories = categories;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_control_category, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SectionItem category = mCategories.get(position);
            holder.mTvTitle.setText(category.title);
            holder.itemView.setSelected(category.selected);
            holder.itemView.setOnClickListener(v -> {
                if (mListener == null) return;
                onNewItemSelected(position);
            });
        }

        private void onNewItemSelected(int position) {
            SectionItem clickedItem = mCategories.get(position);
            if (clickedItem.selected) {
                // do nothing since already selected.
            } else {
                for (SectionItem sectionItem : mCategories) {
                    sectionItem.selected = false;
                }
                clickedItem.selected = true;
            }

            notifyItemRangeChanged(0, mCategories.size());

            mListener.onSectionSelected(clickedItem.id);
        }

        @Override
        public int getItemCount() {
            return mCategories.size();
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            mListener = listener;
        }

        interface OnItemClickListener {
            void onSectionSelected(@SectionItem.SectionId int sectionId);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R2.id.tv_title)
            TextView mTvTitle;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    static class SectionItem {

        public static final int JOG = 0;
        public static final int FILAMENT = 1;
        public static final int HEATED_BED = 2;
        public static final int ENCLOSURE = 3;
        public static final int AIR_PURIFIER = 4;
        public static final int ELECTRIC_MACHINE = 5;
        public static final int LASER = 6;
        public static final int CNC = 7;
        public static final int DRIER = 8;

        public int id;
        public String title;
        public boolean selected;

        public SectionItem(@SectionId int sectionId, String title) {
            this.id = sectionId;
            this.title = title;
        }

        public SectionItem(@SectionId int sectionId, String title, boolean selected) {
            this.id = sectionId;
            this.title = title;
            this.selected = selected;
        }

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({JOG, FILAMENT, HEATED_BED, ENCLOSURE, AIR_PURIFIER, ELECTRIC_MACHINE, LASER, CNC, DRIER})
        public @interface SectionId {
        }
    }
}
