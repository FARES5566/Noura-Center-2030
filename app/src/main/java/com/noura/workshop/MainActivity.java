
package com.noura.workshop;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.content.SharedPreferences;
import android.text.InputType;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Locale;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {

    private final int blue = Color.rgb(25, 95, 190);
    private LinearLayout layout;
    private SharedPreferences prefs;
    private ImageView logoPreview;
    private String selectedLogoPath = "";
    private static final int PICK_LOGO_REQUEST = 2001;
    private final String[] units = {"صيانة", "قطع غيار"};
    private static final double TAX_RATE = 0.15;

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
addButton("بيانات المنشأة", this::showCompanyData);
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
        input.setSingleLine(true);
        layout.addView(input,
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return input;
    }

    private EditText addNumberInput(String hint) {
        EditText input = addInput(hint);
        input.setInputType(
            InputType.TYPE_CLASS_NUMBER
            | InputType.TYPE_NUMBER_FLAG_DECIMAL
            | InputType.TYPE_NUMBER_FLAG_SIGNED);
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
        addButton("كرت الأصناف", this::showItems);
        addButton("الفواتير والمدفوعات",
            () -> showSection("الفواتير والمدفوعات",
                "سيتم تطوير الفواتير في الخطوة التالية."));
        addButton("عروض الأسعار",
            () -> showSection("عروض الأسعار",
                "سيتم تطوير عروض الأسعار لاحقًا."));
    }

private void showCompanyData() {
    setupLayout();
    addTitle("بيانات المنشأة");

    EditText companyName = addInput("اسم المنشأة");
    companyName.setText(prefs.getString("company_name", ""));

    EditText address = addInput("العنوان الوطني");
    address.setText(prefs.getString("company_address", ""));

    EditText taxNumber = addInput("الرقم الضريبي (اختياري)");
    taxNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
    taxNumber.setText(prefs.getString("company_tax", ""));

    EditText otherId = addInput("المعرّف الآخر (اختياري)");
    otherId.setText(prefs.getString("company_other_id", ""));

    TextView logoLabel = new TextView(this);
    logoLabel.setText("شعار المنشأة");
    logoLabel.setTextSize(18);
    logoLabel.setTextColor(blue);
    logoLabel.setPadding(0, 18, 0, 8);
    layout.addView(logoLabel);

    logoPreview = new ImageView(this);
    logoPreview.setAdjustViewBounds(true);
    logoPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);

    LinearLayout.LayoutParams logoParams =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 240);
    layout.addView(logoPreview, logoParams);

    selectedLogoPath = prefs.getString("company_logo_path", "");
    loadCompanyLogo();

    addButton("اختيار شعار من الجهاز", () -> {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_LOGO_REQUEST);
    });

    addButton("حفظ بيانات المنشأة", () -> {
        String name = companyName.getText().toString().trim();

        if (name.isEmpty()) {
            toast("أدخل اسم المنشأة");
            return;
        }

        prefs.edit()
            .putString("company_name", name)
            .putString("company_address",
                address.getText().toString().trim())
            .putString("company_tax",
                taxNumber.getText().toString().trim())
            .putString("company_other_id",
                otherId.getText().toString().trim())
            .putString("company_logo_path", selectedLogoPath)
            .apply();

        toast("تم حفظ بيانات المنشأة");
    });

    addButton("العودة للرئيسية", this::showHome);
}

