package com.marielm.retrofitrxsample;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PokemonService {

    @GET("/")
    Call<List<PokemonModel>> getAll();
}
