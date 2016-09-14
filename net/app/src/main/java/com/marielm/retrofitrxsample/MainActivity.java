package com.marielm.retrofitrxsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
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
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ADD = 0x01;

    @BindView(R.id.parent_view)
    View parent;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.button_add)
    FloatingActionButton addFab;

    private PokemonService service;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)
                    addFab.hide();
                else if (dy < 0)
                    addFab.show();
            }
        });


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
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build();

        service = retrofit.create(PokemonService.class);
    }

    private void showProgress(boolean show) {
        refreshLayout.setRefreshing(show);
    }

    private void getPokemon() {
        subscriptions.add(service.getAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                            showProgress(false);
                            recyclerView.setAdapter(new PokedexAdapter(list));
                        },
                        error -> Log.d(getClass().getSimpleName(), error.getMessage())));
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
        subscriptions.add(service.addPokemon(input)
                .flatMap(post -> {
                    Snackbar.make(parent, "Pokemon Added", Snackbar.LENGTH_SHORT).show();
                    return service.getAll();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(results -> {
                    showProgress(false);
                    recyclerView.setAdapter(new PokedexAdapter(results));
                }));
    }

    @Override
    protected void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
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
            @BindView(R.id.name)
            TextView name;
            @BindView(R.id.pokedex_number)
            TextView number;
            @BindView(R.id.sprite)
            ImageView sprite;

            public PokedexViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
