package com.example.efarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ProductAdapter extends BaseAdapter {

    private Context context;
    private List<Product> products;

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_product, parent, false);
        }

        TextView productNameTextView = convertView.findViewById(R.id.nameTextView);
        TextView productPriceTextView = convertView.findViewById(R.id.priceTextView);
        TextView quantityTextView = convertView.findViewById(R.id.quantityTextView);
        TextView categoryTextView = convertView.findViewById(R.id.categoryTextView);

        Product product = products.get(position);

        productNameTextView.setText(product.getProductName());
        productPriceTextView.setText(String.valueOf(product.getPrice()));
        quantityTextView.setText(String.valueOf(product.getQuantity()));
        categoryTextView.setText(product.getCategory());

        return convertView;
    }
}
