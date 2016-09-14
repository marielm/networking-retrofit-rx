package com.marielm.retrofitrxsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ADD = 0x01;

    @BindView(R.id.parent_view) View parent;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.button_add) FloatingActionButton addFab;

    private PokemonService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupRetrofit();
        getPokemon();

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, AddActivity.class), REQUEST_ADD);
            }
        });

        refreshLayout.setColorSchemeColors(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showProgress(true);
                getPokemon();
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

    private void showProgress(boolean show) {
        refreshLayout.setRefreshing(show);
    }

    private void getPokemon() {
        service.getAll().enqueue(new Callback<List<PokemonModel>>() {
            @Override
            public void onResponse(Call<List<PokemonModel>> call, Response<List<PokemonModel>> response) {
                showProgress(false);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD && resultCode == Activity.RESULT_OK) {
            PokemonInputModel input = (PokemonInputModel) data.getExtras().getSerializable("pokemon_input");

            showProgress(true);
            handleAdd(input);
        }
    }

    private void handleAdd(PokemonInputModel input) {
        service.addPokemon(input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Snackbar.make(parent,  "Pokemon Added", Snackbar.LENGTH_SHORT).show();

                // this should be calling getPokemon() as we have now written it twice
                // leavin as is to show chaining
                service.getAll().enqueue(new Callback<List<PokemonModel>>() {
                    @Override
                    public void onResponse(Call<List<PokemonModel>> call, Response<List<PokemonModel>> response) {
                        showProgress(false);

                        final List<PokemonModel> results = response.body();

                        if (response.isSuccessful() && results.size() > 0) {
                            recyclerView.setAdapter(new PokedexAdapter(results));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PokemonModel>> call, Throwable t) {
                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
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
