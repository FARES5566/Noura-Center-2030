
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

    // لوحة ألوان عصرية: كحلي داكن مع تركوازي وخلفية فاتحة
    private final int blue = Color.rgb(24, 44, 76);       // كحلي
    private final int teal = Color.rgb(0, 150, 136);      // تركوازي
    private final int backgroundColor = Color.rgb(244, 247, 251);
    private final int textColor = Color.rgb(39, 52, 70);
    private final int mutedColor = Color.rgb(105, 119, 138);
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
        scroll.setFillViewport(true);
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 22, 20, 26);
        layout.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        layout.setBackgroundColor(backgroundColor);
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scroll.addView(layout);
        setContentView(scroll);
    }

    private android.graphics.drawable.GradientDrawable roundedBackground(
            int color, int radiusDp) {
        android.graphics.drawable.GradientDrawable shape =
                new android.graphics.drawable.GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radiusDp * getResources().getDisplayMetrics().density);
        return shape;
    }

    private void addTitle(String text) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(18, 22, 18, 22);
        header.setBackground(roundedBackground(blue, 22));

        TextView title = new TextView(this);
        title.setText(text);
        title.setTextSize(25);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        header.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        if ("مركز نوره للصيانة".equals(text)) {
            TextView subtitle = new TextView(this);
            subtitle.setText("إدارة العملاء وأعمال الصيانة والمبيعات");
            subtitle.setTextSize(14);
            subtitle.setTextColor(Color.rgb(220, 232, 244));
            subtitle.setGravity(Gravity.CENTER);
            subtitle.setPadding(0, 8, 0, 0);
            header.addView(subtitle);
        }

        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        headerParams.setMargins(0, 0, 0, 20);
        layout.addView(header, headerParams);
    }

    private void addButton(String text, Runnable action) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setMinHeight((int) (52 * getResources().getDisplayMetrics().density));
        button.setPadding(16, 8, 16, 8);
        button.setBackground(roundedBackground(teal, 15));
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            button.setElevation(2 * getResources().getDisplayMetrics().density);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 5, 0, 9);
        layout.addView(button, params);
        button.setOnClickListener(v -> action.run());
    }

    private EditText addInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(15);
        input.setTextColor(textColor);
        input.setHintTextColor(mutedColor);
        input.setPadding(15, 12, 15, 12);
        input.setSingleLine(true);
        input.setBackground(roundedBackground(Color.WHITE, 13));
        input.setElevation(1 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 4, 0, 9);
        layout.addView(input, params);
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
    TextView menuTitle = new TextView(this);
    menuTitle.setText("القائمة الرئيسية");
    menuTitle.setTextSize(18);
    menuTitle.setTypeface(null, android.graphics.Typeface.BOLD);
    menuTitle.setTextColor(blue);
    menuTitle.setGravity(Gravity.RIGHT);
    menuTitle.setPadding(0, 4, 8, 12);
    layout.addView(menuTitle);

    addMenuGroup("⚙  بيانات المنشأة", new String[]{"بيانات المنشأة", "العودة للرئيسية"}, new Runnable[]{this::showCompanyData, this::showHome});
    addMenuGroup("👥  إدارة العملاء", new String[]{"إضافة / عرض العملاء"}, new Runnable[]{this::showCustomers});
    addMenuGroup("📦  كرت الأصناف", new String[]{"إضافة / عرض الأصناف"}, new Runnable[]{this::showItems});
    addMenuGroup("🧾  إدارة المبيعات", new String[]{"عروض الأسعار", "فواتير المبيعات", "البحث عن عرض سعر", "البحث عن فاتورة"}, new Runnable[]{() -> showSalesDocument(false), () -> showSalesDocument(true), () -> showSalesSearch(false), () -> showSalesSearch(true)});
    addText("اختر القسم من القائمة لعرض الخيارات الفرعية أسفله.");
}

