package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private Button deleteCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;
    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    private City selectedCity;
    private boolean deleteMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        deleteCityButton = findViewById(R.id.buttonDeleteCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // addDummyData(); This was here previously and I'm just gonna keep it commented out

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        deleteCityButton.setOnClickListener(view -> {
            if (deleteMode) {
                Toast myToast = Toast.makeText(MainActivity.this, "Deletion Cancelled.",
                        Toast.LENGTH_SHORT);
                myToast.show();
                deleteMode = false;
            }
            else {
                Toast myToast = Toast.makeText(MainActivity.this, "Press the City You'd Like to Delete, press again to cancel",
                        Toast.LENGTH_LONG);
                myToast.show();
                deleteMode = true;
            }
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedCity = cityArrayAdapter.getItem(i);
            if (!deleteMode) {
                CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(selectedCity);
                cityDialogFragment.show(getSupportFragmentManager(), "City Details");
            }
            else {
                deleteCity(selectedCity);
                Toast myToast = Toast.makeText(MainActivity.this, "Deletion Successful!",
                        Toast.LENGTH_SHORT);
                myToast.show();
                deleteMode = false;
            }
        });

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    cityArrayList.add(new City(name, province));
                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateCity(City city, String title, String year) {
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        // Updating the database using delete + addition
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);
    }

    @Override
    public void deleteCity(City city) {
        cityArrayList.remove(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.delete();
    }

    /*
    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
     */
}