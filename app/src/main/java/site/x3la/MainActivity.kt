package site.x3la

import android.Manifest
import android.app.ProgressDialog
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import site.x3la.databinding.ActivityMainBinding
import java.sql.Date
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Arrays


class MainActivity : AppCompatActivity(), OnMapReadyCallback,InfoWindowAdapter {



    //private lateinit var signInRequest: BeginSignInRequest
    private lateinit var mWindow: View
    private lateinit var mContents: View
    lateinit var mAdView : AdView


    /** Demonstrates customizing the info window and/or its contents.  */
    open fun CustomInfoWindowAdapter() {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Variables de la actividad


    //variables de inicio de sesión
    //private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mMap: GoogleMap

    //variables del servicio de ubicacion
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mService: LocationUpdatesService? = null;
    private var broadcastReceiver: BroadcastReceiver? = null
    private var mBound: Boolean = false

    //variables de funcionamiento de la actividad
    private var ubicacionActual : LatLng? = null

    var progressDialog: ProgressDialog? = null
    var progressDialog2: ProgressDialog? = null


    //constantes
    private val MY_PERMISSIONS_REQUEST_LOGIN = 67
    private val MY_PERMISSIONS_REQUEST_LOCATION = 68
    private val MY_PERMISSIONS_REQUEST_INTERNET = 69
    private val REQUEST_CHECK_SETTINGS = 129
    private var permisosOtorgados: Boolean = false
    private lateinit var googleMapT : GoogleMap
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLocation: Location
    private val permsRequestCode = MY_PERMISSIONS_REQUEST_LOCATION
    private val perms = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.INTERNET
    )
    private val ENDPOINT = "https://x3la.site/wp-json/wp/v2/c197p"
    private var Proveedores: List<Proveedor> = emptyList()
    private lateinit var gson: Gson
    private var primerUso= true
    private lateinit var autoTextViewNom: AutoCompleteTextView
    private lateinit var adapterNom: ArrayAdapter<String>
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var fBoton: FloatingActionButton
    private lateinit var fBoton2: FloatingActionButton



    /*private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, MY_PERMISSIONS_REQUEST_LOGIN)
    }*/

