package site.x3la;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
public class Proveedor {
    @SerializedName("id")
    int id;
    double lat;
    double lon;
    String nombre;
    String descripcion;
    String direccion;
    String telefono;
    String email;
    String web;
    String categoria;
    String subcategoria;
    String fsolvente;
}
