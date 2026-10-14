
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

    addButton("بيانات المنشأة", this::showCompanyData);
    addButton("إدارة العملاء", this::showCustomers);
    addButton("كرت الأصناف", this::showItems);
    addButton("إدارة المبيعات", this::showSales);
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

private void showSales() {
    setupLayout();
    addTitle("إدارة المبيعات");

    addButton("إنشاء عرض سعر",
        () -> showSalesDocument(false));

    addButton("البحث عن عروض الأسعار",
        () -> showSalesSearch(false));

    addButton("إنشاء فاتورة مبيعات",
        () -> showSalesDocument(true));

    addButton("البحث عن فواتير المبيعات",
        () -> showSalesSearch(true));

    addButton("العودة للرئيسية", this::showHome);
}

private void showSalesDocument(boolean invoice) {
    setupLayout();
    addTitle(invoice ? "فاتورة مبيعات جديدة" : "عرض سعر جديد");

    JSONArray customers = readArray("customers");
    java.util.ArrayList<String> customerLabels =
        new java.util.ArrayList<>();
    customerLabels.add("اختر العميل");

    for (int i = 0; i < customers.length(); i++) {
        JSONObject c = customers.optJSONObject(i);
        if (c != null) {
            customerLabels.add(
                c.optString("name") + " - " + c.optString("phone"));
        }
    }

    Spinner customerSpinner = new Spinner(this);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(
        this, android.R.layout.simple_spinner_item, customerLabels);
    adapter.setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item);
    customerSpinner.setAdapter(adapter);
    layout.addView(customerSpinner);

    TextView customerDetails = new TextView(this);
    customerDetails.setTextSize(15);
    customerDetails.setTextColor(Color.DKGRAY);
    layout.addView(customerDetails);

    customerSpinner.setOnItemSelectedListener(
        new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    android.widget.AdapterView<?> parent,
                    View view, int position, long id) {
                if (position > 0) {
                    JSONObject c = customers.optJSONObject(position - 1);
                    if (c != null) {
                        customerDetails.setText(
                            "الاسم: " + c.optString("name")
                            + "\nالجوال: " + c.optString("phone")
                            + "\nالعنوان الوطني: "
                            + c.optString("nationalAddress")
                            + "\nالرقم الضريبي: "
                            + c.optString("taxNumber")
                            + "\nالمعرّف الآخر: "
                            + c.optString("identifierType") + " "
                            + c.optString("identifierValue"));
                    }
                } else {
                    customerDetails.setText("");
                }
            }

            @Override
            public void onNothingSelected(
                    android.widget.AdapterView<?> parent) {}
        });

    addButton("إضافة عميل جديد", this::showCustomers);

    JSONArray items = readArray("items");
    java.util.ArrayList<String> itemLabels =
        new java.util.ArrayList<>();
    itemLabels.add("اختر الصنف");

    for (int i = 0; i < items.length(); i++) {
        JSONObject item = items.optJSONObject(i);
        if (item != null) {
            itemLabels.add(item.optString("number") + " - "
                + item.optString("name"));
        }
    }

    Spinner itemSpinner = new Spinner(this);
    ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(
        this, android.R.layout.simple_spinner_item, itemLabels);
    itemAdapter.setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item);
    itemSpinner.setAdapter(itemAdapter);
    layout.addView(itemSpinner);

    EditText quantityInput = addNumberInput("الكمية");
    TextView totals = new TextView(this);
    totals.setTextSize(17);
    totals.setTextColor(blue);
    layout.addView(totals);

    final double[] sale = {0};
    final double[] taxRate = {0.15};

    Runnable calculate = () -> {
        try {
            int pos = itemSpinner.getSelectedItemPosition();
            double qty = Double.parseDouble(
                quantityInput.getText().toString());
            JSONObject item = items.optJSONObject(pos - 1);
            if (pos <= 0 || item == null || qty <= 0) {
                totals.setText("اختر صنفًا وأدخل كمية صحيحة");
                return;
            }
            sale[0] = item.optDouble("sale") * qty;
            taxRate[0] = item.optBoolean("taxEnabled", true)
                ? item.optDouble("taxRate", 0.15) : 0;
            double tax = sale[0] * taxRate[0];
            totals.setText(
                "الإجمالي قبل الضريبة: " + money(sale[0])
                + " ريال\nالضريبة: " + money(tax)
                + " ريال\nالإجمالي شامل الضريبة: "
                + money(sale[0] + tax) + " ريال");
        } catch (Exception e) {
            totals.setText("اختر صنفًا وأدخل الكمية");
        }
    };

    itemSpinner.setOnItemSelectedListener(
        new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    android.widget.AdapterView<?> p,
                    View v, int position, long id) {
                calculate.run();
            }

            @Override
            public void onNothingSelected(
                    android.widget.AdapterView<?> p) {}
        });

    quantityInput.addTextChangedListener(new android.text.TextWatcher() {
        public void beforeTextChanged(
            CharSequence s, int start, int count, int after) {}
        public void onTextChanged(
            CharSequence s, int start, int before, int count) {
            calculate.run();
        }
        public void afterTextChanged(android.text.Editable e) {}
    });

    EditText notes = addInput("ملاحظات (اختياري)");

    addButton("حفظ المستند", () -> {
        int customerPos = customerSpinner.getSelectedItemPosition();
        int itemPos = itemSpinner.getSelectedItemPosition();

        if (customerPos <= 0 || itemPos <= 0) {
            toast("اختر العميل والصنف");
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(
                quantityInput.getText().toString().trim());
            if (qty <= 0) {
                toast("أدخل كمية أكبر من صفر");
                return;
            }
        } catch (Exception e) {
            toast("تحقق من الكمية");
            return;
        }

        JSONObject c = customers.optJSONObject(customerPos - 1);
        JSONObject item = items.optJSONObject(itemPos - 1);
        if (c == null || item == null) {
            toast("تعذر قراءة بيانات العميل أو الصنف");
            return;
        }

        double subtotal = item.optDouble("sale") * qty;
        double tax = subtotal * (item.optBoolean("taxEnabled", true)
            ? item.optDouble("taxRate", 0.15) : 0);
        double total = subtotal + tax;

        String key = invoice ? "sales_invoices" : "sales_quotes";
        JSONArray documents = readArray(key);
        String prefix = invoice ? "INV-" : "QUO-";
        String number = prefix + String.format(
            Locale.US, "%06d", documents.length() + 1);

        try {
            JSONObject doc = new JSONObject();
            doc.put("number", number);
            doc.put("date", new java.text.SimpleDateFormat(
                "yyyy-MM-dd", Locale.US).format(new java.util.Date()));
            doc.put("customer", c);
            doc.put("item", item);
            doc.put("quantity", qty);
            doc.put("subtotal", subtotal);
            doc.put("tax", tax);
            doc.put("total", total);
            doc.put("notes", notes.getText().toString().trim());
            doc.put("paid", 0);
            doc.put("type", invoice ? "invoice" : "quote");

            documents.put(doc);
            saveArray(key, documents);
            toast("تم حفظ المستند رقم " + number);
            showSalesPreview(doc, invoice);
        } catch (Exception e) {
            toast("تعذر حفظ المستند");
        }
    });

    addButton("العودة لإدارة المبيعات", this::showSales);
}

