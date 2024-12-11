package com.anggiiqna.polafit.network

data class ScanResponse(
    val ID: Int,
    val Makanan: String,
    val berat_per_serving_g: String,
    val kalori_kcal: String,
    val protein_g: String,
    val lemak_g: String,
    val karbohidrat_g: String,
    val serat_g: String,
    val gula_g: String
)