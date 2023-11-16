package site.x3la

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class Info : AppCompatActivity() {

    lateinit var textoLink: TextView
    lateinit var textoLink2: TextView
    lateinit var textoLink3: TextView


    lateinit var buttonRegresar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_info_contents)

        textoLink = findViewById(R.id.textViewTitulo2)
        textoLink2 = findViewById(R.id.textViewTitulo3)
        textoLink3 = findViewById(R.id.textViewTituloManual)

        buttonRegresar = findViewById(R.id.buttonRegresar)
        buttonRegresar.setOnClickListener(View.OnClickListener { //Get data from input field
            finish()
        })
        textoLink.setOnClickListener(View.OnClickListener { //Get data from input field
            getUrlFromIntent(this, "https://x3la.site/ser-proveedor-de-servicios/")
        })
        textoLink2.setOnClickListener(View.OnClickListener { //Get data from input field
            getUrlFromIntent(this, "https://x3la.site/ser-proveedor-de-servicios/")
        })
        textoLink3.setOnClickListener(View.OnClickListener { //Get data from input field
            getUrlFromIntent(this, "https://x3la.site/manual-de-usuario-x3la/")
        })
    }

    fun getUrlFromIntent(view: Info, link:String) {
        val url = link
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}