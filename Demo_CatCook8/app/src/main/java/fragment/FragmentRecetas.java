package fragment;

import static android.content.Intent.getIntent;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListPopupWindow;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo_catcook.ChatBotActivity;
import com.example.demo_catcook.ConnectionClass;
import com.example.demo_catcook.ConnectionSingleton;
import com.example.demo_catcook.MainActivity;
import com.example.demo_catcook.R;
import com.example.demo_catcook.Receta;
import com.example.demo_catcook.RecetaAdapter;
import com.example.demo_catcook.RecetaPersonal;
import com.example.demo_catcook.RecetaPersonalAdapter;
import com.example.demo_catcook.VtCrearCuenta;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FragmentRecetas extends Fragment implements SearchView.OnQueryTextListener{

    ConnectionClass connectionClass;
    SearchView searchView;
    private RecyclerView recyclerView;
    Switch switchRecetas;
    private List<ClipData.Item> itemList;
    private RecetaAdapter adapter, adapterpersonal;
    private List<Receta> recetaList;
    private int idUsuario;
    Spinner spinnerCategoria;
    private Map<Integer, Integer> categoriasMap; // Mapeo de posición a id_categoria
    private String textFiltro = ""; // Filtro de texto del SearchView
    private int idCategoriaSeleccionada = 0;
    private String textoBusqueda = "";
    List<String> categoriasNombres = new ArrayList<>();


    public FragmentRecetas() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idUsuario = getArguments().getInt("id_usuario", 0);
        }

    }
    //Configurar la vista del fragmento recetas
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Intent intent = requireActivity().getIntent();
        String nombre = intent.getStringExtra("nombre");

        Bundle args = getArguments();
        int idUsuario = args.getInt("id_usuario", 0);

        TextView textBienvenido = view.findViewById(R.id.textBienvenido);
        textBienvenido.setText("¡Bienvenid@, " + nombre + "!");


        switchRecetas = view.findViewById(R.id.switchRecetas);

        // Acceder a los elementos de la vista porque ya están inflados
        searchView = view.findViewById(R.id.txtBuscar);
        recyclerView = view.findViewById(R.id.recyclerView);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);

        // Configuración del RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        // Inicializar la lista y el adaptador
        recetaList = new ArrayList<>();
        adapter = new RecetaAdapter(getContext(), recetaList, idUsuario);
        recyclerView.setAdapter(adapter);

        // Cargar categorías en el Spinner
        cargarCategorias();

        //Configurar el switch para cambiar entre tipos de recetas
        switchRecetas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cargarRecetasPublicas();
            } else {
                cargarRecetas();
            }
        });

        searchView.setOnQueryTextListener(this);

        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                idCategoriaSeleccionada = obtenerIdCategoriaDesdePosicion(position);
                aplicarFiltro();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                idCategoriaSeleccionada = 0; // Sin filtro
                aplicarFiltro();
            }
        });

        //Iniciar el metodo para cargar recetas
        cargarRecetas();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recetas, container, false);
        return view;
    }

    private int obtenerIdCategoriaDesdePosicion(int posicion) {
        return categoriasMap.get(posicion); // Mapea la posición al id_categoria
    }


    //Metodo para cargar recetas publicas de usuarios
    private void cargarRecetasPublicas() {
        new Thread(() -> {
            try (Connection con = ConnectionSingleton.getConnection()) {
                String query = "SELECT nombre, descripcion, dificultad, imagen_url, instrucciones, ingredientes, " +
                        "tiempo_preparacion FROM receta_personal WHERE visibilidad = 'publica'";
                PreparedStatement stmt = con.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();

                List<Receta> recetasPublicas = new ArrayList<>();
                Receta receta = new Receta();
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    String descripcion = rs.getString("descripcion");
                    String dificultad = rs.getString("dificultad");
                    String rutaImagen = rs.getString("imagen_url");
                    String instrucciones = rs.getString("instrucciones");
                    String ingredientes= rs.getString("ingredientes");
                    String tiempoPreparacion= rs.getString("tiempo_preparacion");
                    receta.setEsPersonal(true);
                    Log.d("Receta", "nombre: " + receta.getNombre() + ", esPersonal: " +
                            receta.isEsPersonal());
                    adapter.recibirBoolean(true);
                    recetasPublicas.add(new Receta(nombre, dificultad, descripcion, rutaImagen,
                            instrucciones, tiempoPreparacion, ingredientes));
                }


                getActivity().runOnUiThread(() -> {
                    recetaList.clear(); // Limpia la lista actual
                    recetaList.addAll(recetasPublicas); // Agrega las recetas públicas
                    adapter.notifyDataSetChanged();
                });
            } catch (SQLException e) {
                Log.e("FragmentRecetas", "Error al cargar recetas públicas", e);
            }
        }).start();
    }

    //Metodo para cargar recetas predeterminadas de la aplicacion
    private void cargarRecetas() {
        new Thread(() -> {
            Connection con = ConnectionSingleton.getConnection();
            if (con != null) {
                try {
                    Statement statement = con.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT id_receta, nombre, descripcion, " +
                            "dificultad, ruta_imagen, id_categoria FROM receta");
                    recetaList.clear();
                    Receta receta= new Receta();
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id_receta");
                        String nombre = resultSet.getString("nombre");
                        String descripcion = resultSet.getString("descripcion");
                        String dificultad = resultSet.getString("dificultad");
                        String rutaImagen = resultSet.getString("ruta_imagen");
                        int idCategoria = resultSet.getInt("id_categoria");
                        receta.setEsPersonal(false); // Indica que es personal
                        adapter.recibirBoolean(false);
                        recetaList.add(new Receta(id, nombre, dificultad, descripcion, rutaImagen, idCategoria));
                    }

                    requireActivity().runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        adapter.setFullRecetasList(recetaList);
                    });

                    resultSet.close();
                    statement.close();
                    con.close();

                } catch (SQLException e) {
                    Log.e("SQL Error", e.getMessage(), e);
                } finally {
                    try {
                        if (con != null) {
                            con.close();
                        }
                    } catch (SQLException e) {
                        Log.e("Connection Error", e.getMessage());
                    }
                }
            } else {
                Log.e("Connection", "La conexión es nula");
            }
        }).start();
    }

    private void aplicarFiltro() {
        adapter.filtrarRecetas(textoBusqueda, idCategoriaSeleccionada);
    }
    private void cargarCategorias() {
        new Thread(() -> {

            categoriasMap = new HashMap<>();

            categoriasNombres.add("Todas las categorías");
            categoriasMap.put(0, 0); // Posición 0 corresponde al id_categoria 0

            try (Connection con = ConnectionSingleton.getConnection();
                 Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id_categoria, nombre FROM categoria")) {

                while (rs.next()) {
                    int idCategoria = rs.getInt("id_categoria");
                    String nombre = rs.getString("nombre");
                    categoriasNombres.add(nombre);
                    categoriasMap.put(categoriasNombres.size() - 1, idCategoria);
                }

                requireActivity().runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            R.layout.spinner_item, categoriasNombres);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinnerCategoria.setPopupBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                    spinnerCategoria.setAdapter(adapter);
                });

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        textoBusqueda = query;
        aplicarFiltro();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        textoBusqueda = newText;
        aplicarFiltro();
        return true;
    }
}