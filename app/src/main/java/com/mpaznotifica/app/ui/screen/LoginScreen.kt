
package com.mpaznotifica.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mpaznotifica.app.R
import com.mpaznotifica.app.viewmodel.MpazNotificaViewModel

/**
 * Pantalla de autenticación de MPAZ Notifica.
 *
 * Permite iniciar sesión con dos perfiles:
 * - Inspectoría General.
 * - Profesor/a Jefe.
 *
 * El apoderado no inicia sesión ni opera la aplicación.
 *
 * Las credenciales ficticias se validan mediante
 * login() del MpazNotificaViewModel utilizando
 * los usuarios definidos en MockData.
 *
 * Validaciones:
 * - Correo institucional obligatorio.
 * - Contraseña obligatoria.
 * - Credenciales incorrectas.
 *
 * La pantalla muestra los mensajes de error
 * proporcionados por el ViewModel mediante StateFlow.
 *
 * No permite el ingreso mediante modo desarrollo.
 *
 * Credenciales de demostración:
 * Inspectoría:
 * inspectoria@mpaznotifica.cl / inspectoria123
 *
 * Profesor Jefe:
 * profesor.4basicoA@mpaznotifica.cl / profesor123
 */
@Composable
fun LoginScreen(
    viewModel: MpazNotificaViewModel,
    onIngresar: () -> Unit
) {

    // Campos del formulario.
    var usuario by remember {
        mutableStateOf("")
    }

    var clave by remember {
        mutableStateOf("")
    }

    // Controla si la contraseña se muestra u oculta.
    var claveVisible by remember {
        mutableStateOf(false)
    }

    // Observa los errores de autenticación
    // publicados por el ViewModel.
    val error by viewModel.errorLogin.collectAsState()

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Logotipo de la aplicación.
            Image(
                painter = painterResource(
                    id = R.drawable.logo_app
                ),
                contentDescription = "Logo de MPAZ Notifica",
                modifier = Modifier.size(110.dp)
            )

            // Nombre de la aplicación.
            Text(
                text = "MPAZ Notifica",
                style = MaterialTheme.typography.headlineMedium
            )

            // Descripción del acceso.
            Text(
                text = "Ingreso de personal docente y directivo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // =================================================
            // CORREO INSTITUCIONAL
            // =================================================

            OutlinedTextField(
                value = usuario,
                onValueChange = {
                    usuario = it
                },
                label = {
                    Text("Correo institucional")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // =================================================
            // CONTRASEÑA
            // =================================================

            OutlinedTextField(
                value = clave,
                onValueChange = {
                    clave = it
                },
                label = {
                    Text("Clave de acceso")
                },
                singleLine = true,
                visualTransformation = if (claveVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                trailingIcon = {

                    // Permite mostrar u ocultar la contraseña.
                    IconButton(
                        onClick = {
                            claveVisible = !claveVisible
                        }
                    ) {

                        Icon(
                            imageVector = if (claveVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (claveVisible) {
                                "Ocultar clave"
                            } else {
                                "Mostrar clave"
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // =================================================
            // MENSAJES DE ERROR
            // =================================================

            if (error != null) {

                Text(
                    text = error.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            // =================================================
            // BOTÓN INGRESAR
            // =================================================

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    // Valida las credenciales mediante
                    // el ViewModel antes de ingresar.
                    val accesoPermitido = viewModel.login(
                        usuario = usuario.trim(),
                        clave = clave
                    )

                    if (accesoPermitido) {

                        // Navega al panel principal
                        // cuando las credenciales son válidas.
                        onIngresar()
                    }
                }
            ) {

                Text("Ingresar")
            }
        }
    }
}
