package site.x3la

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class VerProveedor : AppCompatActivity() {
    lateinit var nombre: TextView
    lateinit var telefono:TextView
    lateinit var direccion:TextView
    lateinit var email:TextView
    lateinit var web:TextView
    lateinit var descripcion:TextView
    lateinit var buttonRegresar:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_proveedor)

        nombre = findViewById(R.id.textViewTitulo)
        telefono = findViewById(R.id.textViewTelefono)
        direccion = findViewById(R.id.textViewDireccion)
        email = findViewById(R.id.textViewEmail)
        web = findViewById(R.id.textViewWeb)
        descripcion = findViewById(R.id.textViewDescripcion)
        buttonRegresar = findViewById(R.id.buttonRegresar)

        buttonRegresar.setOnClickListener(View.OnClickListener { //Get data from input field
            finish()
        })

        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            nombre.setText(bundle.getString("nombre"))
            var telefonoProveedor = bundle.getString("telefono")

            if (telefonoProveedor != "null"){
                telefono.text = telefonoProveedor
                if (telefonoProveedor != null) {
                    telefono.setOnClickListener(View.OnClickListener { //Get data from input field
                        call(this, telefonoProveedor)
                    })
                }
            } else {
                telefono.text = " - "
            }

            var direccionProveedor = bundle.getString("direccion")
            if (direccionProveedor != "null"){
                direccion.text = direccionProveedor
            } else {
                direccion.text = " - "
            }

            var emailProveedor = bundle.getString("email")
            if (emailProveedor != "null"){
                email.text = emailProveedor
                if (emailProveedor != null) {
                    email.setOnClickListener(View.OnClickListener { //Get data from input field
                        sendEmail(emailProveedor)
                    })
                }
            } else {
                email.text = " - "
            }

            var webProveedor = bundle.getString("web")
            if (webProveedor != "null"){
                web.text = webProveedor
                if (webProveedor != null) {
                    web.setOnClickListener(View.OnClickListener { //Get data from input field
                        getUrlFromIntent(this, webProveedor)
                    })
                }
            } else {
                web.text = " - "
            }

            var descripcionProveedor = bundle.getString("descripcion")
            if (descripcionProveedor != "null"){
                descripcion.text = descripcionProveedor
            } else {
                descripcion.text = " - "
            }

        }


    }

    fun call(view: VerProveedor, telefono1:String) {
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:" + telefono1)
        startActivity(dialIntent)
    }

    private fun sendEmail(recipient: String) {
        /*ACTION_SEND action to launch an email client installed on your Android device.*/
        val mIntent = Intent(Intent.ACTION_SEND)
        /*To send an email you need to specify mailto: as URI using setData() method
        and data type will be to text/plain using setType() method*/
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"
        // put recipient email in intent
        /* recipient is put as array because you may wanna send email to multiple emails
           so enter comma(,) separated emails, it will be stored in array*/
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        //put the Subject in the intent
        mIntent.putExtra(Intent.EXTRA_SUBJECT, "Quisiera contratar sus servicios")
        //put the message in the intent
        mIntent.putExtra(Intent.EXTRA_TEXT, "Hola, me interesa contratar sus servicios, mi informacion de contacto es: ")
        try {
            //start email intent
            startActivity(Intent.createChooser(mIntent, "Contacta con el proveedor"))
        }
        catch (e: Exception){
            //if any thing goes wrong for example no email client application or any exception
            //get and show exception message
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun getUrlFromIntent(view: VerProveedor, link:String) {
        val url = link
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }


}