package com.example.vikas.musify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vikas.musify.data.Sensor;
import com.example.vikas.musify.events.BusProvider;
import com.example.vikas.musify.events.NewSensorEvent;
import com.example.vikas.musify.ui.ExportActivity;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.squareup.otto.Subscribe;

import java.util.List;


public class MainActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RemoteSensorManager remoteSensorManager;

    Toolbar mToolbar;

    private ViewPager pager;
    private View emptyState;
    private List<Node> mNodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main2);

        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        emptyState = findViewById(R.id.empty_state);

        initViewPager();

        remoteSensorManager = RemoteSensorManager.getInstance(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setTitle(R.string.app_name);
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_about:
                            startActivity(new Intent(MainActivity2.this, AboutActivity.class));
                            return true;
                        case R.id.action_export:
                            startActivity(new Intent(MainActivity2.this, ExportActivity.class));
                            return true;
                    }

                    return true;
                }
            });
        }
    }

    private void initViewPager() {
        pager = (ViewPager) findViewById(R.id.pager);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int id) {
                ScreenSlidePagerAdapter adapter = (ScreenSlidePagerAdapter) pager.getAdapter();
                if (adapter != null) {
                    Sensor sensor = adapter.getItemObject(id);
                    if (sensor != null) {
                        //remoteSensorManager.filterBySensorId((int) sensor.getId());
                        remoteSensorManager.filterBySensorId(21);

                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        List<Sensor> sensors = RemoteSensorManager.getInstance(this).getSensors();
        pager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager(), sensors));

        if (sensors.size() > 0) {
            emptyState.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.VISIBLE);
        }

        remoteSensorManager.startMeasurement();

        /*remoteSensorManager.getNodes(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(final NodeApi.GetConnectedNodesResult pGetConnectedNodesResult) {
                mNodes = pGetConnectedNodesResult.getNodes();
                for (Node node : mNodes) {
                    SubMenu menu = mNavigationViewMenu.addSubMenu(node.getDisplayName());

                    MenuItem item = menu.add("15 sensors");
                    if (node.getDisplayName().startsWith("G")) {
                        item.setChecked(true);
                        item.setCheckable(true);
                    } else {
                        item.setChecked(false);
                        item.setCheckable(false);
                    }
                }
            }
        });*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);

        remoteSensorManager.stopMeasurement();
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem pMenuItem) {
        Toast.makeText(this, "Device: " + pMenuItem.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private List<Sensor> sensors;

        public ScreenSlidePagerAdapter(FragmentManager fm, List<Sensor> symbols) {
            super(fm);
            this.sensors = symbols;
        }


        public void addNewSensor(Sensor sensor) {
            this.sensors.add(sensor);
        }


        private Sensor getItemObject(int position) {
            return sensors.get(position);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return SensorFragment.newInstance(sensors.get(position).getId());
        }

        @Override
        public int getCount() {
            return sensors.size();
        }

    }


    private void notifyUSerForNewSensor(Sensor sensor) {
        Toast.makeText(this, "New Sensor!\n" + sensor.getName()+ " "+sensor.getId(), Toast.LENGTH_SHORT).show();
    }


    @Subscribe
    public void onNewSensorEvent(final NewSensorEvent event) {
        ((ScreenSlidePagerAdapter) pager.getAdapter()).addNewSensor(event.getSensor());
        pager.getAdapter().notifyDataSetChanged();
        emptyState.setVisibility(View.GONE);
        notifyUSerForNewSensor(event.getSensor());
    }
}
