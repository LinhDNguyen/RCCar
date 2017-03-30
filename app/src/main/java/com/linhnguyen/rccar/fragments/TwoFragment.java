package com.linhnguyen.rccar.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.linhnguyen.rccar.R;
import com.linhnguyen.rccar.core.CarSoundViewAdapter;
import com.linhnguyen.rccar.core.ImageItem;
import com.linhnguyen.rccar.core.ResourceUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class TwoFragment extends Fragment{
    private GridView gridView;
    private CarSoundViewAdapter gridAdapter;

    public TwoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_two, container, false);

        gridView = (GridView) view.findViewById(R.id.car_sound_grid);
        gridAdapter = new CarSoundViewAdapter(getActivity(), R.layout.sound_item_layout, getData());
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new GridView.OnItemClickListener()
            {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                // TODO: action for item clicked
                Log.i("TwoFragment", "item " + item.getTitle() + " clicked, code: " + item.getItemId());
            }});

        return view;
    }

    // Prepare some dummy data for gridview
    private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();

        Map<String,String> soundMap = ResourceUtils.getHashMapResource(getContext(), R.xml.sound_list);
        Set<String> keys = soundMap.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(key, "drawable", getActivity().getPackageName()));
            imageItems.add(new ImageItem(bitmap, soundMap.get(key), key));
        }

        return imageItems;
    }
}
