package com.marielm.retrofitrxsample;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

public interface PokemonService {

    @GET("/pokemon") Observable<List<PokemonModel>> getAll();

    @POST("/pokemon") Observable<ResponseBody> addPokemon(@Body PokemonInputModel input);
}
