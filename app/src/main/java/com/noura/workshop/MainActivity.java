package com.noura.workshop;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private final int blue = Color.rgb(25, 95, 190);
    private LinearLayout layout;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("noura_data", MODE_PRIVATE);
        showHome();
    }

    private void setupLayout() {
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 32, 24, 24);
        layout.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        layout.setBackgroundColor(Color.rgb(245, 248, 252));
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setContentView(layout);
    }

    private void addTitle(String text) {
        TextView title = new TextView(this);
        title.setText(text);
        title.setTextSize(26);
        title.setTextColor(blue);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 16, 0, 24);
        layout.addView(title);
    }

    private void addButton(String text, Runnable action) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(17);
        button.setTextColor(Color.WHITE);
        button.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(blue)
        );

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
        params.setMargins(0, 8, 0, 8);
        layout.addView(button, params);

        button.setOnClickListener(v -> action.run());
    }

    private void showHome() {
        setupLayout();
        addTitle("مركز نوره للصيانة");

        TextView subtitle = new TextView(this);
        subtitle.setText("إدارة العملاء وأعمال الصيانة والفواتير");
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        layout.addView(subtitle);

        addButton("إدارة العملاء", this::showCustomers);
        addButton("طلبات الصيانة",
            () -> showSection("طلبات الصيانة",
                "يمكنك هنا متابعة طلبات الصيانة ومراحل التنفيذ."));
        addButton("الفواتير والمدفوعات",
            () -> showSection("الفواتير والمدفوعات",
                "سيُخصص هذا القسم للفواتير والمبالغ المدفوعة والمتبقية."));
        addButton("عروض الأسعار",
            () -> showSection("عروض الأسعار",
                "سيُخصص هذا القسم لإعداد عروض الأسعار للعملاء."));
    }

    private void showSection(String title, String message) {
        setupLayout();
        addTitle(title);

        TextView info = new TextView(this);
        info.setText(message);
        info.setTextSize(18);
        info.setTextColor(Color.DKGRAY);
        info.setPadding(0, 12, 0, 24);
        layout.addView(info);

        addButton("العودة للرئيسية", this::showHome);
    }

    private EditText addInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(16);
        input.setSingleLine(true);
        input.setPadding(12, 8, 12, 8);
        layout.addView(input,
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
        return input;
    }

    private void showCustomers() {
        setupLayout();
        addTitle("إدارة العملاء");

        EditText nameInput = addInput("اسم العميل");
        EditText phoneInput = addInput("رقم الجوال");

        addButton("حفظ العميل", () -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this,
                    "أدخل اسم العميل ورقم الجوال",
                    Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONArray customers = new JSONArray(
                    prefs.getString("customers", "[]")
                );

                JSONObject customer = new JSONObject();
                customer.put("name", name);
                customer.put("phone", phone);
                customers.put(customer);

                prefs.edit()
                    .putString("customers", customers.toString())
                    .apply();

                Toast.makeText(this,
                    "تم حفظ بيانات العميل",
                    Toast.LENGTH_SHORT).show();

                showCustomers();
            } catch (Exception e) {
                Toast.makeText(this,
                    "تعذر حفظ بيانات العميل",
                    Toast.LENGTH_SHORT).show();
            }
        });

        addButton("عرض العملاء المسجلين",
            this::showCustomerList);
        addButton("العودة للرئيسية", this::showHome);
    }

    private void showCustomerList() {
        setupLayout();
        addTitle("العملاء المسجلون");

        try {
            JSONArray customers = new JSONArray(
                prefs.getString("customers", "[]")
            );

            if (customers.length() == 0) {
                TextView empty = new TextView(this);
                empty.setText("لا يوجد عملاء مسجلون حتى الآن");
                empty.setTextSize(17);
                layout.addView(empty);
            }

            for (int i = 0; i < customers.length(); i++) {
                JSONObject customer = customers.getJSONObject(i);

                TextView item = new TextView(this);
                item.setText(
                    "الاسم: " + customer.getString("name")
                    + "\nالجوال: " + customer.getString("phone")
                );
                item.setTextSize(17);
                item.setTextColor(Color.DKGRAY);
                item.setPadding(8, 16, 8, 16);
                layout.addView(item);
            }
        } catch (Exception e) {
            Toast.makeText(this,
                "تعذر عرض العملاء",
                Toast.LENGTH_SHORT).show();
        }

        addButton("إضافة عميل", this::showCustomers);
        addButton("العودة للرئيسية", this::showHome);
    }
}
