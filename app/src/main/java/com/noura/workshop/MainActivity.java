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

    private final String[] statuses = {
        "جديد", "قيد الإصلاح", "جاهز للتسليم", "تم التسليم"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("noura_data", MODE_PRIVATE);
        showHome();
    }

    private void setupLayout() {
        ScrollView scroll = new ScrollView(this);
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);
        layout.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        layout.setBackgroundColor(Color.rgb(245, 248, 252));
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scroll.addView(layout);
        setContentView(scroll);
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
        button.setTextSize(16);
        button.setTextColor(Color.WHITE);
        button.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(blue));

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 6, 0, 6);
        layout.addView(button, params);
        button.setOnClickListener(v -> action.run());
    }

    private EditText addInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(16);
        input.setPadding(12, 8, 12, 8);
        layout.addView(input,
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return input;
    }

    private JSONArray readArray(String key) {
        try {
            return new JSONArray(prefs.getString(key, "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void saveArray(String key, JSONArray array) {
        prefs.edit().putString(key, array.toString()).apply();
    }

    private void showHome() {
        setupLayout();
        addTitle("مركز نوره للصيانة");

        addButton("إدارة العملاء", this::showCustomers);
        addButton("طلبات الصيانة", this::showRepairs);
        addButton("الفواتير والمدفوعات",
            () -> showSection("الفواتير والمدفوعات",
                "سيتم تطوير الفواتير في الخطوة التالية."));
        addButton("عروض الأسعار",
            () -> showSection("عروض الأسعار",
                "سيتم تطوير عروض الأسعار لاحقًا."));
    }

    private void showSection(String title, String message) {
        setupLayout();
        addTitle(title);
        TextView info = new TextView(this);
        info.setText(message);
        info.setTextSize(18);
        layout.addView(info);
        addButton("العودة للرئيسية", this::showHome);
    }

    
private void showCustomers() {
    setupLayout();
    addTitle("إدارة العملاء");

    EditText name = addInput("اسم العميل أو المنشأة");
    EditText phone = addInput("رقم الجوال");
    phone.setInputType(3);

    EditText nationalAddress =
        addInput("العنوان الوطني الكامل");

    EditText buildingNumber =
        addInput("رقم المبنى (اختياري)");

    EditText district =
        addInput("الحي (اختياري)");

    EditText city =
        addInput("المدينة (اختياري)");

    EditText postalCode =
        addInput("الرمز البريدي (اختياري)");

    EditText additionalNumber =
        addInput("الرقم الإضافي للعنوان (اختياري)");

    EditText taxNumber =
        addInput("الرقم الضريبي (اختياري)");

    EditText identifierType =
        addInput("نوع المعرّف الآخر (سجل تجاري، رقم منشأة...)");

    EditText identifierValue =
        addInput("رقم المعرّف الآخر (اختياري)");

    addButton("حفظ العميل", () -> {
        String n = name.getText().toString().trim();
        String p = phone.getText().toString().trim();

        if (n.isEmpty() || p.isEmpty()) {
            toast("أدخل اسم العميل ورقم الجوال");
            return;
        }

        JSONArray customers = readArray("customers");

        try {
            JSONObject c = new JSONObject();
            c.put("name", n);
            c.put("phone", p);
            c.put("nationalAddress",
                nationalAddress.getText().toString().trim());
            c.put("buildingNumber",
                buildingNumber.getText().toString().trim());
            c.put("district",
                district.getText().toString().trim());
            c.put("city",
                city.getText().toString().trim());
            c.put("postalCode",
                postalCode.getText().toString().trim());
            c.put("additionalNumber",
                additionalNumber.getText().toString().trim());
            c.put("taxNumber",
                taxNumber.getText().toString().trim());
            c.put("identifierType",
                identifierType.getText().toString().trim());
            c.put("identifierValue",
                identifierValue.getText().toString().trim());

            customers.put(c);
            saveArray("customers", customers);

            toast("تم حفظ بيانات العميل");
            showCustomers();

        } catch (Exception e) {
            toast("تعذر حفظ بيانات العميل");
        }
    });

    addButton("عرض العملاء المسجلين",
        this::showCustomerList);

    addButton("العودة للرئيسية", this::showHome);
}


   
private void showCustomerList() {
    setupLayout();
    addTitle("العملاء المسجلون");

    JSONArray customers = readArray("customers");

    if (customers.length() == 0) {
        addText("لا يوجد عملاء مسجلون.");
    }

    for (int i = 0; i < customers.length(); i++) {
        try {
            JSONObject c = customers.getJSONObject(i);

            String info =
                "الاسم: " + c.optString("name")
                + "\nالجوال: " + c.optString("phone")
                + "\nالعنوان الوطني: "
                + c.optString("nationalAddress", "")
                + "\nرقم المبنى: "
                + c.optString("buildingNumber", "")
                + "\nالحي: " + c.optString("district", "")
                + "\nالمدينة: " + c.optString("city", "")
                + "\nالرمز البريدي: "
                + c.optString("postalCode", "")
                + "\nالرقم الإضافي: "
                + c.optString("additionalNumber", "")
                + "\nالرقم الضريبي: "
                + c.optString("taxNumber", "")
                + "\nنوع المعرّف الآخر: "
                + c.optString("identifierType", "")
                + "\nرقم المعرّف الآخر: "
                + c.optString("identifierValue", "");

            addText(info);

        } catch (Exception ignored) {}
    }

    addButton("إضافة عميل", this::showCustomers);
    addButton("العودة للرئيسية", this::showHome);
}

    private void showRepairs() {
        setupLayout();
        addTitle("طلبات الصيانة");

        EditText customer = addInput("اسم العميل");
        EditText phone = addInput("رقم جوال العميل");
        phone.setInputType(3);
        EditText device = addInput("الجهاز أو المعدة");
        EditText problem = addInput("وصف العطل");

        Spinner statusSpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);
        layout.addView(statusSpinner);

        addButton("حفظ طلب الصيانة", () -> {
            String n = customer.getText().toString().trim();
            String p = phone.getText().toString().trim();
            String d = device.getText().toString().trim();
            String issue = problem.getText().toString().trim();

            if (n.isEmpty() || p.isEmpty()
                    || d.isEmpty() || issue.isEmpty()) {
                toast("أكمل جميع بيانات الطلب");
                return;
            }

            JSONArray repairs = readArray("repairs");

            try {
                JSONObject r = new JSONObject();
                r.put("id", System.currentTimeMillis());
                r.put("customer", n);
                r.put("phone", p);
                r.put("device", d);
                r.put("problem", issue);
                r.put("status",
                    statusSpinner.getSelectedItem().toString());

                repairs.put(r);
                saveArray("repairs", repairs);

                toast("تم حفظ طلب الصيانة");
                showRepairs();
            } catch (Exception e) {
                toast("تعذر حفظ الطلب");
            }
        });

        addButton("عرض طلبات الصيانة", this::showRepairList);
        addButton("العودة للرئيسية", this::showHome);
    }

    private void showRepairList() {
        setupLayout();
        addTitle("قائمة طلبات الصيانة");

        JSONArray repairs = readArray("repairs");

        if (repairs.length() == 0) {
            addText("لا توجد طلبات صيانة مسجلة.");
        }

        for (int i = 0; i < repairs.length(); i++) {
            try {
                JSONObject r = repairs.getJSONObject(i);
                final long id = r.optLong("id");
                String status = r.optString("status", "جديد");

                addText(
                    "العميل: " + r.optString("customer")
                    + "\nالجوال: " + r.optString("phone")
                    + "\nالجهاز: " + r.optString("device")
                    + "\nالعطل: " + r.optString("problem")
                    + "\nالحالة: " + status
                );

                addButton("تحديث حالة الطلب", () ->
                    updateRepairStatus(id));
            } catch (Exception ignored) {}
        }

        addButton("إضافة طلب صيانة", this::showRepairs);
        addButton("العودة للرئيسية", this::showHome);
    }

    private void updateRepairStatus(long id) {
        JSONArray repairs = readArray("repairs");

        for (int i = 0; i < repairs.length(); i++) {
            try {
                JSONObject r = repairs.getJSONObject(i);

                if (r.optLong("id") == id) {
                    String current = r.optString("status", "جديد");
                    int next = 0;

                    for (int j = 0; j < statuses.length; j++) {
                        if (statuses[j].equals(current)) {
                            next = (j + 1) % statuses.length;
                            break;
                        }
                    }

                    r.put("status", statuses[next]);
                    saveArray("repairs", repairs);
                    toast("الحالة الجديدة: " + statuses[next]);
                    showRepairList();
                    return;
                }
            } catch (Exception ignored) {}
        }

        toast("لم يتم العثور على الطلب");
    }

    private void addText(String text) {
        TextView item = new TextView(this);
        item.setText(text);
        item.setTextSize(16);
        item.setTextColor(Color.DKGRAY);
        item.setPadding(8, 14, 8, 14);
        layout.addView(item);
        View line = new View(this);
        line.setBackgroundColor(Color.LTGRAY);
        layout.addView(line,
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
