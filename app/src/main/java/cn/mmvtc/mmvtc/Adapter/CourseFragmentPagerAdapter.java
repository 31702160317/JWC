package cn.mmvtc.mmvtc.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by W on 2019/6/23.
 */

public class CourseFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments;
    public CourseFragmentPagerAdapter(FragmentManager fm, List<Fragment> list){
        super(fm);
        mFragments = list;
    }


    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }


}
