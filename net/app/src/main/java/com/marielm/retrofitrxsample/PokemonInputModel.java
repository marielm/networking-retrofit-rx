package com.marielm.retrofitrxsample;

import java.io.Serializable;

public class PokemonInputModel implements Serializable {
    public String label;
    public int pokedexNumber;

    public PokemonInputModel(String name, int number) {
        this.label = name;
        this.pokedexNumber = number;
    }
}
