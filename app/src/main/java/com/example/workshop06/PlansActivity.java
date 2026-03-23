package com.example.workshop06;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PlansActivity extends AppCompatActivity {

    private RecyclerView recyclerPlans;
    private PlanAdapter planAdapter;
    private List<Plan> planList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);

        recyclerPlans = findViewById(R.id.recyclerPlans);
        recyclerPlans.setLayoutManager(new LinearLayoutManager(this));

        planList = new ArrayList<>();
        planList.add(new Plan("Unlimited 5G", "80GB high-speed data • Canada-wide", "$75/mo", "Popular"));
        planList.add(new Plan("Family Share", "4 lines • Shared data and savings", "$145/mo", "Best Value"));
        planList.add(new Plan("Student Lite", "20GB data • Flexible monthly billing", "$39/mo", "Student"));
        planList.add(new Plan("Home Internet Plus", "Fast home Wi-Fi with modem included", "$89/mo", "Available"));

        planAdapter = new PlanAdapter(planList);
        recyclerPlans.setAdapter(planAdapter);
    }
}