private void addMenuGroup(String title, String[] labels, Runnable[] actions) {
    LinearLayout group = new LinearLayout(this);
    group.setOrientation(LinearLayout.VERTICAL);
    group.setPadding(10, 8, 10, 8);
    group.setBackground(roundedBackground(Color.WHITE, 16));
    LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    gp.setMargins(0, 5, 0, 8);
    layout.addView(group, gp);
    Button header = new Button(this);
    header.setText(title + "   ▾");
    header.setTextSize(16);
    header.setAllCaps(false);
    header.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
    header.setTextColor(Color.WHITE);
    header.setBackground(roundedBackground(blue, 13));
    group.addView(header, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    LinearLayout submenu = new LinearLayout(this);
    submenu.setOrientation(LinearLayout.VERTICAL);
    submenu.setVisibility(View.GONE);
    group.addView(submenu);
    for (int i = 0; i < labels.length; i++) {
        final Runnable action = actions[i];
        Button sub = new Button(this);
        sub.setText("›  " + labels[i]);
        sub.setAllCaps(false);
        sub.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        sub.setTextColor(textColor);
        sub.setBackground(roundedBackground(backgroundColor, 10));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sp.setMargins(0, 5, 0, 2);
        submenu.addView(sub, sp);
        sub.setOnClickListener(v -> action.run());
    }
    header.setOnClickListener(v -> submenu.setVisibility(submenu.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
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

    EditText buildingNumber = addNumberInput("رقم المبنى");
    buildingNumber.setText(prefs.getString("company_building_number", ""));

    EditText postalCode = addNumberInput("الرمز البريدي");
    postalCode.setText(prefs.getString("company_postal_code", ""));

    EditText additionalNumber = addNumberInput("الرقم الإضافي");
    additionalNumber.setText(prefs.getString("company_additional_number", ""));

    EditText otherIdType = addInput("نوع المعرّف الآخر (مثل السجل التجاري)");
    otherIdType.setText(prefs.getString("company_other_id_type", ""));

    EditText otherId = addNumberInput("رقم المعرّف الآخر");
    otherId.setInputType(InputType.TYPE_CLASS_NUMBER);
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
            .putString("company_building_number",
                buildingNumber.getText().toString().trim())
            .putString("company_postal_code",
                postalCode.getText().toString().trim())
            .putString("company_additional_number",
                additionalNumber.getText().toString().trim())
            .putString("company_other_id_type",
                otherIdType.getText().toString().trim())
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
            addNumberInput("رقم المعرّف الآخر (اختياري)");
        identifierValue.setInputType(InputType.TYPE_CLASS_NUMBER);

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
    java.util.ArrayList<String> customerLabels = new java.util.ArrayList<>();
    customerLabels.add("اختر العميل");
    for (int i = 0; i < customers.length(); i++) {
        JSONObject c = customers.optJSONObject(i);
        if (c != null) customerLabels.add(c.optString("name") + " - " + c.optString("phone"));
    }

    Spinner customerSpinner = new Spinner(this);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
        android.R.layout.simple_spinner_item, customerLabels);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    customerSpinner.setAdapter(adapter);
    layout.addView(customerSpinner);

    TextView customerDetails = new TextView(this);
    customerDetails.setTextSize(15);
    customerDetails.setTextColor(Color.DKGRAY);
    layout.addView(customerDetails);
    customerSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            JSONObject c = position > 0 ? customers.optJSONObject(position - 1) : null;
            if (c == null) { customerDetails.setText(""); return; }
            customerDetails.setText("الاسم: " + c.optString("name")
                + "\nالجوال: " + c.optString("phone")
                + "\nالعنوان الوطني: " + c.optString("nationalAddress")
                + "\nرقم المبنى: " + c.optString("buildingNumber")
                + "\nالحي: " + c.optString("district")
                + "\nالمدينة: " + c.optString("city")
                + "\nالرمز البريدي: " + c.optString("postalCode")
                + "\nالرقم الإضافي: " + c.optString("additionalNumber")
                + "\nالرقم الضريبي: " + c.optString("taxNumber")
                + "\n" + c.optString("identifierType") + ": " + c.optString("identifierValue"));
        }
        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
    });
    addButton("إضافة عميل جديد", this::showCustomers);

    JSONArray items = readArray("items");
    java.util.ArrayList<String> itemLabels = new java.util.ArrayList<>();
    itemLabels.add("اختر الصنف");
    for (int i = 0; i < items.length(); i++) {
        JSONObject item = items.optJSONObject(i);
        if (item != null) itemLabels.add(item.optString("number") + " - " + item.optString("name"));
    }

    LinearLayout rowsContainer = new LinearLayout(this);
    rowsContainer.setOrientation(LinearLayout.VERTICAL);
    layout.addView(rowsContainer);
    final java.util.ArrayList<Spinner> itemSpinners = new java.util.ArrayList<>();
    final java.util.ArrayList<EditText> quantities = new java.util.ArrayList<>();
    final java.util.ArrayList<CheckBox> taxChecks = new java.util.ArrayList<>();
    final java.util.ArrayList<ArrayAdapter<String>> itemAdapters = new java.util.ArrayList<>();
    TextView totals = new TextView(this);
    totals.setTextSize(17);
    totals.setTextColor(blue);
    layout.addView(totals);

    Runnable calculate = () -> {
        double subtotal = 0, tax = 0;
        for (int i = 0; i < itemSpinners.size(); i++) {
            int pos = itemSpinners.get(i).getSelectedItemPosition();
            JSONObject item = pos > 0 ? items.optJSONObject(pos - 1) : null;
            try {
                double qty = Double.parseDouble(quantities.get(i).getText().toString().trim());
                if (item != null && qty > 0) {
                    double line = item.optDouble("sale") * qty;
                    subtotal += line;
                    if (taxChecks.get(i).isChecked() && item.optBoolean("taxEnabled", true)) tax += line * TAX_RATE;
                }
            } catch (Exception ignored) {}
        }
        totals.setText("الإجمالي قبل الضريبة: " + money(subtotal)
            + " ريال\nالضريبة: " + money(tax) + " ريال\nالإجمالي شامل الضريبة: " + money(subtotal + tax) + " ريال");
    };

    Runnable addItemRow = () -> {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 8, 0, 8);
        Spinner itemSpinner = new Spinner(this);
        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, itemLabels);
        itemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemSpinner.setAdapter(itemAdapter);
        itemAdapters.add(itemAdapter);
        row.addView(itemSpinner);
        EditText qty = addNumberInputTo(row, "الكمية");
        CheckBox taxCheck = new CheckBox(this);
        taxCheck.setText("تطبيق الضريبة 15% على هذا الصنف");
        taxCheck.setChecked(true);
        row.addView(taxCheck);
        Button remove = new Button(this);
        remove.setText("حذف الصنف من المستند");
        row.addView(remove);
        rowsContainer.addView(row);
        itemSpinners.add(itemSpinner);
        quantities.add(qty);
        taxChecks.add(taxCheck);
        itemSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int position, long id) { calculate.run(); }
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        qty.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int count, int after) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) { calculate.run(); }
            public void afterTextChanged(android.text.Editable e) {}
        });
        taxCheck.setOnCheckedChangeListener((b, checked) -> calculate.run());
        remove.setOnClickListener(v -> {
            rowsContainer.removeView(row);
            itemSpinners.remove(itemSpinner);
            itemAdapters.remove(itemAdapter);
            quantities.remove(qty);
            taxChecks.remove(taxCheck);
            calculate.run();
        });
    };

    addButton("＋ إضافة صنف للمستند", addItemRow);
    addButton("＋ إنشاء صنف جديد وحفظه في كرت الأصناف وإضافته للمستند", () -> {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(8, 8, 8, 8);
        EditText numberInput = new EditText(this); numberInput.setHint("رقم الصنف"); form.addView(numberInput);
        EditText nameInput = new EditText(this); nameInput.setHint("اسم الصنف"); form.addView(nameInput);
        EditText priceInput = new EditText(this); priceInput.setHint("سعر البيع قبل الضريبة"); priceInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); form.addView(priceInput);
        EditText costInput = new EditText(this); costInput.setHint("التكلفة (اختياري)"); costInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); form.addView(costInput);
        CheckBox taxable = new CheckBox(this); taxable.setText("تطبيق ضريبة 15%"); taxable.setChecked(true); form.addView(taxable);
        Spinner unit = new Spinner(this); ArrayAdapter<String> ua = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units); ua.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); unit.setAdapter(ua); form.addView(unit);
        new android.app.AlertDialog.Builder(this).setTitle("صنف جديد").setView(form)
            .setNegativeButton("إلغاء", null).setPositiveButton("حفظ وإضافة", (dialog, which) -> {
                String number = numberInput.getText().toString().trim();
                String name = nameInput.getText().toString().trim();
                try {
                    double salePrice = Double.parseDouble(priceInput.getText().toString().trim());
                    double cost = costInput.getText().toString().trim().isEmpty() ? 0 : Double.parseDouble(costInput.getText().toString().trim());
                    if (number.isEmpty() || name.isEmpty() || salePrice < 0 || cost < 0) { toast("أدخل رقم الصنف واسمه وسعرًا صحيحًا"); return; }
                    for (int k=0;k<items.length();k++) { JSONObject existing=items.optJSONObject(k); if(existing!=null && number.equalsIgnoreCase(existing.optString("number"))) { toast("رقم الصنف مسجل مسبقًا"); return; } }
                    JSONObject fresh = new JSONObject();
                    fresh.put("number", number); fresh.put("name", name); fresh.put("unit", unit.getSelectedItem().toString()); fresh.put("sale", salePrice); fresh.put("cost", cost); fresh.put("taxEnabled", taxable.isChecked()); fresh.put("taxRate", taxable.isChecked() ? TAX_RATE : 0); fresh.put("total", taxable.isChecked() ? salePrice * (1 + TAX_RATE) : salePrice);
                    items.put(fresh); saveArray("items", items);
                    itemLabels.add(number + " - " + name);
                    for (ArrayAdapter<String> a : itemAdapters) a.notifyDataSetChanged();
                    int last = itemLabels.size() - 1;
                    if (!itemSpinners.isEmpty()) itemSpinners.get(itemSpinners.size()-1).setSelection(last);
                    toast("تم حفظ الصنف في كرت الأصناف وإضافته للمستند");
                } catch (Exception ex) { toast("تحقق من سعر البيع والتكلفة"); }
            }).show();
    });
    addItemRow.run();
    EditText notes = addInput("ملاحظات (اختياري)");

    addButton("حفظ المستند", () -> {
        int customerPos = customerSpinner.getSelectedItemPosition();
        if (customerPos <= 0) { toast("اختر العميل"); return; }
        JSONObject c = customers.optJSONObject(customerPos - 1);
        if (c == null) { toast("تعذر قراءة بيانات العميل"); return; }

        JSONArray lines = new JSONArray();
        double subtotal = 0, tax = 0;
        for (int i = 0; i < itemSpinners.size(); i++) {
            int pos = itemSpinners.get(i).getSelectedItemPosition();
            JSONObject item = pos > 0 ? items.optJSONObject(pos - 1) : null;
            if (item == null) continue;
            double qty;
            try { qty = Double.parseDouble(quantities.get(i).getText().toString().trim()); }
            catch (Exception e) { toast("أدخل كمية صحيحة لكل صنف"); return; }
            if (qty <= 0) { toast("يجب أن تكون الكمية أكبر من صفر"); return; }
            double lineSubtotal = item.optDouble("sale") * qty;
            boolean applyTax = taxChecks.get(i).isChecked() && item.optBoolean("taxEnabled", true);
            double lineTax = applyTax ? lineSubtotal * TAX_RATE : 0;
            JSONObject line = new JSONObject();
            try {
                line.put("item", item);
                line.put("quantity", qty);
                line.put("subtotal", lineSubtotal);
                line.put("tax", lineTax);
                line.put("total", lineSubtotal + lineTax);
                line.put("taxEnabled", applyTax);
                lines.put(line);
            } catch (Exception ignored) {}
            subtotal += lineSubtotal;
            tax += lineTax;
        }
        if (lines.length() == 0) { toast("أضف صنفًا واحدًا على الأقل"); return; }

        String key = invoice ? "sales_invoices" : "sales_quotes";
        JSONArray documents = readArray(key);
        String prefix = invoice ? "INV-" : "QUO-";
        String number = prefix + String.format(Locale.US, "%06d", documents.length() + 1);
        try {
            JSONObject doc = new JSONObject();
            doc.put("number", number);
            doc.put("date", new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date()));
            doc.put("customer", c);
            doc.put("lines", lines);
            // حقول توافقية للمستندات القديمة وأي بحث يعتمد عليها
            doc.put("item", lines.optJSONObject(0).optJSONObject("item"));
            doc.put("quantity", lines.optJSONObject(0).optDouble("quantity"));
            doc.put("subtotal", subtotal);
            doc.put("tax", tax);
            doc.put("total", subtotal + tax);
            doc.put("notes", notes.getText().toString().trim());
            doc.put("paid", 0);
            doc.put("type", invoice ? "invoice" : "quote");
            documents.put(doc);
            saveArray(key, documents);
            toast("تم حفظ المستند رقم " + number);
            showSalesPreview(doc, invoice);
        } catch (Exception e) { toast("تعذر حفظ المستند"); }
    });
    addButton("العودة لإدارة المبيعات", this::showSales);
}

