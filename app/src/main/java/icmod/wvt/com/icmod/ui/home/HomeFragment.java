package icmod.wvt.com.icmod.ui.home;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import icmod.wvt.com.icmod.R;
import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.FinalValuable;
import icmod.wvt.com.icmod.others.MOD;
import icmod.wvt.com.icmod.ui.MainActivity;
import icmod.wvt.com.icmod.ui.home.tab.MAPFragment;
import icmod.wvt.com.icmod.ui.home.tab.MODFragment;
import icmod.wvt.com.icmod.ui.home.tab.ResFragment;

import static icmod.wvt.com.icmod.ui.MainActivity.print;

public class HomeFragment extends Fragment {

    //控件定义部分
    TabLayout tabLayout;
    List<Fragment> fragments = new ArrayList<>();
    ViewPager viewPager;
    //其他定义
    MainActivity mainActivity;

    //数据定义
    List<String> titles = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.home_tablayout);
        viewPager = view.findViewById(R.id.home_viewPager);
        mainActivity = (MainActivity) getActivity();

        MainActivity.getFab().setVisibility(View.VISIBLE);

        fragments.add(new MODFragment());
        fragments.add(new MAPFragment(FinalValuable.ICMAP));
        fragments.add(new MAPFragment(FinalValuable.MCMAP));
        fragments.add(new ResFragment());

        titles.add("MOD");
        titles.add("IC地图");
        titles.add("MC地图");
        titles.add("IC材质");

        viewPager.setAdapter(new FragmentPagerAdapterCompat(getChildFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                super.destroyItem(container, position, object);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return titles.get(position);
            }
        });
//        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                FragmentPagerAdapterCompat fragmentPagerAdapterCompat2 = (FragmentPagerAdapterCompat) viewPager.getAdapter();
//                switch (position) {
//                    case 0:
//                        MODFragment modFragment = (MODFragment) fragmentPagerAdapterCompat2.getFragment(position);
//                        modFragment.flashList(false);
//                        break;
//                    case 1:
//                        break;
//                    case 2:
//                        break;
//                    case 3:
//                        break;
//                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
    }

    public abstract class FragmentPagerAdapterCompat extends FragmentPagerAdapter {

        private SparseArray<Fragment> fragments;

        public FragmentPagerAdapterCompat(FragmentManager fm) {
            super(fm);
            fragments = new SparseArray<>(getCount());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            fragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            fragments.remove(position);
        }



        public Fragment getFragment(int position) {
            return fragments.get(position);
        }

    }

}