private void showSalesSearch(boolean invoice) {
    setupLayout();
    addTitle(invoice ? "البحث عن فواتير المبيعات"
                    : "البحث عن عروض الأسعار");

    EditText fromDate = addInput("من تاريخ YYYY-MM-DD");
    EditText toDate = addInput("إلى تاريخ YYYY-MM-DD");
    EditText customerName = addInput("اسم العميل (اختياري)");
    EditText phone = addInput("رقم الجوال (اختياري)");

    addButton("بحث", () -> {
        setupLayout();
        addTitle("نتائج البحث");

        JSONArray docs = readArray(
            invoice ? "sales_invoices" : "sales_quotes");

        String from = fromDate.getText().toString().trim();
        String to = toDate.getText().toString().trim();
        String name = customerName.getText().toString().trim();
        String mobile = phone.getText().toString().trim();
        int count = 0;

        for (int i = 0; i < docs.length(); i++) {
            JSONObject d = docs.optJSONObject(i);
            if (d == null) continue;

            JSONObject c = d.optJSONObject("customer");
            if (c == null) continue;

            String date = d.optString("date");
            String n = c.optString("name");
            String p = c.optString("phone");

            if (!from.isEmpty() && date.compareTo(from) < 0) continue;
            if (!to.isEmpty() && date.compareTo(to) > 0) continue;
            if (!name.isEmpty() &&
                !n.toLowerCase(Locale.ROOT).contains(
                    name.toLowerCase(Locale.ROOT))) continue;
            if (!mobile.isEmpty() && !p.contains(mobile)) continue;

            count++;
            addText(d.optString("number") + "\nالتاريخ: " + date
                + "\nالعميل: " + n + "\nالجوال: " + p
                + "\nالإجمالي: " + money(d.optDouble("total"))
                + " ريال");

            final JSONObject selected = d;
            addButton("عرض وطباعة " + d.optString("number"),
                () -> showSalesPreview(selected, invoice));
        }

        if (count == 0) addText("لا توجد نتائج مطابقة.");
        addButton("بحث جديد", () -> showSalesSearch(invoice));
        addButton("العودة لإدارة المبيعات", this::showSales);
    });

    addButton("العودة لإدارة المبيعات", this::showSales);
}

