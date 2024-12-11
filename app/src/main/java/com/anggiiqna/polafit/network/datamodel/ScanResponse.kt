package com.anggiiqna.polafit.network

data class ScanResponse(
    val ID: Int,
    val Makanan: String,
    val berat_per_serving_g: String,
    val Kalori_kcal: String,
    val Protein_g: String,
    val Lemak_g: String,
    val Karbohidrat_g: String,
    val Serat_g: String,
    val Gula_g: String
)
