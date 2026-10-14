
package com.noura.workshop;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private int blue = Color.rgb(25, 95, 190);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 32, 24, 24);
        layout.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        layout.setBackgroundColor(Color.rgb(245, 248, 252));
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView title = new TextView(this);
        title.setText("مركز نوره للصيانة");
        title.setTextSize(28);
        title.setTextColor(blue);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 12);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("إدارة العملاء وأعمال الصيانة والفواتير");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 28);
        layout.addView(subtitle);

        addButton(layout, "إدارة العملاء");
        addButton(layout, "طلبات الصيانة");
        addButton(layout, "الفواتير والمدفوعات");
        addButton(layout, "عروض الأسعار");

        TextView note = new TextView(this);
        note.setText("مرحبًا بك في نظام إدارة الورشة");
        note.setTextSize(14);
        note.setTextColor(Color.GRAY);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, 28, 0, 0);
        layout.addView(note);

        setContentView(layout);
    }

    private void addButton(LinearLayout layout, String text) {
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
    }
}