@Override
protected void onActivityResult(
        int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == PICK_LOGO_REQUEST
            && resultCode == RESULT_OK
            && data != null
            && data.getData() != null) {

        Uri uri = data.getData();

        try (InputStream input = getContentResolver()
                .openInputStream(uri)) {

            if (input == null) {
                toast("تعذر فتح الصورة");
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(input);

            if (bitmap == null) {
                toast("صيغة الصورة غير مدعومة");
                return;
            }

            File logoFile = new File(getFilesDir(), "company_logo.png");

            try (FileOutputStream output =
                    new FileOutputStream(logoFile)) {
                bitmap.compress(
                    Bitmap.CompressFormat.PNG, 100, output);
                output.flush();
            }

            selectedLogoPath = logoFile.getAbsolutePath();

            prefs.edit()
                .putString("company_logo_path", selectedLogoPath)
                .apply();

            if (logoPreview != null) {
                logoPreview.setImageBitmap(bitmap);
            }

            toast("تم اختيار شعار المنشأة");

        } catch (Exception e) {
            toast("تعذر حفظ الشعار");
        }
    }
}

private void loadCompanyLogo() {
    if (logoPreview == null || selectedLogoPath.isEmpty()) {
        return;
    }

    File file = new File(selectedLogoPath);

    if (file.exists()) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        if (bitmap != null) {
            logoPreview.setImageBitmap(bitmap);
        }
    }
}

    private void showSection(String title, String message) {
        setupLayout();
        addTitle(title);
        addText(message);
        addButton("العودة للرئيسية", this::showHome);
    }

    // إدارة العملاء
    private void showCustomers() {
        setupLayout();
        addTitle("إدارة العملاء");

        EditText name = addInput("اسم العميل أو المنشأة");
        EditText phone = addNumberInput("رقم الجوال");
        EditText nationalAddress = addInput("العنوان الوطني الكامل");
        EditText buildingNumber = addNumberInput("رقم المبنى (اختياري)");
        EditText district = addInput("الحي (اختياري)");
        EditText city = addInput("المدينة (اختياري)");
        EditText postalCode = addNumberInput("الرمز البريدي (اختياري)");
        EditText additionalNumber =
            addNumberInput("الرقم الإضافي للعنوان (اختياري)");
        EditText taxNumber = addNumberInput("الرقم الضريبي (اختياري)");
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
                c.put("district", district.getText().toString().trim());
                c.put("city", city.getText().toString().trim());
                c.put("postalCode",
                    postalCode.getText().toString().trim());
                c.put("additionalNumber",
                    additionalNumber.getText().toString().trim());
                c.put("taxNumber", taxNumber.getText().toString().trim());
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

        addButton("عرض العملاء المسجلين", this::showCustomerList);
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

                addText(
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
                    + c.optString("identifierValue", "")
                );
            } catch (Exception ignored) {}
        }

        addButton("إضافة عميل", this::showCustomers);
        addButton("العودة للرئيسية", this::showHome);
    }

    // كرت الأصناف
    private void showItems() {
        setupLayout();
        addTitle("كرت الأصناف");

        EditText itemNumber = addInput("رقم الصنف");
        EditText itemName = addInput("نوع الصنف / اسم الصنف");

        TextView unitLabel = new TextView(this);
        unitLabel.setText("الوحدة");
        unitLabel.setTextSize(16);
        unitLabel.setTextColor(Color.DKGRAY);
        layout.addView(unitLabel);

        Spinner unitSpinner = new Spinner(this);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);
        layout.addView(unitSpinner);

        EditText costInput = addNumberInput("سعر التكلفة بالريال");
        EditText saleInput = addNumberInput("سعر البيع قبل الضريبة");

        CheckBox taxCheck = new CheckBox(this);
        taxCheck.setText("تطبيق ضريبة القيمة المضافة 15%");
        taxCheck.setTextSize(16);
        taxCheck.setChecked(true);
        layout.addView(taxCheck);

        TextView totalLabel = new TextView(this);
        totalLabel.setTextSize(19);
        totalLabel.setTextColor(blue);
        totalLabel.setPadding(0, 16, 0, 16);
        layout.addView(totalLabel);

        Runnable calculate = () -> {
            try {
                double sale = Double.parseDouble(
                    saleInput.getText().toString().trim());

                double total = taxCheck.isChecked()
                    ? sale * (1 + TAX_RATE) : sale;

                totalLabel.setText(
                    "سعر البيع شامل الضريبة: "
                    + String.format(Locale.US, "%.2f", total)
                    + " ريال"
                );
            } catch (Exception e) {
                totalLabel.setText(
                    "سعر البيع شامل الضريبة: —");
            }
        };

        saleInput.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(
                CharSequence s, int start, int count, int after) {}
            public void onTextChanged(
                CharSequence s, int start, int before, int count) {
                calculate.run();
            }
            public void afterTextChanged(android.text.Editable s) {}
        });

        taxCheck.setOnCheckedChangeListener(
            (buttonView, isChecked) -> calculate.run());

        calculate.run();

        addButton("حفظ الصنف", () -> {
            String number = itemNumber.getText().toString().trim();
            String name = itemName.getText().toString().trim();
            String costText = costInput.getText().toString().trim();
            String saleText = saleInput.getText().toString().trim();

            if (number.isEmpty() || name.isEmpty()
                    || costText.isEmpty() || saleText.isEmpty()) {
                toast("أكمل رقم الصنف واسمه والأسعار");
                return;
            }

            double cost;
            double sale;

            try {
                cost = Double.parseDouble(costText);
                sale = Double.parseDouble(saleText);

                if (cost < 0 || sale < 0) {
                    toast("لا يمكن إدخال سعر سالب");
                    return;
                }
            } catch (Exception e) {
                toast("تحقق من صحة الأسعار");
                return;
            }

            JSONArray items = readArray("items");

            for (int i = 0; i < items.length(); i++) {
                if (items.optJSONObject(i) != null
                        && number.equalsIgnoreCase(
                            items.optJSONObject(i).optString("number"))) {
                    toast("رقم الصنف مسجل مسبقًا");
                    return;
                }
            }

            try {
                double total = taxCheck.isChecked()
                    ? sale * (1 + TAX_RATE) : sale;

                JSONObject item = new JSONObject();
                item.put("number", number);
                item.put("name", name);
                item.put("unit",
                    unitSpinner.getSelectedItem().toString());
                item.put("cost", cost);
                item.put("sale", sale);
                item.put("taxEnabled", taxCheck.isChecked());
                item.put("taxRate",
                    taxCheck.isChecked() ? TAX_RATE : 0);
                item.put("saleWithTax", total);

                items.put(item);
                saveArray("items", items);

                toast("تم حفظ الصنف بنجاح");
                showItems();
            } catch (Exception e) {
                toast("تعذر حفظ الصنف");
            }
        });

        addButton("عرض الأصناف المسجلة", this::showItemList);
        addButton("العودة للرئيسية", this::showHome);
    }

    private void showItemList() {
        setupLayout();
        addTitle("الأصناف المسجلة");

        JSONArray items = readArray("items");

        if (items.length() == 0) {
            addText("لا توجد أصناف مسجلة.");
        }

        for (int i = 0; i < items.length(); i++) {
            try {
                JSONObject item = items.getJSONObject(i);

                String taxStatus = item.optBoolean("taxEnabled", true)
                    ? "15%" : "غير مطبقة";

                addText(
                    "رقم الصنف: " + item.optString("number")
                    + "\nنوع الصنف: " + item.optString("name")
                    + "\nالوحدة: " + item.optString("unit")
                    + "\nسعر التكلفة: "
                    + money(item.optDouble("cost"))
                    + " ريال"
                    + "\nسعر البيع: "
                    + money(item.optDouble("sale"))
                    + " ريال"
                    + "\nالضريبة: " + taxStatus
                    + "\nسعر البيع شامل الضريبة: "
                    + money(item.optDouble("saleWithTax"))
                    + " ريال"
                );
            } catch (Exception ignored) {}
        }

        addButton("إضافة صنف", this::showItems);
        addButton("العودة للرئيسية", this::showHome);
    }

    private String money(double value) {
        return String.format(Locale.US, "%.2f", value);
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
