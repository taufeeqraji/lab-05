package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private CityArrayAdapter cityArrayAdapter;

    // Firestore
    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Firestore setup
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // Adapter
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // Snapshot listener (Firestore -> UI)
        citiesRef.addSnapshotListener((QuerySnapshot value, FirebaseFirestoreException error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
                return;
            }

            if (value != null) {
                cityArrayList.clear();

                for (QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");
                    cityArrayList.add(new City(name, province));
                }

                cityArrayAdapter.notifyDataSetChanged();
            }
        });

        // Add city dialog
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment dialog = new CityDialogFragment();
            dialog.show(getSupportFragmentManager(), "Add City");
        });

        // Edit/view city dialog
        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment dialog = CityDialogFragment.newInstance(city);
            dialog.show(getSupportFragmentManager(), "City Details");
        });

        // ✅ Delete on long press
        cityListView.setOnItemLongClickListener((adapterView, view, position, id) -> {
            City city = cityArrayAdapter.getItem(position);
            if (city != null) {
                deleteCity(city);
            }
            return true;
        });
    }

    @Override
    public void updateCity(City city, String title, String year) {
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        // If your lab later asks: implement Firestore update here.
    }

    @Override
    public void addCity(City city) {
        citiesRef
                .document(city.getName())
                .set(city)
                .addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error writing document", e));
    }

    private void deleteCity(City city) {
        citiesRef
                .document(city.getName())
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "City deleted: " + city.getName()))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error deleting city: " + city.getName(), e));
    }
}