    private lateinit var analytics: FirebaseAnalytics



    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)

            setContentView(binding.root)
            analytics = Firebase.analytics
            auth = Firebase.auth

            MobileAds.initialize(this) {}

            mAdView = findViewById(R.id.adView)
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)


            // Configure Google Sign In
            /*val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("80788162080-rip6phbnbu9u4uofuskuuj0d9s07l198.apps.googleusercontent.com")
                .requestEmail()
                .build()*/



            //googleSignInClient = GoogleSignIn.getClient(this, gso)


            progressDialog = ProgressDialog(this@MainActivity)
            progressDialog!!.setMessage("Descargando...")
            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            progressDialog!!.setCancelable(false)

            progressDialog!!.show()
            fBoton = findViewById(R.id.floatingActionButton4)
            fBoton.setOnClickListener(View.OnClickListener { //Get data from input field
                val intent = Intent(this@MainActivity, Info::class.java)
                startActivity(intent)
            })

            fBoton2 = findViewById(R.id.floatingActionButton5)
            fBoton2.setOnClickListener(View.OnClickListener { //Get data from input field
                //Toast.makeText(applicationContext, "Cargando, porfavor espera...", Toast.LENGTH_LONG).show();
                if (ubicacionActual == (null)){
                    val posicionQuetzaltenango = LatLng(14.8347200,-91.5180600)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionQuetzaltenango, 17f))
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual!!, 17f))
                }

                dibujarProveedores(Proveedores)
                hideKeyboard()
            })

            val subcategoriasArray = resources.getStringArray(R.array.subcategorias_array)
            val autoTextViewSub: AutoCompleteTextView = findViewById(R.id.autoCompleteTextViewSubcategoria)
            val adapterSub: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.select_dialog_item, subcategoriasArray)
            autoTextViewSub.threshold = 0
            autoTextViewSub.setAdapter(adapterSub)
            autoTextViewNom = findViewById(R.id.autoCompleteTextViewNombre)





            mWindow = layoutInflater.inflate(R.layout.custom_info_window, null)
            mContents = layoutInflater.inflate(R.layout.custom_info_contents, null)

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(perms, permsRequestCode)
            }

            val gsonBuilder = GsonBuilder()
            gson = gsonBuilder.create()
            val requestQueue = Volley.newRequestQueue(applicationContext)
            val request = StringRequest(Request.Method.GET, ENDPOINT, onPostsLoaded, onPostsError)
            request.setShouldCache(false)
            requestQueue.add(request)


            autoTextViewSub.setOnItemClickListener { _, _, position, _ ->
                // You can get the label or item that the user clicked:
                val value = adapterSub.getItem(position) ?: ""
                var proveedoresSubcate: MutableList<Proveedor> = mutableListOf()
                for (proveedor in Proveedores){
                    val subcategoria: String = java.lang.String.valueOf(proveedor.subcategoria)
                    if (subcategoria.equals(value)){
                        proveedoresSubcate.add(proveedor)
                        //Toast.makeText(this, "Proveedor encontrado como subcatergoria", Toast.LENGTH_LONG).show();
                    }

                }
                var current : String
                val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    current = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } else {
                    current = "2023-04-20"
                }

                val parameters = Bundle().apply {

                    this.putString("fecha_log", current)
                    this.putString("busqueda_sub", value)

                }
                analytics.logEvent("BusquedaSub", parameters)
                hideKeyboard()

                if (ubicacionActual!!.equals(null)){
                    val posicionQuetzaltenango = LatLng(14.8347200,-91.5180600)

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionQuetzaltenango, 15f))
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual!!, 15f))

                }


                dibujarProveedores(proveedoresSubcate)
                autoTextViewSub.setText("")
                autoTextViewNom.setText("")


            }

            autoTextViewNom.setOnItemClickListener { _, _, position, _ ->
                // You can get the label or item that the user clicked:
                val value = adapterNom.getItem(position) ?: ""
                var proveedoresNom: MutableList<Proveedor> = mutableListOf()
                for (proveedor in Proveedores){
                    val nombre: String = java.lang.String.valueOf(proveedor.nombre)
                    if (nombre.equals(value)){
                        proveedoresNom.add(proveedor)
                        //Toast.makeText(this, "Proveedor encontrado como subcatergoria", Toast.LENGTH_LONG).show();
                    }
                }
                val ubicacionP = LatLng(proveedoresNom.elementAt(0).lat,proveedoresNom.elementAt(0).lon )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( ubicacionP , 19f))

                var current : String
                val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    current = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } else {
                    current = "2023-04-20"
                }

                val parameters = Bundle().apply {

                    this.putString("fecha_log", current)
                    this.putString("busqueda_sub", value)
                }
                analytics.logEvent("BusquedaNom", parameters)

                hideKeyboard()

                dibujarProveedores(proveedoresNom)
                autoTextViewSub.setText("")
                autoTextViewNom.setText("")
            }

            //signIn()

            Toast.makeText(this, "Cargando, porfavor espera unos segundos...", Toast.LENGTH_LONG).show();
        } catch (e: Exception){
            Log.e("ERROR1:",e.toString())
        }


    }

    fun AppCompatActivity.hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }






    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Permisos
    //Que hacer con el resultado de la solicitud de permisos, si no se dan la app no deberia funcionar
    //Inicializar el servicio de ubicación, el cliente Fused y el mapa
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.e("MainActivity:","Location Permission Granted")
                    if (getLocationMode() == 3) {
                        Log.e("MainActivity:","Already set High Accuracy Mode")
                    } else {
                        Log.e("MainActivity:","Alert Dialog Shown")
                        showAlertDialog(this@MainActivity)
                    }
                    // este codigo se ejecuta cuando la app se instala y recibe permisos por primera vez
                    permisosOtorgados = true
                    initializeService()
                    getLastLocation()


                } else {
                    permisosOtorgados = false
                    Toast.makeText(applicationContext,"Sin permisos de ubicación.  La app no se centrara en su ubicación",Toast.LENGTH_SHORT).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(perms, permsRequestCode)

                    }
                }
                if (Proveedores == null){
                    this.recreate()
                } else {
                    dibujarPantalla()
                }

                return
            }

            MY_PERMISSIONS_REQUEST_INTERNET -> {
                if ((grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    Log.e("MainActivity:","Internet Permission Granted")
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(applicationContext,"Sin permiso de internet. La app no mostrara el mapa.",Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun initializeService(){
        bindService(Intent(this, LocationUpdatesService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)

        // Se ha aprobado el permiso, puedes hacer lo que quieras hacer
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //servicio de ubicacion

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    //Se obtiene la ubicacion del usuario y el mapa se centra en su posicion incialmente
    private fun getLastLocation() {
        if (permisosOtorgados) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(perms, permsRequestCode)
                    }

                    return
                }
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        //findViewById<TextView>(R.id.latTextView).text = location.latitude.toString()
                        //findViewById<TextView>(R.id.lonTextView).text = location.longitude.toString()
                        val  cadenaCoor: String? = location.latitude.toString() + location.longitude.toString()
                        //Toast.makeText(this, "Coordenadas: "+cadenaCoor , Toast.LENGTH_LONG).show()
                        var posicionActual = LatLng(location.latitude, location.longitude)
                        ubicacionActual = posicionActual


                        ////////////////////////
                        posicionActual = LatLng(14.8347200,-91.5180600)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual!!, 17f))
                        mMap.addMarker(
                            MarkerOptions()
                                .position(ubicacionActual!!)
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.posicion_actual))
                                .title("Estas aquí ").snippet(ubicacionActual!!.latitude.toString() +" , " + ubicacionActual!!.longitude.toString() )
                        )


                    }
                }

            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(perms, permsRequestCode)
            }
        }

    }

    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation!!
            ubicacionActual = LatLng(mLastLocation.latitude, mLastLocation.longitude)
        }
    }

    //solicitar activar la ubicacion del dispositivo
    private fun showAlertDialog(context: Context?) {
        try {
            context?.let {
                val builder = AlertDialog.Builder(it)
                builder.setTitle(it.resources.getString(R.string.app_name))
                    .setMessage("Please select High accuracy Location Mode from Mode Settings")
                    .setPositiveButton(it.resources.getString(android.R.string.ok)) { dialog, which ->
                        dialog.dismiss()
                        startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CHECK_SETTINGS)
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLocationMode(): Int {
        return Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE)
    }

    // Monitors the state of the connection to the service.
    private var mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: LocationUpdatesService.LocalBinder = service as LocationUpdatesService.LocalBinder
            mService = binder.service
            mBound = true
            // Check that the user hasn't revoked permissions by going to Settings.

            mService!!.requestLocationUpdates()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            mBound = false
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //mapa de google

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        ContextCompat.getDrawable(context, vectorResId)?.let { vectorDrawable ->
            vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
            val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
        return null
    }



    private fun processingBitmap(fuente: Bitmap, texto: String, tamañoTexto: Int): Bitmap? {
        var bm1: Bitmap? = null
        var newBitmap: Bitmap? = null
        bm1 = fuente
        var config = bm1.config
        if (config == null) {
            config = Bitmap.Config.ARGB_8888
        }
        newBitmap = Bitmap.createBitmap(bm1.width, bm1.height, config)
        val newCanvas = Canvas(newBitmap)
        newCanvas.drawBitmap(bm1, 0f, 0f, null)
        if (texto != null) {
            val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
            paintText.color = Color.WHITE
            paintText.textSize = tamañoTexto.toFloat()
            paintText.style = Paint.Style.FILL_AND_STROKE
            paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val rectText = Rect()
            paintText.getTextBounds(texto, 0, texto.length, rectText)
            val largoTexto = paintText.measureText(texto).toInt()
            val xPos = (newCanvas.width - largoTexto) / 2
            val yPos =
                (newCanvas.height / 2 - (paintText.descent() + paintText.ascent()) / 2).toInt()
            newCanvas.drawText(texto, xPos.toFloat(), yPos.toFloat(), paintText)
        } else {
        }
        return newBitmap
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //funciones generales de la actividad

    //override el onActivityResult para que haga lo que se desea
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            MY_PERMISSIONS_REQUEST_LOGIN-> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    // Google Sign In was successful, authenticate with Firebase
                    if (task.equals(null)){
                        Log.w(TAG, "Task null------------------------------------Google sign in failed")
                    } else {
                        val account = task.getResult(ApiException::class.java)!!
                        Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                        firebaseAuthWithGoogle(account.idToken!!)
                        //Toast.makeText(applicationContext,"Google Sign In was successful, authenticate with Firebase",Toast.LENGTH_LONG).show()
                    }
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e)
                }
            }

            REQUEST_CHECK_SETTINGS -> {
                try {
                    initializeService()

                } catch (_: Exception) {

                }
            }
        }
    }



    private fun firebaseAuthWithGoogle(idToken: String) {
        lateinit var user: FirebaseUser
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    user = auth.currentUser!!
                    //updateUI(user)
                    if (user.equals(null)) {
                        Toast.makeText(applicationContext,"Usuario nulo",Toast.LENGTH_SHORT).show()
                        progressDialog!!.dismiss()

                    } else {
                        Toast.makeText(applicationContext,"Bienvenid@ "+user.email,Toast.LENGTH_SHORT).show()
                        var current : String
                        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            current = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        } else {
                            current = "2023-04-20"
                        }

                        val parameters = Bundle().apply {
                            if (user == null){
                                this.putString("usuario", user.email)
                            } else {
                                this.putString("usuario", "Sin Usuario")
                            }
                            this.putString("fecha_log", current)
                        }
                        analytics.logEvent("LogIn", parameters)
                        progressDialog!!.dismiss()

                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    //updateUI(null)
                    Toast.makeText(applicationContext,"La autenticación fallo...",Toast.LENGTH_LONG).show()

                }
            }
    }


    //Funciones estandar de la actividad
    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction("NotifyUser")
        broadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(it, intentFilter)
        }
    }

    override fun onPause() {
        broadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
        }
        super.onPause()
    }

    override fun onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection)
            mBound = false
        }
        super.onStop()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.clear()
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        val posicionActual = LatLng(14.8347200,-91.5180600)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionActual!!, 19f))




        mMap.setOnInfoWindowClickListener(OnInfoWindowClickListener { marker ->
            //Toast.makeText(applicationContext,marker.title,Toast.LENGTH_LONG).show()
            for (proveedor in Proveedores){
                val nombre: String = java.lang.String.valueOf(proveedor.nombre)
                if (nombre.equals(marker.title)){
                    var id : Int = Integer.valueOf(proveedor.id)
                    var lat: Double = java.lang.Double.valueOf(proveedor.lat)
                    var lon: Double = java.lang.Double.valueOf(proveedor.lon)
                    var nombre: String = java.lang.String.valueOf(proveedor.nombre)
                    var descripcion: String = java.lang.String.valueOf(proveedor.descripcion)
                    var direccion: String = java.lang.String.valueOf(proveedor.direccion)
                    var telefono : String = java.lang.String.valueOf(proveedor.telefono)
                    var email: String = java.lang.String.valueOf(proveedor.email)
                    var web: String = java.lang.String.valueOf(proveedor.web)
                    var categoria: String = java.lang.String.valueOf(proveedor.categoria)
                    var subcategoria: String = java.lang.String.valueOf(proveedor.subcategoria)
                    var fsolvente: String = java.lang.String.valueOf(proveedor.fsolvente)
                    //Pass data to 2nd activity
                    //Pass data to 2nd activity
                    val intent = Intent(this@MainActivity, VerProveedor::class.java)
                    intent.putExtra("nombre", nombre)
                    intent.putExtra("direccion", direccion)
                    intent.putExtra("telefono", telefono)
                    intent.putExtra("email", email)
                    intent.putExtra("web", web)
                    intent.putExtra("descripcion", descripcion)
                    startActivity(intent)
                    break
                }
            }
        })
    }

    private val onPostsLoaded: Response.Listener<String?> = object : Response.Listener<String?> {
        override fun onResponse(response: String?) {
            val respuesta = response
            if (response != null) {
                Log.i("CasosPaises", response)
                Proveedores = Arrays.asList(
                    *gson.fromJson<Array<Proveedor>>(
                        respuesta,
                        Array<Proveedor>::class.java
                    )
                )

                dibujarProveedores(Proveedores)
            } else {
                Toast.makeText(applicationContext,"No se puedo establecer conexion con el servidor, intente mas tarde.",Toast.LENGTH_LONG).show()

            }

        }
    }

    private val onPostsError: Response.ErrorListener = object : Response.ErrorListener {
        override fun onErrorResponse(error: VolleyError) {
            Log.e("PostActivity", error.toString())
        }
    }



    fun dibujarPantalla() {
        if (mMap != null) {
            mMap.clear()
            if (primerUso) {
                primerUso = false
            } else {
            }
        }

        dibujarProveedores(Proveedores)

    }






    fun dibujarProveedores( proveedoresDibujar : List<Proveedor>){

        runOnUiThread {
            mMap.clear()

            if (ubicacionActual != null){
                mMap.addMarker(
                    MarkerOptions()
                        .position(ubicacionActual!!)
                        .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.posicion_actual))
                        .title("Estas aquí ").snippet(ubicacionActual!!.latitude.toString() +" , " + ubicacionActual!!.longitude.toString() )
                )
            }



            val nombresArray: MutableList<String> = mutableListOf()

            for (proveedor in Proveedores) {

                var nombre: String = java.lang.String.valueOf(proveedor.nombre)
                var fsolvente: String = java.lang.String.valueOf(proveedor.fsolvente)
                var current : String
                val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    current = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } else {
                    current = "2023-04-20"
                }
                val solvencia = Date.valueOf(fsolvente)
                if (current <= solvencia.toString()){
                    nombresArray.add(nombre)
                }
            }

            for (proveedor in proveedoresDibujar) {
                var id : Int = Integer.valueOf(proveedor.id)
                var lat: Double = java.lang.Double.valueOf(proveedor.lat)
                var lon: Double = java.lang.Double.valueOf(proveedor.lon)
                var nombre: String = java.lang.String.valueOf(proveedor.nombre)
                var descripcion: String = java.lang.String.valueOf(proveedor.descripcion)
                var direccion: String = java.lang.String.valueOf(proveedor.direccion)
                var telefono : String = java.lang.String.valueOf(proveedor.telefono)
                var email: String = java.lang.String.valueOf(proveedor.email)
                var web: String = java.lang.String.valueOf(proveedor.web)
                var categoria: String = java.lang.String.valueOf(proveedor.categoria)
                var subcategoria: String = java.lang.String.valueOf(proveedor.subcategoria)
                var fsolvente: String = java.lang.String.valueOf(proveedor.fsolvente)

                var current : String
                val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    current = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } else {
                    current = "2023-04-20"

                }

                val solvencia = Date.valueOf(fsolvente)
                val solventeT = true

                if (current <= solvencia.toString()){



                    //añadir el procedimiento para configurar marcadores
                    val tamaño1 = 0
                    var tamaño = 0

                    tamaño = tamaño1 + 40
                    if (telefono == "null"){
                        telefono = " - "
                    }

                    //val bitmap = BitmapFactory.decodeResource(resources, R.drawable.otro)



                    /*val bitmap = BitmapFactory.decodeResource(resources, R.drawable.otro)
                    val markerOptions = MarkerOptions()
                        .position(LatLng(lat,lon))
                        .title(nombre)
                        .snippet(telefono)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    mMap.addMarker(markerOptions)*/




                    if (subcategoria.equals("Colegio")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.colegios))
                                .title(nombre).snippet(telefono)
                        )
                    }else if (subcategoria.equals("Libreria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.librerias))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Universidad")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.universidades))
                                .title(nombre).snippet(telefono)
                        )
                    }
                    else if (subcategoria.equals("Instituto")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.institutos))
                                .title(nombre).snippet(telefono)
                        )
                    }  else if (subcategoria.equals("Escuela")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.escuela))
                                .title(nombre).snippet(telefono)
                        )
                    }  else if (subcategoria.equals("Imprenta")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.imprenta))
                                .title(nombre).snippet(telefono)
                        )
                    }  else if (subcategoria.equals("Fotocopiadora")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.fotocopiadora))
                                .title(nombre).snippet(telefono)
                        )
                    }  else if (subcategoria.equals("Internet")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.internet))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Profesor Particular")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.profesor_particular))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Lugar Turistico")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.lugar_turistico))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Balneario")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.balneario))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Escuela de Idiomas")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.escuelaidiomas))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Hotel")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.hoteles))
                                .title(nombre).snippet(telefono)
                        )
                    }else if (subcategoria.equals("Restaurante")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.restaurant))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Cafeteria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.cafeteria))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Parada")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.parada))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Taxi")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.taxi))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Deposito de Licores")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.deposito_licores))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Mecanico")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.mecanico))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Electromecanico")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.electromecanico))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Enderezado y Pintura")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.enderezado_pintura))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Compra y Venta Autos")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.compra_venta_carros))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Insumos Automotrices")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.insumos_carros))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Repuestos")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.repuestos_carros))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Taller de Motos")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.taller_motos))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Taller de Bicicletas")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.taller_bicicletas))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Llantera")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.llantera))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Pinchazo")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.pinchazo))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Tapiceria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.tapiceria))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Medico")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.medico_general))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Pediatra")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.pediatra))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Oftalmologo")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.oftalmologo))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Dermatologo")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.dermatologo))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Cardiologo")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.cardiologo))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Odontologo")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.odontologo))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Enfermera")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.enfermera))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Farmacia")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.farmacia))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Hospital")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.hospital))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Psicologo")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.psicologo))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Laboratorio Medico")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.laboratorio_medico))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Optica")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.optica))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Spa")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.spa))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Estilista")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.estilista))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Uñas Acrilicas")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.unas_acrilicas))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Billar")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.billar))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Discoteca")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.disco))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Bar")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.bar))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Juegos")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.juegos))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Cancha Sintetica")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.foot))
                                .title(nombre).snippet(telefono)
                        )
                    }else if (subcategoria.equals("Flete")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.flete))
                                .title(nombre).snippet(telefono)
                        )
                    }else if (subcategoria.equals("Madera")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.madera))
                                .title(nombre).snippet(telefono)
                        )
                    }else if (subcategoria.equals("Camion Agua")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.camion_agua))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Albañil")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.albanil))
                                .title(nombre).snippet(telefono)
                        )
                    }else if (subcategoria.equals("Ingeniero")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.ingeniero))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Arquitecto")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.arquitecto))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Material Construccion")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.material_construccion))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Material Electrico")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.material_electrico))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Ferreteria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.ferreteria))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Bloquera")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.bloquera))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Agregados")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.agregados))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Electricista")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.electricista))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Fontanero")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.fontanero))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Herrero")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.herrero))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("PVC")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.pvc))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Pisos")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.piso))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Proveedor Agua")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.proveedor_agua))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Carpintero")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.carpintero))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Proveedor Electricidad")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.proveedor_electricidad))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Centro de Convenciones")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.centro_convenciones))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Eventos Especiales")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.eventos_especiales))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Alquifiestas")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.alquifiestas))
                                .title(nombre).snippet(telefono)
                        )
                    }  else if (subcategoria.equals("Transmision")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.transmision))
                                .title(nombre).snippet(telefono)
                        )
                    }  else if (subcategoria.equals("Restaurante")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.restaurant))
                                .title(nombre).snippet(telefono)
                        )
                    }  else if (subcategoria.equals("Cafeteria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.cafeteria))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Parada")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.parada))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Taxi")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.taxi))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Deposito de Licores")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.deposito_licores))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Banco")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.banco))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Cooperativa")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.cooperativa))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Casa de Empeño")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.casa_empeno))
                                .title(nombre).snippet(telefono)
                        )
                    }  else if (subcategoria.equals("Supermercado")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.supermercado))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Zapateria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.zapateria))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Funeraria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.funeraria))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Sastreria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.sastre))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Electrodomesticos")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.electrodomestico))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Telas")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.tela))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Hilos")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.hilo))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Marmoleria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.marmoleria))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Vitrales")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.vitral))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Peleteria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.peleteria))
                                .title(nombre).snippet(telefono)
                        )
                    }  else if (subcategoria.equals("Floristeria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.floristeria))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Abogado")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.abogado))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Contable")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.contable))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Tramite")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.tramite))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Tecnico")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.tecnico))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Muebles")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.muebles))
                                .title(nombre).snippet(telefono)
                        )
                    }else if (subcategoria.equals("Software")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.software))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Computadoras")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.computadoras))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Celulares")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.celulares))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Television")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.televisores))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Ministerio")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.ministerios))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Municipalidad")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.municipalidades))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Policia")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.policia))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Bomberos")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.bombero))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Ministerio Publico")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.ministerio_publico))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Organismo Judicial")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.organismo_judicial))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Cementerio")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.cementerio))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Iglesia")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.iglesia))
                                .title(nombre).snippet(telefono)
                        )
                    }else if (subcategoria.equals("Carniceria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.carniceria))
                                .title(nombre).snippet(telefono)
                        )
                    }    else if (subcategoria.equals("Nutricionista")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.nutricionista))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Panaderia")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.panaderia))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Pollo Frito")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.pollo_frito))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Hamburguesas")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.hamburguesas))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Pizza")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.pizza))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Tacos")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.tacos))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Cine")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.cine))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Helados")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.helados))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Paquetes")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.paquetes))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("CarWash")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.carwash))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Parqueo")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.parqueo))
                                .title(nombre).snippet(telefono)
                        )
                    }
                    else if (subcategoria.equals("Gasolinera")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.gasolinera))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Teneria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.teneria))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Centro Comercial")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.centro_comercial))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Joyeria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.joyeria))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Artesania")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.artesania))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Acuario")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.acuario))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Aeropuerto")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.aeropuerto))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Agencia de seguros")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.agencia_de_seguros))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Agencia de viajes")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.agencia_de_viajes))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Agencia inmobiliaria")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.agencia_inmobiliaria))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Alquiler de peliculas")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.alquiler_de_peliculas))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Biblioteca")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.biblioteca))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Bolera")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.bolera))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Cajero automatico")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.cajero_automatico))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Camping")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.camping))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Casino")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.casino))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Cerrajero")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.cerrajero))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Embajada")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.embajada))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Estacion de autobuses")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.estacion_de_autobuses))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Estadio")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.estadio))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Fisioterapeuta")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.fisioterapeuta))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Galeria de Arte")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.galeria_de_arte))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Gimnasio")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.gimnasio))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Lavanderia")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.lavanderia))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Pintor")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.pintor))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Ropa")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.ropa))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Techos")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.techos))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Tienda de bicicletas")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.tienda_de_bicicletas))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Tienda de conveniencia")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.tienda_de_conveniencia))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Tienda de mascotas")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.tienda_de_mascotas))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Veterinario")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.veterinario))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Club nocturno")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.bar))
                                .title(nombre).snippet(telefono)
                        )
                    } else if (subcategoria.equals("Mudanzas")){
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.flete))
                                .title(nombre).snippet(telefono)
                        )
                    }else {
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat,lon))
                                .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.otro))
                                .title(nombre).snippet(telefono)
                        )
                    }

                }



            }
            adapterNom = ArrayAdapter<String>(this, android.R.layout.select_dialog_item, nombresArray)
            autoTextViewNom.threshold = 1
            autoTextViewNom.setAdapter(adapterNom)

        }
        //Toast.makeText(this, "Carga completa, disfruta el mapa!", Toast.LENGTH_LONG).show();


        progressDialog!!.dismiss()

    }

    override fun getInfoContents(p0: Marker): View? {
        TODO("Not yet implemented")
    }





    override fun getInfoWindow(p0: Marker): View? {

        for (proveedor in Proveedores){
            if (p0.title.equals(proveedor.nombre)){
                render(p0, mWindow, proveedor.telefono);
            }
        }
        return mWindow;
    }


    private fun render(marker: Marker, view: View, subcate:String) {
        val badge: Int
        // Use the equals() method on a Marker to check for equals.  Do not use ==.
        if (subcate.equals("Colegio")){
            badge = R.drawable.colegios
        } else {
            badge = R.drawable.posicion_actual
        }
        (view.findViewById<View>(R.id.badge) as ImageView).setImageResource(badge)
        val title = marker.title
        val titleUi = view.findViewById<View>(R.id.title) as TextView
        if (title != null) {
            // Spannable string allows us to edit the formatting of the text.
            val titleText = SpannableString(title)
            titleText.setSpan(ForegroundColorSpan(Color.BLUE), 0, titleText.length, 0)
            titleUi.text = titleText
        } else {
            titleUi.text = ""
        }
        val snippet = marker.snippet
        val snippetUi = view.findViewById<View>(R.id.snippet) as TextView
        if (snippet != null) {
            val snippetText = SpannableString(snippet)
            snippetText.setSpan(ForegroundColorSpan(Color.BLACK), 0, snippet.length, 0)
            snippetUi.text = snippetText
        } else {
            snippetUi.text = ""
        }
    }


}
