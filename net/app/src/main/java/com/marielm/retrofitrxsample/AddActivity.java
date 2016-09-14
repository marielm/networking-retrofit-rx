package com.marielm.retrofitrxsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddActivity extends AppCompatActivity {

    @BindView(R.id.add) Button addButton;
    @BindView(R.id.pokemon_name) EditText nameInput;
    @BindView(R.id.pokemon_number) EditText numberInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        ButterKnife.bind(this);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PokemonInputModel input = new PokemonInputModel(nameInput.getText().toString(), Integer.valueOf(numberInput.getText().toString()));

                Intent data = new Intent().putExtra("pokemon_input", input);

                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
    }
}
