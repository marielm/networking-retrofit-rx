package com.marielm.retrofitrxsample;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface PokemonService {

    @GET("/pokemon")
    Call<List<PokemonModel>> getAll();

    @POST("/pokemon")
    Call<ResponseBody> addPokemon(@Body PokemonInputModel input);
}
