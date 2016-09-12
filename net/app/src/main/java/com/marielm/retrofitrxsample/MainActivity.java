package com.marielm.retrofitrxsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    private PokemonService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupRetrofit();

        service.getAll().enqueue(new Callback<List<PokemonModel>>() {
            @Override
            public void onResponse(Call<List<PokemonModel>> call, Response<List<PokemonModel>> response) {
                final List<PokemonModel> results = response.body();

                if (response.isSuccessful() && results.size() > 0) {
                    recyclerView.setAdapter(new PokedexAdapter(results));
                }
            }

            @Override
            public void onFailure(Call<List<PokemonModel>> call, Throwable t) {
                Log.d(getClass().getSimpleName(), t.getMessage());
            }
        });
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://retrofit-rx-sample.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(PokemonService.class);
    }

    class PokedexAdapter extends RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder> {
        private List<PokemonModel> data;

        public PokedexAdapter(List<PokemonModel> data) {
            this.data = data;
        }

        @Override
        public PokedexViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PokedexViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.pokedex_row, parent, false));
        }

        @Override
        public void onBindViewHolder(PokedexViewHolder holder, int position) {
            PokemonModel pokemon = data.get(position);

            holder.name.setText(pokemon.label);
            holder.number.setText("#" + pokemon.pokedexNumber);

            Picasso.with(MainActivity.this).load(pokemon.spriteUrl).into(holder.sprite);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class PokedexViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.name) TextView name;
            @BindView(R.id.pokedex_number) TextView number;
            @BindView(R.id.sprite) ImageView sprite;

            public PokedexViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