private void showSalesPreview(JSONObject doc, boolean invoice) {
    setupLayout();
    addTitle(invoice ? "فاتورة مبيعات" : "عرض سعر");

    JSONObject c = doc.optJSONObject("customer");
    JSONObject item = doc.optJSONObject("item");

    addText(prefs.getString("company_name", "مركز نوره للصيانة")
        + "\nالعنوان: " + prefs.getString("company_address", "")
        + "\nالرقم الضريبي: " + prefs.getString("company_tax", "")
        + "\nرقم المستند: " + doc.optString("number")
        + "\nالتاريخ: " + doc.optString("date"));

    if (c != null) {
        addText("بيانات العميل\nالاسم: " + c.optString("name")
            + "\nالجوال: " + c.optString("phone")
            + "\nالعنوان الوطني: " + c.optString("nationalAddress")
            + "\nالرقم الضريبي: " + c.optString("taxNumber")
            + "\nالمعرّف الآخر: " + c.optString("identifierType")
            + " " + c.optString("identifierValue"));
    }

    if (item != null) {
        addText("الصنف: " + item.optString("name")
            + "\nرقم الصنف: " + item.optString("number")
            + "\nالكمية: " + money(doc.optDouble("quantity"))
            + "\nسعر الوحدة: " + money(item.optDouble("sale"))
            + "\nالإجمالي قبل الضريبة: "
            + money(doc.optDouble("subtotal"))
            + "\nالضريبة: " + money(doc.optDouble("tax"))
            + "\nالإجمالي: " + money(doc.optDouble("total")) + " ريال");
    }

    addText("ملاحظات: " + doc.optString("notes"));

    addButton("طباعة / معاينة الطباعة", () -> {
        android.webkit.WebView webView = new android.webkit.WebView(this);
        String html = "<html><meta charset='UTF-8'><body dir='rtl' "
            + "style='font-family:sans-serif;padding:20px'>"
            + "<h2>" + (invoice ? "فاتورة مبيعات" : "عرض سعر") + "</h2>"
            + "<h3>" + prefs.getString("company_name", "مركز نوره للصيانة")
            + "</h3><p>رقم المستند: " + doc.optString("number")
            + "</p><p>التاريخ: " + doc.optString("date")
            + "</p><p>العميل: " + (c == null ? "" : c.optString("name"))
            + "</p><p>الجوال: " + (c == null ? "" : c.optString("phone"))
            + "</p><p>العنوان الوطني: "
            + (c == null ? "" : c.optString("nationalAddress"))
            + "</p><p>الصنف: " + (item == null ? "" : item.optString("name"))
            + "</p><p>الكمية: " + doc.optDouble("quantity")
            + "</p><p>الإجمالي قبل الضريبة: " + money(doc.optDouble("subtotal"))
            + "</p><p>الضريبة: " + money(doc.optDouble("tax"))
            + "</p><h3>الإجمالي: " + money(doc.optDouble("total"))
            + " ريال</h3><p>ملاحظات: " + doc.optString("notes")
            + "</p></body></html>";

        webView.loadDataWithBaseURL(
            null, html, "text/html", "UTF-8", null);

        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageFinished(android.webkit.WebView view,
                                       String url) {
                android.print.PrintManager printManager =
                    (android.print.PrintManager) getSystemService(
                        PRINT_SERVICE);
                android.print.PrintDocumentAdapter adapter =
                    view.createPrintDocumentAdapter(
                        invoice ? "فاتورة مبيعات" : "عرض سعر");
                printManager.print(
                    doc.optString("number"), adapter,
                    new android.print.PrintAttributes.Builder().build());
            }
        });
    });

    addButton("العودة لإدارة المبيعات", this::showSales);
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