private EditText addNumberInputTo(LinearLayout parent, String hint) {
    EditText input = new EditText(this);
    input.setHint(hint);
    input.setTextSize(16);
    input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    parent.addView(input, new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    return input;
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
    JSONArray lines = doc.optJSONArray("lines");
    addText(prefs.getString("company_name", "مركز نوره للصيانة")
        + "\nالعنوان الوطني: " + prefs.getString("company_address", "")
        + "\nرقم المبنى: " + prefs.getString("company_building_number", "")
        + "\nالرمز البريدي: " + prefs.getString("company_postal_code", "")
        + "\nالرقم الإضافي: " + prefs.getString("company_additional_number", "")
        + "\nالرقم الضريبي: " + prefs.getString("company_tax", "")
        + "\n" + prefs.getString("company_other_id_type", "المعرّف الآخر")
        + ": " + prefs.getString("company_other_id", "")
        + "\nرقم المستند: " + doc.optString("number")
        + "\nالتاريخ: " + doc.optString("date"));

    if (c != null) {
        addText("بيانات العميل\nالاسم: " + c.optString("name")
            + "\nالجوال: " + c.optString("phone")
            + "\nالعنوان الوطني: " + c.optString("nationalAddress")
            + "\nرقم المبنى: " + c.optString("buildingNumber")
            + "\nالحي: " + c.optString("district")
            + "\nالمدينة: " + c.optString("city")
            + "\nالرمز البريدي: " + c.optString("postalCode")
            + "\nالرقم الإضافي: " + c.optString("additionalNumber")
            + "\nالرقم الضريبي: " + c.optString("taxNumber")
            + "\n" + c.optString("identifierType") + ": " + c.optString("identifierValue"));
    }

    if (lines != null) {
        for (int i = 0; i < lines.length(); i++) {
            JSONObject line = lines.optJSONObject(i);
            if (line == null) continue;
            JSONObject item = line.optJSONObject("item");
            if (item == null) continue;
            addText((i + 1) + ". " + item.optString("name")
                + " (" + item.optString("number") + ")"
                + "\nالكمية: " + money(line.optDouble("quantity"))
                + "\nسعر الوحدة قبل الضريبة: " + money(item.optDouble("sale"))
                + "\nالإجمالي قبل الضريبة: " + money(line.optDouble("subtotal"))
                + "\nالضريبة: " + money(line.optDouble("tax"))
                + "\nإجمالي الصنف: " + money(line.optDouble("total")) + " ريال");
        }
    } else {
        // دعم عرض وطباعة المستندات التي أُنشئت بالنسخة السابقة
        JSONObject item = doc.optJSONObject("item");
        if (item != null) addText("الصنف: " + item.optString("name")
            + "\nالكمية: " + money(doc.optDouble("quantity"))
            + "\nالإجمالي قبل الضريبة: " + money(doc.optDouble("subtotal"))
            + "\nالضريبة: " + money(doc.optDouble("tax")));
    }
    addText("الإجمالي قبل الضريبة: " + money(doc.optDouble("subtotal"))
        + "\nإجمالي الضريبة: " + money(doc.optDouble("tax"))
        + "\nالإجمالي النهائي: " + money(doc.optDouble("total")) + " ريال"
        + "\nملاحظات: " + doc.optString("notes"));

    addButton("طباعة / معاينة الطباعة", () -> {
        android.webkit.WebView webView = new android.webkit.WebView(this);
        StringBuilder html = new StringBuilder();
        html.append("<html><meta charset='UTF-8'><body dir='rtl' style='font-family:Arial,sans-serif;padding:28px;color:#273446;background:#fff'>")
            .append("<div style='border-radius:14px;background:#182c4c;color:#fff;padding:20px;text-align:center'><h1 style='margin:0'>").append(invoice ? "فاتورة مبيعات" : "عرض سعر").append("</h1><p style='margin:8px 0 0'>مركز نوره للصيانة</p></div>")
            .append("<h3>").append(escapeHtml(prefs.getString("company_name", "مركز نوره للصيانة"))).append("</h3>")
            .append("<p>العنوان الوطني: ").append(escapeHtml(prefs.getString("company_address", "")))
            .append("<br>رقم المبنى: ").append(escapeHtml(prefs.getString("company_building_number", "")))
            .append("<br>الرمز البريدي: ").append(escapeHtml(prefs.getString("company_postal_code", "")))
            .append("<br>الرقم الإضافي: ").append(escapeHtml(prefs.getString("company_additional_number", "")))
            .append("<br>الرقم الضريبي: ").append(escapeHtml(prefs.getString("company_tax", "")))
            .append("<br>").append(escapeHtml(prefs.getString("company_other_id_type", "المعرّف الآخر")))
            .append(": ").append(escapeHtml(prefs.getString("company_other_id", ""))).append("</p>")
            .append("<p>رقم المستند: ").append(escapeHtml(doc.optString("number")))
            .append("<br>التاريخ: ").append(escapeHtml(doc.optString("date"))).append("</p>");
        if (c != null) {
            html.append("<h3>بيانات العميل</h3><p>الاسم: ").append(escapeHtml(c.optString("name")))
                .append("<br>الجوال: ").append(escapeHtml(c.optString("phone")))
                .append("<br>العنوان الوطني: ").append(escapeHtml(c.optString("nationalAddress")))
                .append("<br>رقم المبنى: ").append(escapeHtml(c.optString("buildingNumber")))
                .append("<br>الحي: ").append(escapeHtml(c.optString("district")))
                .append("<br>المدينة: ").append(escapeHtml(c.optString("city")))
                .append("<br>الرمز البريدي: ").append(escapeHtml(c.optString("postalCode")))
                .append("<br>الرقم الإضافي: ").append(escapeHtml(c.optString("additionalNumber")))
                .append("<br>الرقم الضريبي: ").append(escapeHtml(c.optString("taxNumber")))
                .append("<br>").append(escapeHtml(c.optString("identifierType")))
                .append(": ").append(escapeHtml(c.optString("identifierValue"))).append("</p>");
        }
        html.append("<table style='width:100%;margin-top:18px;border-collapse:collapse;border:1px solid #dbe3ed' cellpadding='9'><tr style='background:#009688;color:#fff'><th>م</th><th>الصنف</th><th>الكمية</th><th>سعر الوحدة</th><th>قبل الضريبة</th><th>الضريبة</th><th>الإجمالي</th></tr>");
        if (lines != null) {
            for (int i = 0; i < lines.length(); i++) {
                JSONObject line = lines.optJSONObject(i);
                if (line == null) continue;
                JSONObject item = line.optJSONObject("item");
                if (item == null) continue;
                html.append("<tr><td>").append(i + 1).append("</td><td>")
                    .append(escapeHtml(item.optString("name"))).append("</td><td>")
                    .append(money(line.optDouble("quantity"))).append("</td><td>")
                    .append(money(item.optDouble("sale"))).append("</td><td>")
                    .append(money(line.optDouble("subtotal"))).append("</td><td>")
                    .append(money(line.optDouble("tax"))).append("</td><td>")
                    .append(money(line.optDouble("total"))).append("</td></tr>");
            }
        } else {
            JSONObject item = doc.optJSONObject("item");
            if (item != null) html.append("<tr><td>1</td><td>")
                .append(escapeHtml(item.optString("name"))).append("</td><td>")
                .append(money(doc.optDouble("quantity"))).append("</td><td>")
                .append(money(item.optDouble("sale"))).append("</td><td>")
                .append(money(doc.optDouble("subtotal"))).append("</td><td>")
                .append(money(doc.optDouble("tax"))).append("</td><td>")
                .append(money(doc.optDouble("total"))).append("</td></tr>");
        }
        html.append("</table><div style='margin-top:18px;background:#f4f7fb;border-radius:12px;padding:16px'><h3>الإجمالي قبل الضريبة: ").append(money(doc.optDouble("subtotal")))
            .append(" ريال</h3><h3>الضريبة: ").append(money(doc.optDouble("tax")))
            .append(" ريال</h3><h2 style='color:#009688'>الإجمالي النهائي: ").append(money(doc.optDouble("total")))
            .append(" ريال</h2></div><p>ملاحظات: ").append(escapeHtml(doc.optString("notes"))).append("</p><hr><p style='text-align:center;color:#68778a'>شكرًا لثقتكم بمركز نوره للصيانة</p></body></html>");

        webView.loadDataWithBaseURL(null, html.toString(), "text/html", "UTF-8", null);
        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override public void onPageFinished(android.webkit.WebView view, String url) {
                android.print.PrintManager printManager = (android.print.PrintManager) getSystemService(PRINT_SERVICE);
                android.print.PrintDocumentAdapter printAdapter = view.createPrintDocumentAdapter(
                    invoice ? "فاتورة مبيعات" : "عرض سعر");
                printManager.print(doc.optString("number"), printAdapter,
                    new android.print.PrintAttributes.Builder().build());
            }
        });
    });
    addButton("العودة لإدارة المبيعات", this::showSales);
}

private String escapeHtml(String value) {
    if (value == null) return "";
    return value.replace("&", "&amp;").replace("<", "&lt;")
        .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
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
